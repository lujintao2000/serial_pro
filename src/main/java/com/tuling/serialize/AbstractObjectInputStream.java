package com.tuling.serialize;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import com.tuling.serialize.exception.*;
import com.tuling.serialize.util.*;
import org.apache.log4j.Logger;

/**
 * 该类主要实现了对象的反序列化
 *
 * @author lujintao
 * @date 2017-04-16
 */
public abstract class AbstractObjectInputStream implements ObjectInputStream {

    private static final Logger LOGGER = Logger.getLogger(AbstractObjectInputStream.class);
    private static final Map<Class, ObjectRead> readMap = new HashMap<>();


    static {
        readMap.put(boolean.class, new BooleanRead());
        readMap.put(Boolean.class, new BooleanRead());
        readMap.put(byte.class, new ByteRead());
        readMap.put(Byte.class, new ByteRead());
        readMap.put(char.class, new CharacterRead());
        readMap.put(Character.class, new CharacterRead());
        readMap.put(short.class, new ShortRead());
        readMap.put(Short.class, new ShortRead());
        readMap.put(int.class, new IntegerRead());
        readMap.put(Integer.class, new IntegerRead());
        readMap.put(long.class, new LongRead());
        readMap.put(Long.class, new LongRead());
        readMap.put(float.class, new FloatRead());
        readMap.put(Float.class, new FloatRead());
        readMap.put(double.class, new DoubleRead());
        readMap.put(Double.class, new DoubleRead());
        readMap.put(String.class, new StringRead());
        readMap.put(Enum.class, new EnumRead());
    }

    //是否缓存类的字段信息
    protected boolean isCacheField = true;


    public AbstractObjectInputStream() {
    }

    public AbstractObjectInputStream(boolean isCacheField) {
        this.isCacheField = isCacheField;
    }

