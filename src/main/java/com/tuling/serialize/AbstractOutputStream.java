package com.tuling.serialize;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.sun.xml.internal.ws.developer.Serialization;
import com.tuling.serialize.util.*;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.util.*;

/**
 * Created by Administrator on 2020-06-12.
 */
public abstract class AbstractOutputStream implements ObjectOutputStream {

    private static final Logger LOGGER = Logger.getLogger(ObjectOutputStream.class);

    protected static final ThreadLocal<Context> threadLocal = new ThreadLocal();

    //默认不缓存类的字段信息
    private boolean isCacheField = true;

    private static final Map<Class, ObjectWrite> writerMap = new HashMap<>();

    private static final StringWrite stringWriter = new StringWrite();

    static {
        writerMap.put(boolean.class, new BooleanWrite());
        writerMap.put(Boolean.class, new BooleanWrite());
        writerMap.put(byte.class, new ByteWrite());
        writerMap.put(Byte.class, new ByteWrite());
        writerMap.put(char.class, new CharacterWrite());
        writerMap.put(Character.class, new CharacterWrite());
        writerMap.put(short.class, new ShortWrite());
        writerMap.put(Short.class, new ShortWrite());
        writerMap.put(int.class, new IntegerWrite());
        writerMap.put(Integer.class, new IntegerWrite());
        writerMap.put(long.class, new LongWrite());
        writerMap.put(Long.class, new LongWrite());
        writerMap.put(float.class, new FloatWrite());
        writerMap.put(Float.class, new FloatWrite());
        writerMap.put(double.class, new DoubleWrite());
        writerMap.put(Double.class, new DoubleWrite());
        writerMap.put(String.class, new StringWrite());
    }

    public AbstractOutputStream() {
        this(false);
    }

    public AbstractOutputStream(boolean isCacheField) {
        this.isCacheField = isCacheField;
    }

    @Override
    public void write(Object obj, OutputStream out) throws IOException {
        write(obj, true, out);
    }

    /**
     * 序列化某个对象
     *
     * @param obj              要序列化的对象
     * @param isWriteClassName 序列化时是否写入对象所属类的类名
     * @throws IOException
     */
    @Override
    public void write(Object obj, boolean isWriteClassName, OutputStream out) throws IOException {
        //写入当前序列化格式版本 上移动
        out.write(Constant.CURRENT_VERSION);
        Context context = Context.create();
        //先将对象数据写入缓冲,这样可以提高写入速度
        ByteBuf buf = new ByteBuf(256);

        write(obj, isWriteClassName, buf, context, true);
        //写入对象长度。对象长度占用的字节随着长度的变化而变化，尽量用更少的字节存储
        writeLengthOfObject(buf.readableBytes(), out);
        //写入对象内容
        out.write(buf.fullArray());
        buf = null;
        context.destory();
    }

    /**
     * 获得指定对象对应的序列化字节
     *
     * @param obj
     * @return
     */
    public byte[] getBytes(Object obj) {
        byte[] result = new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            this.write(obj, out);
            result = out.toByteArray();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;
    }

    /**
     * 获得指定对象对应的序列化字节
     *
     * @param obj              要获取对应字节数组的对象
     * @param isWriteClassName 序列化时是否写入对象所属类的类名
     * @return
     */
    public byte[] getBytes(Object obj, boolean isWriteClassName) {
        byte[] result = new byte[0];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            this.write(obj, isWriteClassName, out);
            result = out.toByteArray();
        } catch (IOException ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return result;

    }


    /**
     * 往输出流写入对象长度
     *
     * @param length
     * @param out
     * @throws IOException
     */
    private void writeLengthOfObject(int length, OutputStream out) throws IOException {
        //如果length 不大于32767，则写入2字节数据，否则写入4字节数据;当写入四字节数据时，第一高位为1
        if (length <= Short.MAX_VALUE) {
            out.write(NumberUtil.getByteArray((short) length));
        } else {
            out.write(NumberUtil.getByteArray(length | Integer.MIN_VALUE));
        }

    }

