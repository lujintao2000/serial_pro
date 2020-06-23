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
 * 该实现是序列化的默认实现，它在写入属性的时候，只写入值，不写入属性名称。属性写入的先后与属性按名称排序后的先后一致。
 * 此种实现适用于客户端/服务器端实体类属性一致的情况，优点是码率小，序列化数据占用空间小；如果不一致(不包含顺序不一致)，请参考CompatibleObjectOutputStream。
 * @author lujintao
 * @date 2020-06-12
 */
public class DefaultObjectOutputStream extends AbstractOutputStream{


    public DefaultObjectOutputStream(){
        this(true);
    }

    public DefaultObjectOutputStream( boolean isCacheField){
        super(isCacheField);
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



//    private static interface  ObjectWrite<T>{
//        /**
//         * 将值写入指定流中
//         * @param out
//         * @param value
//         */
//        public void write(OutputStream out,T value) throws IOException;
//    }
//
//    private static class BooleanWrite implements  ObjectWrite<Boolean>{
//        public void write(OutputStream out,Boolean value) throws IOException{
//            out.write( ((Boolean)value).equals(Boolean.TRUE) ? 1 : 0 );
//        }
//    }
//
//    private static class ByteWrite implements  ObjectWrite<Byte>{
//        public void write(OutputStream out,Byte value) throws IOException{
//            out.write(new byte[]{(Byte)value});
//        }
//    }
//
//    private static class CharacterWrite implements  ObjectWrite<Character>{
//        public void write(OutputStream out,Character value) throws IOException{
//            out.write(NumberUtil.getByteArray( ((Character)value).charValue() ));
//        }
//    }
//
//    private static class ShortWrite implements  ObjectWrite<Short>{
//        public void write(OutputStream out,Short value) throws IOException{
//            out.write(NumberUtil.getByteArray((Short)value));
//        }
//    }
//
//    private static class IntegerWrite implements  ObjectWrite<Integer>{
//        public void write(OutputStream out,Integer value) throws IOException{
//            out.write(NumberUtil.getByteArray((Integer)value));
//        }
//    }
//
//    private static class LongWrite implements  ObjectWrite<Long>{
//        public void write(OutputStream out,Long value) throws IOException{
//            out.write(NumberUtil.getByteArray((Long)value));
//        }
//    }
//
//    private static class FloatWrite implements  ObjectWrite<Float>{
//        public void write(OutputStream out,Float value) throws IOException{
//            out.write(NumberUtil.getByteArray((Float)value));
//        }
//    }
//
//    private static class DoubleWrite implements  ObjectWrite<Double>{
//        public void write(OutputStream out,Double value) throws IOException{
//            out.write(NumberUtil.getByteArray((Double)value));
//        }
//    }
//
//    private static class StringWrite implements  ObjectWrite<String>{
//        public void write(OutputStream out,String value) throws IOException{
//            //先写入字符串长度，再写入字符串对应的字节
//            byte[] bytes = ((String)value).getBytes();
//            out.write(NumberUtil.getByteArray( bytes.length ));
//            out.write(bytes);
//        }
//    }
}
