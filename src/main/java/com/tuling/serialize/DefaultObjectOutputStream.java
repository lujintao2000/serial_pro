package com.tuling.serialize;

import com.sun.rowset.WebRowSetImpl;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author lujintao
 * @date 2020-06-12
 */
public class DefaultObjectOutputStream extends AbstractOutputStream{
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

    public DefaultObjectOutputStream(){

    }

    public DefaultObjectOutputStream(boolean needOrder, boolean isCacheField){
        super(needOrder,isCacheField);
    }

    /**
     * 将指定对象 指定的属性写入输出流
     * @param field
     * @param obj
     * @param out 输出流
     * @throws IOException
     */
    @Override
    protected  void writeField(Field field,Object obj,OutputStream out) throws IOException{
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            //3. 写入属性值
            this.writeValue(value, field.getType(),out);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    /**
     * 写入基本数据类型对应包装类对象的值
     * @param value  要写入的值
     * @param  type 值所属字段的类型
     * @param out 输出流
     * @throws IOException
     */
    @Override
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
//        if(type == boolean.class || type == Boolean.class){
//            this.out.write( ((Boolean)value).equals(Boolean.TRUE) ? 1 : 0 );
//        }else if(type == char.class || type == Character.class){
//            this.out.write(NumberUtil.getByteArray( ((Character)value).charValue() ));
//        }else if(type == byte.class || type == Byte.class){
//            this.out.write(new byte[]{(Byte)value});
//        }else if(type == short.class || type == Short.class){
//            this.out.write(NumberUtil.getByteArray((Short)value));
//        }else if(type == int.class || type == Integer.class){
//            this.out.write(NumberUtil.getByteArray((Integer)value));
//        }else if(type == long.class || type == Long.class){
//            this.out.write(NumberUtil.getByteArray((Long)value));
//        }else if(type == float.class || type == Float.class){
//            this.out.write(NumberUtil.getByteArray((Float)value));
//        }else if(type == double.class || type == Double.class){
//            this.out.write(NumberUtil.getByteArray((Double)value));
//        }else if(type == String.class){
//            //先写入字符串长度，再写入字符串对应的字节
//            byte[] bytes = ((String)value).getBytes();
//            this.out.write(NumberUtil.getByteArray( bytes.length ));
//            this.out.write(bytes);
//        }else{
//            Context context = threadLocal.get();
//            //如果要写入的对象已经在当前序列化上下文中，则只需要写入其引用标识
//            if(context != null && context.contains(value)){
//                this.writeReference();
//                this.writeClassName(value.getClass());
//                this.out.write(NumberUtil.getByteArray((short) context.getIndex(value)));
//            }else{
//                this.writeNormal();
//                this.write(value);
//            }
//        }
    }

    @Override
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


    private static interface  ObjectWrite<T>{
        /**
         * 将值写入指定流中
         * @param out
         * @param value
         */
        public void write(OutputStream out,T value) throws IOException;
    }

    private static class BooleanWrite implements  ObjectWrite<Boolean>{
        public void write(OutputStream out,Boolean value) throws IOException{
            out.write( ((Boolean)value).equals(Boolean.TRUE) ? 1 : 0 );
        }
    }

    private static class ByteWrite implements  ObjectWrite<Byte>{
        public void write(OutputStream out,Byte value) throws IOException{
            out.write(new byte[]{(Byte)value});
        }
    }

    private static class CharacterWrite implements  ObjectWrite<Character>{
        public void write(OutputStream out,Character value) throws IOException{
            out.write(NumberUtil.getByteArray( ((Character)value).charValue() ));
        }
    }

    private static class ShortWrite implements  ObjectWrite<Short>{
        public void write(OutputStream out,Short value) throws IOException{
            out.write(NumberUtil.getByteArray((Short)value));
        }
    }

    private static class IntegerWrite implements  ObjectWrite<Integer>{
        public void write(OutputStream out,Integer value) throws IOException{
            out.write(NumberUtil.getByteArray((Integer)value));
        }
    }

    private static class LongWrite implements  ObjectWrite<Long>{
        public void write(OutputStream out,Long value) throws IOException{
            out.write(NumberUtil.getByteArray((Long)value));
        }
    }

    private static class FloatWrite implements  ObjectWrite<Float>{
        public void write(OutputStream out,Float value) throws IOException{
            out.write(NumberUtil.getByteArray((Float)value));
        }
    }

    private static class DoubleWrite implements  ObjectWrite<Double>{
        public void write(OutputStream out,Double value) throws IOException{
            out.write(NumberUtil.getByteArray((Double)value));
        }
    }

    private static class StringWrite implements  ObjectWrite<String>{
        public void write(OutputStream out,String value) throws IOException{
            //先写入字符串长度，再写入字符串对应的字节
            byte[] bytes = ((String)value).getBytes();
            out.write(NumberUtil.getByteArray( bytes.length ));
            out.write(bytes);
        }
    }
}