    /**
     * @param obj              要写入对象
     * @param isWriteClassName 是否写入对象所属类的类名
     * @param out              输出缓冲
     * @param context          序列化上下文
     * @param possibleBaseType 是否可能是基本数据类型(包括String)
     * @throws IOException
     */
    protected void write(Object obj, boolean isWriteClassName, ByteBuf out, Context context, boolean possibleBaseType) {
        if (obj == null) {
            this.writeNull(out);
        } else {
            context.put(obj);
            Class targetClass = obj.getClass();
            int type = ReflectUtil.getTypeOfClass(targetClass, obj);
            boolean isBaseType = possibleBaseType && type == ReflectUtil.BASETYPE;
            //1. 写入对象类型
            if (isWriteClassName) {
                this.writeClassName(obj, targetClass, out, context, isBaseType);
            }
            //判断是否是数组类型或集合类型
            if (type == ReflectUtil.ARRAY) {
                //2.写入元素个数
                int length = Array.getLength(obj);
                out.writeScalableInt(length);
                boolean isCommon = false;
                Object[] arrayObj = null;
                if (Object.class.isAssignableFrom(targetClass.getComponentType())) {
                    arrayObj = (Object[]) obj;
                    isCommon = true;
                }
                Class currentType = targetClass.getComponentType();
                //3. 循环写入数组中的元素
                for (int i = 0; i < length; i++) {
                    Object value = isCommon ? arrayObj[i] : Array.get(obj, i);
                    if (value != null && value.getClass() != currentType) {
                        currentType = value.getClass();
                        writeType(value,currentType,out,context);
                    }
                    this.writeValue(value, currentType, out, context);
                }
            } else {
                //标识序列化时是否走最普通逻辑
                boolean isExecuteCommon = false;
                //先写入该类类名及该类自定义的属性，然后写入父类名及父类定义的属性
                //判断是否是基本数据类型对应的包装类型
                if (isBaseType) {
                    this.writeValue(obj, targetClass, out, context);
                } else if (type == ReflectUtil.ENUM) {
                    out.writeString(obj.toString(), true);
                } else if (obj instanceof Collection) {
                    //先判断该集合是否支持add方法
                    try {
                        //先试探集合，看集合是否支持add()方法
                        Collection collection = (Collection) ReflectUtil.createObject(targetClass);
                        collection.add(1);
                        //写入集合元素个数
                        this.writeLengthOrIndex(((Collection) obj).size(), out);
                        Class currentType = Object.class;
                        for (Object item : (Collection) obj) {
                            if (item != null && item.getClass() != currentType) {
                                currentType = item.getClass();
                                this.writeType(item,currentType,out,context);
                            }
                            this.writeValue(item, currentType, out, context);
                        }
                    } catch (Exception ex) {
                        isExecuteCommon = true;
                    }
                } else  if(obj instanceof Map){
                    try {
                        //先试探集合，看集合是否支持put()方法
                        Map tempMap = (Map) ReflectUtil.createObject(targetClass);
                        if(tempMap == null){
                            if(BuilderUtil.isSpecifyBuilder(targetClass)){
                                tempMap =  (Map)BuilderUtil.get(targetClass).newInstance();
                            }
                        }
                        tempMap.put("",1);
                        //写入集合元素个数
                        this.writeLengthOrIndex(((Map) obj).size(), out);
                        Class keyType = Object.class;
                        Class valueType = Object.class;
                        for(Object item2 : ((Map)obj).entrySet()){
                            Map.Entry entry = (Map.Entry)item2;
                            Object key = entry.getKey();
                            if (key != null && key.getClass() != keyType) {
                                keyType = key.getClass();
                                writeType(key,keyType,out,context);
                            }
                            this.writeValue(key, keyType, out, context);

                            Object value = entry.getValue();
                            if (value != null && value.getClass() != valueType) {
                                valueType = value.getClass();
                                writeType(value,valueType,out,context);
                            }
                            this.writeValue(value, valueType, out, context);
                        }

                    } catch (Exception ex) {
                        isExecuteCommon = true;
                    }
                }else {
                    isExecuteCommon = true;
                }
                if (isExecuteCommon) {
                    //获得包含该类以及该类的所有父类的集合
                    List<Class> superClassAndSelfList = ReflectUtil.getSelfAndSuperClass(targetClass);
                    for (Class item : superClassAndSelfList) {
                        Field[] fields = ReflectUtil.getAllInstanceField(item, isCacheField);
                        //2. 写入属性个数  2字节
                        writeLengthOrIndex(fields.length, out);

                        //3. 循环写入属性
                        for (Field field : fields) {
                            context.setCurrentField(field);
                            this.writeField(field, obj, out, context);
                        }
                        context.setCurrentField(null);
                    }
                }
            }
        }
    }

    /**
     * 写入指定值value的类型信息
     * @param value  要写入对象
     * @param type   值所属类型
     */
    private void writeType(Object value,Class type,ByteBuf out, Context context){
        startWriteClassName(out);
        writeClassName(value, type, out, context, ReflectUtil.isBaseType(type));
    }

