package com.tuling.serialize;

import com.tuling.serialize.util.NumberUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;

/**
 * @author lujintao
 * @date 2020-06-22
 */
public class CompatibleObjectOutputStream extends AbstractOutputStream {

    public CompatibleObjectOutputStream(){
    }

    public CompatibleObjectOutputStream(boolean needOrder, boolean isCacheField){
        super(needOrder,isCacheField);
    }

    @Override
    protected void writeField(Field field, Object obj, OutputStream out) throws IOException {
        field.setAccessible(true);
        try {
            Object value = field.get(obj);
            //写入属性名
            byte[] bytes = field.getName().getBytes();
            out.write(NumberUtil.getByteArray( bytes.length ));
            out.write(bytes);
            //3. 写入属性值
            this.writeValue(value, field.getType(),out);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

}
