package com.tuling.serialize;

import com.tuling.serialize.util.NumberUtil;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * 该实现会在序列化的时候写入属性名称,因此码率较大，适用于客户端/服务器端实体类属性不一致的情况。
 * 如果能够确保客户端/服务器端的实体类属性一致，用DefaultObjectOutputStream进行序列化会更好
 * @author lujintao
 * @date 2020-06-22
 */
public class CompatibleObjectOutputStream extends AbstractOutputStream {
    //临时缓冲大小
    private static final int BUFFER_SIZE = 256;

    public CompatibleObjectOutputStream(){
    }

    public CompatibleObjectOutputStream( boolean isCacheField){
        super(isCacheField);
    }

    @Override
    protected void writeField(Field field, Object obj, OutputStream out) throws IOException {
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            //1. 写入属性名
            byte[] bytes = field.getName().getBytes();
            out.write(NumberUtil.getByteArray( bytes.length ));
            out.write(bytes);
            //先将值写入一个临时流中
            ByteArrayOutputStream output = new ByteArrayOutputStream(BUFFER_SIZE);
            this.writeValue(value, field.getType(),output);
            //2. 写入属性值的字节长度
            out.write(NumberUtil.getByteArray( output.size()));
            //3. 写入属性值
            output.writeTo(out);
            output.close();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
