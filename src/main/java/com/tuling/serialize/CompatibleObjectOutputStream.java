package com.tuling.serialize;

import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.util.*;
import java.lang.reflect.Field;
import java.util.List;

/**
 * 该实现会在序列化的时候写入属性名称,因此码率较大，适用于客户端/服务器端实体类属性不一致的情况。
 * 如果能够确保客户端/服务器端的实体类属性一致，用DefaultObjectOutputStream进行序列化会更好
 *
 * @author lujintao
 * @date 2020-06-22
 */
public class CompatibleObjectOutputStream extends AbstractOutputStream {
    //临时缓冲大小
    private static final int BUFFER_SIZE = 256;

    public CompatibleObjectOutputStream() {
    }

    public CompatibleObjectOutputStream(boolean isCacheField) {
        super(isCacheField);
    }

    @Override
    protected void writeAllFields(Object obj, Class type, Context context, ByteBuf out) {
        List<List<Field>> allFields = ReflectUtil.getAllFields(obj.getClass());
        for(int i = 0 ; i < allFields.size(); i++){
            List<Field> fields = allFields.get(i);
            //写入属性个数  2字节
            writeLengthOrIndex(fields.size(), out);
            for(int j = 0; j < fields.size(); j++){
                Field field = fields.get(j);
                context.setCurrentField(field);
                this.writeField(field, obj, out, context);
            }

        }
        context.setCurrentField(null);

    }

        @Override
        protected void writeField (Field field, Object obj, ByteBuf buf, Context context){
            try {
                Object value = field.get(obj);
                this.writeValue(value, field.getType(), buf, context, SituationEnum.POSSIBLE_BE);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

    }
