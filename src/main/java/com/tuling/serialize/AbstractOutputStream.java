package com.tuling.serialize;

import com.tuling.serialize.util.Constant;
import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import org.apache.log4j.Logger;
import org.msgpack.io.Output;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2020-06-12.
 */
public abstract class AbstractOutputStream implements ObjectOutputStream{

    private static final Logger LOGGER = Logger.getLogger(ObjectOutputStream.class);

    protected static final ThreadLocal<Context> threadLocal = new ThreadLocal();


    //默认不缓存类的字段信息
    private boolean isCacheField = true;

    private static final Map<Class,ObjectWrite> writerMap = new HashMap<>();

    static{
        writerMap.put(boolean.class, new BooleanWrite());
        writerMap.put(Boolean.class, new BooleanWrite());
        writerMap.put(byte.class,new ByteWrite());
        writerMap.put(Byte.class,new ByteWrite());
        writerMap.put(char.class,new CharacterWrite());
        writerMap.put(Character.class, new CharacterWrite());
        writerMap.put(short.class,new ShortWrite());
        writerMap.put(Short.class, new ShortWrite());
        writerMap.put(int.class,new IntegerWrite());
        writerMap.put(Integer.class, new IntegerWrite());
        writerMap.put(long.class,new LongWrite());
        writerMap.put(Long.class, new LongWrite());
        writerMap.put(float.class,new FloatWrite());
        writerMap.put(Float.class, new FloatWrite());
        writerMap.put(double.class,new DoubleWrite());
        writerMap.put(Double.class, new DoubleWrite());
        writerMap.put(String.class, new StringWrite());
    }

    public AbstractOutputStream(){
        this(false);
    }

    public AbstractOutputStream( boolean isCacheField){
        this.isCacheField = isCacheField;
    }

    @Override
    public void write(Object obj,OutputStream out) throws IOException{
        write(obj,true,out);
    }

    /**
     * 序列化某个对象
     * @param obj 要序列化的对象
     * @param isWriteClassName  序列化时是否写入对象所属类的类名
     * @throws IOException
     */
    @Override
    public void write(Object obj, boolean isWriteClassName, OutputStream out) throws IOException{            Context context = threadLocal.get();
        if(obj == null){
            this.writeNull(out);
        }else{
            this.writeNotNull(out);
            if(context == null){
                context = new Context();
                threadLocal.set(context);
                //写入当前序列化格式版本
                out.write(Constant.CURRENT_VERSION);
            }
            context.enter();
            context.put(obj);
            //1. 写入对象类型
            if(isWriteClassName){
                this.writeClassName(obj.getClass(),out);
            }

            //判断是否是数组类型或集合类型
            if(obj.getClass().isArray()){
                //2.写入元素个数
                int length = obj.getClass().isArray() ? Array.getLength(obj) : ((obj instanceof Collection) ? ((Collection)obj).size() : ((Map)obj).size());
                out.write(NumberUtil.getByteArray(length));
                //3. 循环写入数组中的元素
                if(obj.getClass().isArray()){
                    for(int i = 0; i < length; i++){
                        Object value = Array.get(obj, i);
                        this.writeValue(value, obj.getClass().getComponentType(),out);
                    }
                }
            }else{
                //先写入该类类名及该类自定义的属性，然后写入父类名及父类定义的属性
                Class targetClass = obj.getClass();
                //判断是否是基本数据类型对应的包装类型
                if(ReflectUtil.isBaseType(targetClass)){
                    this.writeValue(obj, targetClass, out);
                }else if(targetClass.isEnum()){
                    this.writeValue(obj.toString(),String.class,out);
                }else{
                    //获得包含该类以及该类的所有父类的集合
                    List<Class> superClassAndSelfList = ReflectUtil.getSelfAndSuperClass(targetClass);
                    for(Class item : superClassAndSelfList){
                        Field[] fields = ReflectUtil.getAllInstanceField(item,isCacheField);
                        //2. 写入属性个数  2字节
                        out.write(NumberUtil.getByteArray( ((short)fields.length) ));
                        //3. 循环写入属性
                        for(Field field : fields){
                            context.setCurrentField(field);
                            this.writeField(field, obj,out);
                        }
                        context.setCurrentField(null);
                    }
                }
            }
            context.leave();
            if(context.isFinish()){
                threadLocal.remove();
                out.flush();
            }
        }
    }

    /**
     * 获得指定类的所有字段，将这些字段放入一个栈中。先压入该类自己定义的字段，然后依次压入上一级父类定义的字段
     * @param target
     * @return 包含所有字段的栈
     */
    private Stack<Field> getAllFields(Class targetClass){
        Stack<Field> stack = new Stack<Field>();
        while(targetClass != null){
            this.putField(targetClass, stack);
            targetClass = targetClass.getSuperclass();
        }

        return stack;
    }

    /**
     * 将param 指定类自身定义的所有字段(不包括父类的字段)压入栈中
     * @param param
     * @param stack
     */
    private void putField(Class targetClass,Stack stack){
        Field[] fields = targetClass.getDeclaredFields();
        for(Field item : fields){
            stack.push(item);
        }
    }