    /**
     * 写入对象索引、类索引或字段长度
     *
     * @param length 字段个数
     */
    protected final void writeLengthOrIndex(int length, ByteBuf buf) {
        if (length <= 127) {
            buf.writeByte(length - 128);
        } else {
            buf.writeShort(length);
        }
    }

    /**
     * 获得指定类的所有字段，将这些字段放入一个栈中。先压入该类自己定义的字段，然后依次压入上一级父类定义的字段
     *
     * @param targetClass  需要获取字段的类
     * @return 包含所有字段的栈
     */
    private Stack<Field> getAllFields(Class targetClass) {
        Stack<Field> stack = new Stack<Field>();
        while (targetClass != null) {
            this.putField(targetClass, stack);
            targetClass = targetClass.getSuperclass();
        }

        return stack;
    }

    /**
     * 将param 指定类自身定义的所有字段(不包括父类的字段)压入栈中
     *
     * @param targetClass 要将其字段压入栈中的类
     * @param stack 栈
     */
    private void putField(Class targetClass, Stack stack) {
        Field[] fields = targetClass.getDeclaredFields();
        for (Field item : fields) {
            stack.push(item);
        }
    }

    /**
     * 将指定类的类名输出到流中
     *
     * @param type
     * @param out  输出流
     * @throws IOException
     */
    protected void writeClassName(Object obj, Class type, ByteBuf out, Context context, boolean isBaseType) {
        if (type == null) {
            throw new IllegalArgumentException("type can't be null");
        }
        if (isBaseType) {
            writeBaseType(obj, out, type);
        } else {
                if (context.getCurrentField() != null && context.getCurrentField().getType() == type) {  //当值的实际类型和字段类型相同时，不需要写入类名，写入标识字符即可
                    out.writeByte(Constant.CLASSNAME_SAME_WITH_FIELD);
                } else {
                    //将类名写入缓冲
                    this.writeClass(out, type);
                }
        }

    }

    /***
     * 往缓冲中写入对类名的引用序号
     * @param out
     * @param index
     */
    public static void writeReferenceIndex(int index, ByteBuf out) {
        //当index 小于等于31时，只写入一字节，字节的高三位是100;当index大于31时，写入2字节，字节的高三位是101
        if (index <= 31) {  //1000 0000
            byte temp = (byte) (Constant.CLASSNAME_REFERENCE | index);
            out.writeByte(temp);
        } else {// 1010 0000
            out.writeShort((Constant.CLASSNAME_REFERENCE_OVER_FLOW << 8) | index);
        }
    }

    /**
     * 将指定对象 指定的属性写入输出流
     *
     * @param field
     * @param obj
     * @param out     输出流
     * @param context 序列化上下文
     * @throws IOException
     */
    protected abstract void writeField(Field field, Object obj, ByteBuf out, Context context);

    /**
     * 将类相关信息(类名以及类标识)写出到缓冲
     *
     * @param out     输出流
     * @param type  类型
     */
    protected void writeClass(ByteBuf out, Class type) {
        //1. 写入类标识
        out.writeScalableInt(ReflectUtil.getIdForClass(type));
        //2. 写入类名
        out.writeString(type.getName(), true);
    }

    public static void writeBaseType(Object obj, ByteBuf out, Class type) {
        int baseType = Constant.STRING;
        if (obj instanceof Boolean) {
            baseType = Constant.BOOLEAN;
        } else if (obj instanceof Byte) {
            baseType = Constant.BYTE;
        } else if (obj instanceof Character) {
            baseType = Constant.CHARACTER;
        } else if (obj instanceof Short) {
            baseType = Constant.SHORT;
        } else if (obj instanceof Integer) {
            baseType = Constant.INTEGER;
        } else if (obj instanceof Long) {
            baseType = Constant.LONG;
        } else if (obj instanceof Float) {
            baseType = Constant.FLOAT;
        } else if (obj instanceof Double) {
            baseType = Constant.DOUBLE;
        }
        //1100 0000
        out.writeByte(baseType);
    }

    protected void writeStart(ByteBuf out) {
        out.writeByte(Constant.BEGIN_FLAG);
    }

    protected void writeEnd(ByteBuf out) {
        out.writeByte(Constant.END_FLAG);
    }

    protected void writeNull(ByteBuf out) {
        out.writeByte(Constant.NULL_FLAG);
    }

    protected void writeNotNull(ByteBuf out) {
        out.writeByte(Constant.NOT_NULL_FLAG);
    }

    /**
     * 标识即将要在缓冲中写入类名
     *
     * @param out
     */
    protected void startWriteClassName(ByteBuf out) {
        out.writeByte(Constant.WRITE_CLASS_NAME_FLAG);
    }