    public Object readObject(InputStream in) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        return readObject(null, in);
    }


    /**
     * 读取数组对象
     *
     * @param context 序列化上下文
     * @param type    数组类型名，如String,Integer
     * @param in      包含序列化数据的输入流
     * @return
     */
    protected Object readArray(Context context, Class type, ByteBuf in) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        Object obj = null;
        //3.读取数组长度
        int length = this.readArrayLength(in);
        obj = Array.newInstance(type, length);
        context.put(obj,false);
        boolean isCommon = false;
        Object[] arrayObj = null;
        if (Object.class.isAssignableFrom(type)) {
            arrayObj = (Object[]) obj;
            isCommon = true;
        }
        Class arrayType = type;
        //标识数组元素是否是基本类型
        boolean isBaseType = ReflectUtil.isBaseType(arrayType);
        //判断是否是基本数据类型数组
        if (isCommon) {
            Class itemType = arrayType;
            for (int i = 0; i < length; i++) {
                if (hasWriteClassName(in)) {
                    itemType = this.readClass(in, context);
                    isBaseType = ReflectUtil.isBaseType(itemType);
                } else {
                    in.decreaseReaderIndex(1);
                }
                arrayObj[i] = this.readValue(itemType, in, context, isBaseType ? SituationEnum.MUST_BE : SituationEnum.MUST_NOT);
            }
        } else {
            dealArrayWithBaseType(arrayType,in,obj,length);
        }
        return obj;
    }

    /**
     * 读取java基本类型的数组
     * @param type  数组类型
     * @param out   数组要写入到的缓冲
     * @param obj   需要写入的数组
     * @param length  数组长度
     */
    private void dealArrayWithBaseType(Class type,ByteBuf in,Object obj,int length){
        if (type == int.class) {
            int[] temp = (int[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readIntWithScala();
            }
        } else if (type == long.class) {
            long[] temp = (long[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readLongWithScala();
            }
        } else if (type == boolean.class) {
            boolean[] temp = (boolean[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readBoolean();
            }
        } else if (type == byte.class) {
            byte[] temp = (byte[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readByte();
            }
        } else if (type == short.class) {
            short[] temp = (short[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readShort();
            }
        } else if (type == char.class) {
            char[] temp = (char[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readChar();
            }
        } else if (type == float.class) {
            float[] temp = (float[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readFloat();
            }
        } else if (type == double.class) {
            double[] temp = (double[]) obj;
            for (int i = 0; i < length; i++) {
                temp[i] = in.readDouble();
            }
        }
    }

    /**
     * 读取集合对象中的元素，将其放入集合中
     *
     * @param obj     要装入元素的集合对象
     * @param in      包含序列化数据的输入流
     * @param context 序列化上下文
     * @return
     */
    protected Object readCollection(Collection obj, ByteBuf in, Context context) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        int size = this.readCollectionSize(in);
        for (int i = 0; i < size; i++) {
            obj.add(this.readValue(Object.class, in, context, SituationEnum.POSSIBLE_BE));
        }
        return obj;
    }

    /**
     * 读取key,value,将其存入Map对象
     *
     * @param obj     要装入key,value的对象
     * @param in      包含序列化数据的输入流
     * @param context 序列化上下文
     * @return
     */
    protected Object readMap(Map obj, ByteBuf in, Context context) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        int size = this.readMapSize(in);
        for (int i = 0; i < size; i++) {
            ((Map) obj).put(this.readValue(Object.class, in, context, SituationEnum.POSSIBLE_BE), this.readValue(Object.class, in, context, SituationEnum.POSSIBLE_BE));
        }
        return obj;
    }

    /**
     * 当不能调用无参构造方法创建对象时，就用该方法来完成对象的创建及设值工作
     *
     * @param objectClass
     * @param context
     * @param in          包含序列化数据的输入流
     * @return
     */
//    protected Object readObjectWithAnother(Class objectClass, Context context, ByteBuf in) throws IOException, ClassNotSameException, ClassNotFoundException, InvalidDataFormatException, IllegalAccessException, InvalidAccessException, BuilderNotFoundException {
//        Object obj = null;
//        Map<String, Object> map = new HashMap<>();
//        map = (Map) readValue(obj, ReflectUtil.getAllFields(objectClass), context, in);
//        Builder builder = BuilderUtil.get(objectClass);
//        obj = builder.newInstance();
//        return obj;
//    }


    /**
     * 读取值，将值存入指定的对象中,如果obj为空,则将读取的值存入Map
     *
     * @param obj     需要读入值的对象
     * @param context 序列化上下文
     * @param in      包含序列化数据的输入流
     * @return 存储了读取值的对象
     */
    protected abstract Object readValue(Object obj, Context context, ByteBuf in) throws IOException, ClassNotSameException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, BuilderNotFoundException;


    /**
     * 读取值，将值存入指定的对象中,如果obj为空,则将读取的值存入Map
     *
     * @param obj         需要读入值的对象
     * @param objectClass 要读取对象的类型
     * @param context     序列化上下文
     * @param in          包含序列化数据的输入流
     * @return 存储了读取值的对象
     */
//    protected abstract Object readValue(List<PreObject> list, Class objectClass, Context context, ByteBuf in) throws IOException, ClassNotSameException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, BuilderNotFoundException;


    /**
     * 判断是否是对象读取的开始位置
     *
     * @param in 包含序列化数据的输入流
     */
    protected boolean start(ByteBuf in) throws IOException {
        return in.readByte() == (byte) Constant.BEGIN_FLAG;
    }


    /**
     * 判断是否是对象读取的结束位置
     *
     * @param in 包含序列化数据的缓冲
     */
    protected boolean end(ByteBuf in) throws IOException {
        return in.readByte() == (byte) Constant.END_FLAG;
    }

    /**
     * 判断当前的值是否为空标识
     *
     * @param in 包含序列化数据的缓冲
     * @throws IOException
     */
    protected boolean isNull(ByteBuf in) throws IOException {
        return in.readByte() == (byte) Constant.NULL_FLAG;
    }

    /**
     * 读取属性总个数或对象索引、类索引
     *
     * @param in 包含序列化数据的缓冲
     * @throws IOException
     */
    public final short readLengthOrIndex(ByteBuf in) throws IOException {
        short result = in.readByte();
        if (result < 0) {
            result += 128;
        } else {
            result = (short) ((result << 8) | (in.readByte() & 0xff));
        }
        return result;
    }

    /**
     * 读取短整型值
     *
     * @param in 包含序列化数据的输入流
     * @return
     * @throws IOException
     */
    protected Short readShort(ByteBuf in) throws IOException {
        return in.readShort();
    }

    /**
     * 读取整型值
     *
     * @param in 包含序列化数据的缓冲
     * @return
     * @throws IOException
     */
    protected Integer readInt(ByteBuf in) throws IOException {
        return in.readInt();
    }

    /**
     * 读取整型值
     *
     * @param in 包含序列化数据的输入流
     * @return
     * @throws IOException
     */
    protected Integer readInt(InputStream in) throws IOException {
        byte[] array = new byte[4];
        in.read(array);
        return array[0] << 24 |
                (array[1] & 0xff) << 16 |
                (array[2] & 0xff) << 8 |
                (array[3] & 0xff);
    }

    /**
     * 读取布尔值
     *
     * @param in 包含序列化数据的输入流
     * @return
     * @throws IOException
     */
    protected Boolean readBoolean(ByteBuf in) throws IOException {
        return in.readBoolean();
    }

    /***
     * 读取字符类型数据
     * @param in  包含序列化数据的输入流
     * @return
     * @throws IOException
     */
    protected Character readCharacter(ByteBuf in) throws IOException {
        return in.readChar();
    }

    /**
     * 读取字节型数据
     *
     * @return
     * @throws IOException
     */
    protected Byte readByte(ByteBuf in) throws IOException {
        return in.readByte();
    }

    /**
     * 读取长整型数据
     *
     * @param in 包含序列化数据的输入流
     * @return
     * @throws IOException
     */
    protected Long readLong(ByteBuf in) throws IOException {
        return in.readLong();
    }

    protected Float readFloat(ByteBuf in) throws IOException {
        return in.readFloat();
    }

    protected Double readDouble(ByteBuf in) throws IOException {
        return in.readDouble();
    }

    /**
     * 读取字符串
     *
     * @param in 包含序列化数据的输入流
     * @return
     * @throws IOException
     */
    protected String readString(ByteBuf in) throws IOException {
        return in.readString();
    }

    /**
     * 从输入流中读取属性的值并给属性设置值
     *
     * @param obj
     * @param type    属性定义所在的类
     * @param field   当前在读取的字段
     * @param in      包含序列化数据的缓冲
     * @param context 序列化上下文
     * @throws IOException
     * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
     */
    protected void readField(Object obj, Class type, Field field, ByteBuf in, Context context) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        try {
            try {
                field.set(obj, this.readValue(field.getType(), in, context, SituationEnum.POSSIBLE_BE));
            } catch (IllegalArgumentException e) {
                LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
                throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
            } catch (IllegalAccessException e) {
                LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
                throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
            }
        } catch (SecurityException e) {
            LOGGER.error(String.format("属性 %s 访问受限", field.getName()), e);
            throw new InvalidAccessException(String.format("属性 %s 访问受限", field.getName()), e);
        }
    }

    /**
     * 从输入流中读取字段的值，将值放到map中
     *
     * @param map     存放值的map
     * @param field   当前即将要读取的字段
     * @param in      包含序列化数据的缓冲
     * @param context 序列化上下文
     * @throws IOException
     * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
     */
    protected Map readField(Map map, Field field, ByteBuf in, Context context) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        if (map == null) {
            throw new IllegalArgumentException("map can't be null");
        }
        map.put(field.getName(), this.readValue(field.getType(), in, context, SituationEnum.POSSIBLE_BE));
        return map;
    }

    /**
     * 读取当前要反序列化的对象的类名
     *
     * @param in      包含序列化数据的输入流
     * @param context 序列化上下文
     * @return 读取的类名
     * @throws IOException
     * @throws ClassNotFoundException 当类名对应的类不存在时，抛出此异常
     */
    protected Class readClass(ByteBuf in, Context context) throws IOException, ClassNotFoundException {
        Class result = null;
        //1. 读入类名字节长度,该次读取读到的并不一定是真实的字符串长度
        byte preLength = in.readByte();
        if (preLength > 0) {
            in.decreaseReaderIndex(1);
            //根据类标识获取与之对应的类；如果存在对应的类，就跳过类名数据读取；否则，读取类名数据，然后读取的类与标识绑定
            //读取类标识
            int classId = in.readScalableInt();
            result = context.getClassById(classId);
            if (result == null) {
                //读入类名
                String fullClassName = in.readString(true);
                if (fullClassName.endsWith("[]")) {
                    Class arrayType = ReflectUtil.get(fullClassName.substring(0, fullClassName.length() - 2));
                    result = Array.newInstance(arrayType, 0).getClass();
                } else {
                    result = ReflectUtil.getComplexClass(fullClassName);
                }
                context.addClassId(result, classId);
            }

        } else if (preLength == Constant.CLASSNAME_SAME_WITH_FIELD) {
            //字段值的类型和字段类型相同
            Class type = context.getCurrentField().getType();
            return type;
        } else if (preLength > (byte) 0xbf) {
            //类型是基本类型
            switch (preLength) {
                case Constant.BOOLEAN:
                    return Boolean.class;
                case Constant.BYTE:
                    return Byte.class;
                case Constant.CHARACTER:
                    return Character.class;
                case Constant.SHORT:
                    return Short.class;
                case Constant.INTEGER:
                    return Integer.class;
                case Constant.LONG:
                    return Long.class;
                case Constant.FLOAT:
                    return Float.class;
                case Constant.DOUBLE:
                    return Double.class;
                default:
                    return String.class;
            }
        }
        return result;
    }

    /**
     * 读入引用的类序号
     *
     * @param in
     * @param firstByte 已经读取的字节
     * @return
     */
    public static short readReferenceIndex(ByteBuf in, byte firstByte) {
        short result = 0;
        if (firstByte < (byte) 0xa0) {   //index <= 31
            result = (short) (firstByte & 0x1f);
        } else {
            byte secondByte = in.readByte();
            result = (short) (((firstByte & 0x1f) << 8) | (secondByte & 0xff));
        }
        return result;
    }

    /**
     * 读取数组长度
     *
     * @param in 包含序列化数据的缓冲
     * @return
     * @throws IOException
     */
    protected int readArrayLength(ByteBuf in) throws IOException {
        return in.readScalableInt();
    }

    /**
     * 读取集合元素个数
     *
     * @param in 包含序列化数据的缓冲
     * @return
     * @throws IOException
     */
    protected int readCollectionSize(ByteBuf in) throws IOException {
        return in.readScalableInt();
    }

    /**
     * 读取Map元素个数
     *
     * @param in 包含序列化数据的缓冲
     * @return
     * @throws IOException
     */
    protected int readMapSize(ByteBuf in) throws IOException {
        return in.readScalableInt();
    }

    /**
     * 判断当前即将要读取到的对象是否为已反序列化对象的引用
     *
     * @param in 包含序列化数据的缓冲
     * @return
     * @throws IOException
     */
    protected boolean isReference(ByteBuf in) throws IOException {
        return in.readByte() == (byte) Constant.REFERENCE_FLAG;
    }


    /**
     * 从流中当前位置读取指定类型的值
     *
     * @param type           要读取数据的类型
     * @param count          要读取对象的个数
     * @param in             包含序列化数据的缓冲
     * @param context        序列化上下文
     * @param isSureBaseType 是否确定是基本类型(包括String)
     * @return
     */
//    protected Object[] readValue(Class type, int count, ByteBuf in, Context context, boolean isSureBaseType) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
//        List list = new ArrayList();
//        for (int i = 0; i < count; i++) {
//            if (isNull(in)) {
//                list.add(null);
//                count++;
//                continue;
//            } else {
//                in.decreaseReaderIndex(1);
//            }
//
//            Object value = null;
//            ObjectRead objectRead = null;
//            if (isSureBaseType | (objectRead = readMap.get(type)) != null) {
//                if (objectRead == null) {
//                    objectRead = readMap.get(type);
//                }
//                value = objectRead.read(in, type, 0);
//            } else {
//                byte currentByte = in.readByte();
//                if (currentByte == Constant.REFERENCE_FLAG) {
//                    int index = in.readScalableInt();
//                    value = context.get(index);
//                } else {
//                    value = this.readObject(currentByte == Constant.NORMAL_CONTAIN_CLASSNAME_FLAG ? null : type, in, context);
//                    context.put(value,false);
//                }
//            }
//            list.add(value);
//        }
//        return list.toArray();
//    }

    /**
     * 从流中当前位置读取指定类型的值
     *
     * @param type           要读取数据的类型
     * @param in             包含序列化数据的缓冲
     * @param context        序列化上下文
     * @param situationEnum  对是否是基本类型做出的判断
     * @return
     */
    protected Object readValue(Class type, ByteBuf in, Context context, SituationEnum situationEnum) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        if (isNull(in)) {
            return null;
        } else {
            in.decreaseReaderIndex(1);
        }
        Object value = null;
        ObjectRead objectRead = null;

        if (situationEnum == SituationEnum.MUST_BE || ( situationEnum == SituationEnum.POSSIBLE_BE && (objectRead = readMap.get(type)) != null)) {
            if (objectRead == null) {
                objectRead = readMap.get(type);
            }
            value = objectRead.read(in, type, 0);
        } else {
            byte currentByte = in.readByte();
            if (currentByte == Constant.REFERENCE_FLAG) {
                int index = in.readScalableInt();
                value = context.get(index);
            } else {
                value = this.readObject(currentByte == Constant.NORMAL_CONTAIN_CLASSNAME_FLAG ? null : type, in, context);
            }
        }
        return value;
    }

    /**
     * 从流中当前位置读取普通类型对象
     *
     * @param type    要读取数据的类型
     * @param in      包含序列化数据的缓冲
     * @param context 序列化上下文
     * @return
     */
    protected Object readGeneralObject(Class type, ByteBuf in, Context context) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        if (isNull(in)) {
            return null;
        } else {
            in.decreaseReaderIndex(1);
        }
        Object value = null;
        byte currentByte = in.readByte();
        if (currentByte == Constant.REFERENCE_FLAG) {
            int index = in.readScalableInt();
            value = context.get(index);
        } else {
            value = this.readObject(currentByte == Constant.NORMAL_CONTAIN_CLASSNAME_FLAG ? null : type, in, context);
        }

        return value;
    }


    /**
     * 根据类型标识获取对应的类型
     *
     * @param typeFlag
     * @return
     */
    private Class getType(byte typeFlag) {
        Class result = null;
        switch (typeFlag) {
            case Constant.BOOLEAN_FLAG:
                result = Boolean.class;
                break;
            case Constant.BYTE_FLAG:
                result = Byte.class;
                break;
            case Constant.CHAR_FLAG:
                result = Character.class;
                break;
            case Constant.SHORT_FLAG:
                result = Short.class;
                break;
            case Constant.INT_FLAG:
                result = Integer.class;
                break;
            case Constant.LONG_FLAG:
                result = Long.class;
                break;
            case Constant.FLOAT_FLAG:
                result = Float.class;
                break;
            case Constant.DOUBLE_FLAG:
                result = Double.class;
                break;
            case Constant.STRING_FLAG:
                result = String.class;
                break;
            default:
                result = Enum.class;
        }
        return result;
    }

    /**
     * @param objectClass 需要反序列化对象的类型
     * @param in          包含序列化数据的输入流
     * @return 反序列化出的对象
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
     * @throws InvalidAccessException     如果方法或字段不让访问或方法传递参数不对，抛出该异常
     * @throws ClassNotSameException      当反序列化时加载的类的属性与序列化时类的属性不一致时，抛出此异常
     */
    public Object readObject(Class objectClass, InputStream in) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        //判断序列化格式版本是否支持
        int version = readVersion(in);
        if (version < Constant.MIN_VERSION || version > Constant.MAX_VERSION) {
            throw new VersionNotSupportException("Current version of serialization is not supported.Current version is " + version + ",but " + (version < Constant.MIN_VERSION ? "the mininum supported version is " + Constant.MIN_VERSION : "the maximum supported version is " + Constant.MAX_VERSION));
        }
        //读取对象长度
        int length = readLengthOfObject(in);
        byte[] objectData = new byte[length];
        in.read(objectData);

        //先将对象数据读到缓冲
        ByteBuf buf = new ByteBuf(objectData);

        Context context = new Context();
        Object result = readObject(objectClass, buf, context);
        buf.release();
        context.destory();
        return result;
    }

    private int readLengthOfObject(InputStream in) throws IOException {
        int result = 0;
        byte[] temp = new byte[2];
        in.read(temp);
        result = ((temp[0] << 8) | (temp[1] & 0xff));
        if (result < 0) {
            byte[] temp2 = new byte[2]; //0111 1111
            in.read(temp2);
            result = ((result & 0x7fff) << 16) | ((temp2[0] & 0xff) << 8) | (temp2[1] & 0xff);
        }
        return result;
    }

    /**
     * @param objectClass 需要反序列化对象的类型
     * @param in          包含序列化数据的输入流
     * @param context     序列化上下文
     * @return 反序列化出的对象
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
     * @throws InvalidAccessException     如果方法或字段不让访问或方法传递参数不对，抛出该异常
     * @throws ClassNotSameException      当反序列化时加载的类的属性与序列化时类的属性不一致时，抛出此异常
     */
    public Object readObject(Class objectClass, ByteBuf in, Context context) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        Object obj = null;
        if (!this.isNull(in)) {
            if (objectClass == null) {
                //读取类信息
                objectClass = this.readClass(in, context);
            }

            //判断是否是数组类型
            if (objectClass.isArray()) {
                obj = readArray(context, objectClass.getComponentType(), in);
            } else {
                obj = readObjectWithOutArray(context, objectClass, in);
            }
        }
        return obj;
    }


    /**
     * 读取当前对象数据的序列化格式版本
     *
     * @param in
     * @return
     */
    private int readVersion(InputStream in) throws IOException {
        return in.read();
    }

    /**
     * 读取数组类型之外的对象
     *
     * @param context
     * @param objectClass
     * @param in          包含序列化数据的缓冲
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidDataFormatException
     * @throws InvalidAccessException
     * @throws ClassNotSameException
     * @throws BuilderNotFoundException
     */
    protected Object readObjectWithOutArray(Context context, Class objectClass, ByteBuf in) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        Object obj = null;
        try {
            int typeFlag = ReflectUtil.getTypeOfClass(objectClass);
            if (typeFlag == ReflectUtil.BASETYPE) {
                obj = readValue(objectClass, in, context, SituationEnum.MUST_BE);
                context.put(obj,false);
            } else if (typeFlag == ReflectUtil.ENUM) {
                String name = in.readString(true);
                try {
                    obj = ReflectUtil.getEnum(objectClass, name);
                } catch (Exception e) {
                    LOGGER.error("Can't find enum element with name " + name + "", e);
                }
                context.put(obj,false);
            } else {
                try {
                    obj = createObject(objectClass);
                    //将当前对象放入上下文中
                    context.put(obj,false);
                    //是否执行普通方式读取
                    boolean isExecuteCommon = false;
                    if (obj instanceof Collection) {
                        try {
                            Collection collection = (Collection) ReflectUtil.createObject(objectClass);
                            collection.add(1);
                            int size = readLengthOrIndex(in);
                            Class currentType = Object.class;
                            //判断集合元素是否是基本类型
                            boolean isBaseType = false;
                            for (int i = 0; i < size; i++) {
                                if (hasWriteClassName(in)) {
                                    currentType = readClass(in, context);
                                    isBaseType = ReflectUtil.isBaseType(currentType);
                                } else {
                                    in.decreaseReaderIndex(1);
                                }
                                ((Collection) obj).add(readValue(currentType, in, context, isBaseType ? SituationEnum.MUST_BE : SituationEnum.MUST_NOT));
                            }
                        } catch (UnsupportedOperationException ex) {
                            isExecuteCommon = true;
                        }
                    } else if (obj instanceof Map) {
                        try {
                            //先试探集合，看集合是否支持put()方法
                            Map tempMap = (Map) ReflectUtil.createObject(objectClass);
                            tempMap.put("", 1);
                            int size = readLengthOrIndex(in);
                            Class keyType = Object.class;
                            Class valueType = Object.class;
                            Object key = null;
                            Object value = null;
                            //标识集合中元素key的类型是否是基本类型
                            boolean isKeyBaseType = false;
                            //标识集合中元素value的类型是否是基本类型
                            boolean isValueBaseType = false;
                            for (int i = 0; i < size; i++) {
                                if (hasWriteClassName(in)) {
                                    keyType = readClass(in, context);
                                    isKeyBaseType = ReflectUtil.isBaseType(keyType);
                                } else {
                                    in.decreaseReaderIndex(1);
                                }
                                key = readValue(keyType, in, context, isKeyBaseType ? SituationEnum.MUST_BE : SituationEnum.MUST_NOT);

                                if (hasWriteClassName(in)) {
                                    valueType = readClass(in, context);
                                    isValueBaseType  = ReflectUtil.isBaseType(valueType);
                                } else {
                                    in.decreaseReaderIndex(1);
                                }
                                value = readValue(valueType, in, context, isValueBaseType ? SituationEnum.MUST_BE : SituationEnum.MUST_NOT);
                                ((Map) obj).put(key, value);
                            }
                        } catch (UnsupportedOperationException ex) {
                            isExecuteCommon = true;
                        }
                    } else {
                        isExecuteCommon = true;
                    }
                    if (isExecuteCommon) {
                        readValue(obj, context, in);
                    }
                } catch (Exception e) {
                    LOGGER.error(e.getCause(), e);
                    throw new InvalidDataFormatException(e.getMessage(), e);
                }
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }
        return obj;
    }

    /**
     * 创建指定类的对象
     *
     * @param type 类型
     * @return 指定类型的对象
     * @throws BuilderNotFoundException
     */
    private Object createObject(Class type) {
        return ReflectUtil.createObject(type);
    }

    /**
     * 缓冲中接下来的数据是否是类名数据
     *
     * @return
     */
    private boolean hasWriteClassName(ByteBuf in) {
        return in.readByte() == Constant.WRITE_CLASS_NAME_FLAG;
    }

    /**
     * 将指定的字节数组反序列化为对象
     *
     * @param value
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidDataFormatException
     * @throws InvalidAccessException
     * @throws ClassNotSameException
     * @throws BuilderNotFoundException
     */
    public Object readObject(byte[] value) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("Value can't be null");
        }
        InputStream in = new ByteArrayInputStream(value);
        return readObject(in);
    }

    /**
     * 将指定的字节数组反序列化为对象
     *
     * @param value 包含序列化数据的字节数组
     * @param type  反序列化的类型
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     * @throws InvalidDataFormatException
     * @throws InvalidAccessException
     * @throws ClassNotSameException
     * @throws BuilderNotFoundException
     */
    public Object readObject(byte[] value, Class type) throws IOException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, ClassNotSameException, BuilderNotFoundException {
        if (value == null || value.length == 0) {
            throw new IllegalArgumentException("Value can't be null");
        }
        InputStream in = new ByteArrayInputStream(value);
        return readObject(type, in);
    }

    private static interface ObjectRead<T> {
        public T read(ByteBuf in, Class type, int length);
    }

    private static class BooleanRead implements ObjectRead<Boolean> {
        public Boolean read(ByteBuf in, Class type, int length) {
            return in.readBoolean();
        }
    }

    private static class ByteRead implements ObjectRead<Byte> {
        public Byte read(ByteBuf in, Class type, int length) {
            return in.readByte();
        }
    }

    private static class CharacterRead implements ObjectRead<Character> {
        public Character read(ByteBuf in, Class type, int length) {
            return in.readChar();
        }
    }

    private static class ShortRead implements ObjectRead<Short> {
        public Short read(ByteBuf in, Class type, int length) {
            return in.readShort();
        }
    }

    private static class IntegerRead implements ObjectRead<Integer> {
        public Integer read(ByteBuf in, Class type, int length) {
            return in.readIntWithScala();
        }
    }

    private static class LongRead implements ObjectRead<Long> {
        public Long read(ByteBuf in, Class type, int length) {
            return in.readLongWithScala();
        }
    }

    private static class FloatRead implements ObjectRead<Float> {
        public Float read(ByteBuf in, Class type, int length) {
            return in.readFloat();
        }
    }

    private static class DoubleRead implements ObjectRead<Double> {
        public Double read(ByteBuf in, Class type, int length) {
            return in.readDouble();
        }
    }

    private static class StringRead implements ObjectRead<String> {
        public String read(ByteBuf in, Class type, int length) {
            return in.readString();
        }
    }

    private static class EnumRead implements ObjectRead<Enum> {
        public Enum read(ByteBuf in, Class type, int length) {
            String name = in.readString(true);
            try {
                Method method = type.getMethod("valueOf", String.class);
                return (Enum) method.invoke(null, name);
            } catch (Exception e) {
                LOGGER.error("Can't find enum element with name " + name + "", e);
                return null;
            }
        }
    }


    /**
     * 表示尚未完成初始化的对象
     */
    protected static class PreObject {
        private Object obj;
        private ByteBuf buf;


        public PreObject(Object obj, ByteBuf buf) {
            this.obj = obj;
            this.buf = buf;

        }

        public Object getObj() {
            return obj;
        }

        public ByteBuf getBuf() {
            return buf;
        }


    }
}
