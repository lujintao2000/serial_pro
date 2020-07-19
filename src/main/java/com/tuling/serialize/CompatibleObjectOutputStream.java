package com.tuling.serialize;

import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.Constant;
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
    protected void writeField(Field field, Object obj, ByteBuf buf, Context context){
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            //1. 写入属性名
            writeString(field.getName(),buf);
            //先将值写入一个临时缓冲
            ByteBuf tempBuf = new ByteBuf(Constant.DEFAULT_BUFFER_SIZE_OF_FIELD);
            this.writeValue(value,field.getType(), tempBuf,context);
            //2. 写入属性值的字节长度
            buf.writeInt(tempBuf.readableBytes());
            //3. 写入属性值
            buf.writeBytes(tempBuf);
            tempBuf.release();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