    protected void writeContinue(ByteBuf out) {
        out.writeByte(Constant.CONTINUE_FLAG);
    }

    /**
     * 写入引用标记
     *
     * @throws IOException
     */
    protected void writeReference(ByteBuf out) {
        out.writeByte(Constant.REFERENCE_FLAG);
    }

    /**
     * 写入普通标记,当属性值的类型和属性类型一致时，写入该标记
     */
    protected void writeNormal(ByteBuf out,boolean isContainType) {
        out.writeByte(isContainType ? Constant.NORMAL_CONTAIN_CLASSNAME_FLAG  : Constant.NORMAL_WITHOUT_CLASSNAME_FLAG );
    }


    /**
     * 写入字符串
     *
     * @param content 要写入的字符串
     * @throws IOException
     */
    protected void writeString(String content, ByteBuf out) {
        stringWriter.write(out, content, 0);
    }

    //将一个short类型整数 转变成对应的字节数组
    private byte[] transfer(short num) {
        byte[] result = new byte[2];
        result[0] = (byte) (num / 256);
        result[1] = this.transformNumToByte(num % 256);
        return result;
    }


    /**
     * 将一个小于256的整数 转变成对应的byte表示
     *
     * @param num
     * @return
     */
    private byte transformNumToByte(int num) {
        return (num > 127) ? (byte) (num - 256) : (byte) num;
    }


    /**
     * 写入基本数据类型对应包装类对象的值
     *
     * @param value 要写入的值
     * @param out   输出流
     * @throws IOException
     */
    public void writeValue(Object value, Class type, ByteBuf out, Context context) {
        if (value == null) {
            this.writeNull(out);
            return;
        }
        if (type == null) {
            type = value.getClass();
        }
        ObjectWrite objectWrite = writerMap.get(type);
        if (objectWrite != null) {
            int length = this.getLength(value);
            out.writeByte(length);
            objectWrite.write(out, value, length);
        }else {
            //如果要写入的对象已经在当前序列化上下文中，则只需要写入其引用标识
            int index = context.getIndex(value);
            if (index >= 0) {
                this.writeReference(out);
                out.writeScalableInt(index);
            } else {
                boolean isDifferent = value.getClass() != type;
                this.writeNormal(out, isDifferent);
                this.write(value, isDifferent, out, context, isDifferent);
            }
        }
    }

    /**
     * 获取要写入对象在序列化流中占用的字节数
     * @param value 要写入的值
     * @return 要写入数据的长度，0表示不确定
     */
    private int getLength(Object value) {
        return value instanceof Integer ? NumberUtil.getLength((Integer) value) : (value instanceof Long ? NumberUtil.getLength((Long) value) : 0);
    }

    private static interface ObjectWrite<T> {
        /**
         * 将值写入指定流中
         *
         * @param out
         * @param value
         * @param length 需要写入的字节数
         */
        public void write(ByteBuf out, T value, int length);
    }

    private static class BooleanWrite implements ObjectWrite<Boolean> {
        public void write(ByteBuf out, Boolean value, int length) {
            out.writeBoolean(value);
        }
    }

    private static class ByteWrite implements ObjectWrite<Byte> {
        public void write(ByteBuf out, Byte value, int length) {
            out.writeByte(value);
        }
    }

    private static class CharacterWrite implements ObjectWrite<Character> {
        public void write(ByteBuf out, Character value, int length) {
            out.writeChar(value.charValue());
        }
    }

    private static class ShortWrite implements ObjectWrite<Short> {
        public void write(ByteBuf out, Short value, int length) {
            out.writeShort(value);
        }
    }

    private static class IntegerWrite implements ObjectWrite<Integer> {
        public void write(ByteBuf out, Integer value, int length) {
            out.writeInt(value, length);
        }
    }

    private static class LongWrite implements ObjectWrite<Long> {
        public void write(ByteBuf out, Long value, int length) {
            out.writeLong(value, length);
        }
    }

    private static class FloatWrite implements ObjectWrite<Float> {
        public void write(ByteBuf out, Float value, int length) {
            out.writeFloat(value);
        }
    }

    private static class DoubleWrite implements ObjectWrite<Double> {
        public void write(ByteBuf out, Double value, int length) {
            out.writeDouble(value);
        }
    }

    public static class StringWrite implements ObjectWrite<String> {
        public void write(ByteBuf out, String value, int length) {
            //写入字符串
            out.writeString(value);
        }
    }

    public static class EnumWrite implements ObjectWrite<Enum> {
        public void write(ByteBuf out, Enum value, int length) {
            //写入枚举名称
            out.writeString(value.name());
        }
    }
}