    /**
     * 将指定类的类名输出到流中
     * @param type
     * @param  out 输出流
     * @throws IOException
     */
    protected void writeClassName(Class type,OutputStream out) throws IOException{
        if(type == null){
            throw new IllegalArgumentException("type can't be null");
        }
        Context context = threadLocal.get();
        if(context == null || !context.contains(type)){
            String className = "";
            if(ReflectUtil.isBaseType(type)){
                className = ReflectUtil.getFlagOfBaseType(type);
            }else if(context.getCurrentField() != null && context.getCurrentField().getType() == type){  //当值的实际类型和字段类型相同时，不需要写入类名，写入标识字符即可
                className = BaseTypeEnum.VOID.getValue();
            }else{
                className = type.getTypeName();
            }
            //1. 写入类名长度  2字节
            out.write(NumberUtil.getByteArray( ((short)className.getBytes().length) ) );
            //2. 写入类名
            out.write(className.getBytes());
            context.addClass(type);
        }else{
            //长度0表示该类的类名之前已写入流中，这里写入的只是对该类名的引用序号
            out.write(NumberUtil.getByteArray((short)0));
            out.write(NumberUtil.getByteArray((short) context.getIndex(type)));
        }
    }

    /**
     * 将指定对象 指定的属性写入输出流
     * @param field
     * @param obj
     * @param out 输出流
     * @throws IOException
     */
    protected abstract void writeField(Field field,Object obj,OutputStream out) throws IOException;

    protected void writeStart(OutputStream out) throws IOException{
        out.write(Constant.BEGIN_FLAG);
    }

    protected void writeEnd(OutputStream out) throws IOException{
        out.write(Constant.END_FLAG);
    }

    protected void writeNull(OutputStream out) throws IOException{
        out.write(Constant.NULL_FLAG);
    }

    protected void writeNotNull(OutputStream out) throws IOException{
        out.write(Constant.NOT_NULL_FLAG);
    }

    protected void writeContine(OutputStream out) throws IOException{
        out.write(Constant.CONTINUE_FLAG);
    }

    /**
     * 写入引用标记
     * @throws IOException
     */
    protected void writeReference(OutputStream out) throws IOException{
        out.write(Constant.REFERENCE_FLAG);
    }

    /**
     * 写入普通标记
     * @throws IOException
     */
    protected void writeNormal(OutputStream out) throws IOException{
        out.write(Constant.NORMAL_FLAG);
    }

    //将一个short类型整数 转变成对应的字节数组
    private byte[] transfer(short num){
        byte[] result = new byte[2];
        result[0] = (byte)(num / 256);
        result[1] = this.transformNumToByte(num % 256);
        return result;
    }


    /**
     * 将一个小于256的整数 转变成对应的byte表示
     * @param num
     * @return
     */
    private byte transformNumToByte(int num){
        return (num > 127) ? (byte)(num - 256) : (byte)num;
    }


    /**
     * 写入基本数据类型对应包装类对象的值
     * @param value  要写入的值
     * @param  type 值所属字段的类型
     * @param out 输出流
     * @throws IOException
     */
    protected void writeValue(Object value, Class type,OutputStream out) throws IOException{
        if(value == null){
            this.writeNull(out);
            return;
        }
        this.writeNotNull(out);
        if(type == null){
            throw new IllegalArgumentException("when value is not null,type can't be null");
        }
        ObjectWrite objectWrite = writerMap.get(type);
        if(objectWrite != null){
            objectWrite.write(out,value);
        }else{
            Context context = threadLocal.get();
            //如果要写入的对象已经在当前序列化上下文中，则只需要写入其引用标识
            if(context != null && context.contains(value)){
                this.writeReference(out);
                this.writeClassName(value.getClass(),out);
                out.write(NumberUtil.getByteArray((short) context.getIndex(value)));
            }else{
                this.writeNormal(out);
                this.write(value,out);
            }
        }
    }

    private static interface  ObjectWrite<T>{
        /**
         * 将值写入指定流中
         * @param out
         * @param value
         */
        public void write(OutputStream out,T value) throws IOException;
    }

    private static class BooleanWrite implements ObjectWrite<Boolean> {
        public void write(OutputStream out,Boolean value) throws IOException{
            out.write( ((Boolean)value).equals(Boolean.TRUE) ? 1 : 0 );
        }
    }

    private static class ByteWrite implements ObjectWrite<Byte> {
        public void write(OutputStream out,Byte value) throws IOException{
            out.write(new byte[]{(Byte)value});
        }
    }

    private static class CharacterWrite implements ObjectWrite<Character> {
        public void write(OutputStream out,Character value) throws IOException{
            out.write(NumberUtil.getByteArray( ((Character)value).charValue() ));
        }
    }

    private static class ShortWrite implements ObjectWrite<Short> {
        public void write(OutputStream out,Short value) throws IOException{
            out.write(NumberUtil.getByteArray((Short)value));
        }
    }

    private static class IntegerWrite implements ObjectWrite<Integer> {
        public void write(OutputStream out,Integer value) throws IOException{
            out.write(NumberUtil.getByteArray((Integer)value));
        }
    }

    private static class LongWrite implements ObjectWrite<Long> {
        public void write(OutputStream out,Long value) throws IOException{
            out.write(NumberUtil.getByteArray((Long)value));
        }
    }

    private static class FloatWrite implements ObjectWrite<Float> {
        public void write(OutputStream out,Float value) throws IOException{
            out.write(NumberUtil.getByteArray((Float)value));
        }
    }

    private static class DoubleWrite implements ObjectWrite<Double> {
        public void write(OutputStream out,Double value) throws IOException{
            out.write(NumberUtil.getByteArray((Double)value));
        }
    }

    private static class StringWrite implements ObjectWrite<String> {
        public void write(OutputStream out,String value) throws IOException{
            //先写入字符串长度，再写入字符串对应的字节
            byte[] bytes = ((String)value).getBytes();
            out.write(NumberUtil.getByteArray( bytes.length ));
            out.write(bytes);
        }
    }
}
