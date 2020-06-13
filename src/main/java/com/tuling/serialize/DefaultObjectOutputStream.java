package com.tuling.serialize;

import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * @author lujintao
 * @date 2020-06-12
 */
public class DefaultObjectOutputStream extends AbstractOutputStream{


    public DefaultObjectOutputStream(OutputStream output){
        super(output);
    }

    public DefaultObjectOutputStream(OutputStream output, boolean needOrder, boolean isCacheField){
        super(output,needOrder,isCacheField);
    }

    /**
     * 将指定对象 指定的属性写入输出流
     * @param field
     * @param obj
     * @throws IOException
     */
    @Override
    protected void writeField(Field field, Object obj) throws IOException{
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            //3. 写入属性值
            this.writeValue(value, field.getType());
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
     * @throws IOException
     */
    @Override
    protected void writeValue(Object value, Class type) throws IOException{
        if(value == null){
            this.writeNull();
            return;
        }
        this.writeNotNull();

        if(value instanceof Boolean){
            this.out.write( ((Boolean)value).equals(Boolean.TRUE) ? 1 : 0 );
        }else if(value instanceof Character){
            this.out.write(NumberUtil.getByteArray( ((Character)value).charValue() ));
        }else if(value instanceof Byte){
            this.out.write(new byte[]{(Byte)value});
        }else if(value instanceof Short){
            this.out.write(NumberUtil.getByteArray((Short)value));
        }else if(value instanceof Integer){
            this.out.write(NumberUtil.getByteArray((Integer)value));
        }else if(value instanceof Long){
            this.out.write(NumberUtil.getByteArray((Long)value));
        }else if(value instanceof Float){
            this.out.write(NumberUtil.getByteArray((Float)value));
        }else if(value instanceof Double){
            this.out.write(NumberUtil.getByteArray((Double)value));
        }else if(value instanceof String){
            //先写入字符串长度，再写入字符串对应的字节
            byte[] bytes = ((String)value).getBytes();
            this.out.write(NumberUtil.getByteArray( bytes.length ));
            this.out.write(bytes);
        }else{
            Context context = threadLocal.get();
            //如果要写入的对象已经在当前序列化上下文中，则只需要写入其引用标识
            if(context != null && context.contains(value)){
                this.writeReference();
                this.out.write(NumberUtil.getByteArray(context.getIndex(value)));
            }else{
                this.write(value);
            }
        }
    }
}
