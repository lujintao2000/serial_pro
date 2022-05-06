package com.tuling.serialize;


import com.tuling.serialize.exception.BuilderNotFoundException;
import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.ReflectUtil;
import com.tuling.serialize.util.SituationEnum;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该实现在反序列化的时候，会根据读取的字段名称，进行相应的反射设值；如果读取的字段名称在反序列化类中不存在，则忽略该字段。
 * 它只对反序列化类中存在的字段进行设值。它不要求反序列化类和序列化类是一致的，它们可以不同。
 *
 * @author ljt
 * @date 2020-06-22
 */
public class CompatibleObjectInputStream extends AbstractObjectInputStream {
    private static final Logger LOGGER = Logger.getLogger(CompatibleObjectInputStream.class);


    public CompatibleObjectInputStream() {

    }

    public CompatibleObjectInputStream(boolean isCacheField) {
        super(isCacheField);
    }

    /**
     * 读取值，将值存入指定的对象中,如果obj为空,则将读取的值存入Map
     * @param obj    需要读入值的对象
     * @param context 序列化上下文
     * @param in  包含序列化数据的输入流
     * @return  存储了读取值的对象
     */
    @Override
    protected Object readValue(Object obj, Context context, ByteBuf in) throws IOException, ClassNotSameException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, BuilderNotFoundException {
        List<List<Field>> allFields = ReflectUtil.getAllFields(obj.getClass());
        for(int i = 0 ; i < allFields.size(); i++){
            short fieldCount = this.readLengthOrIndex(in);
            List<Field> fields = allFields.get(i);
            for(int j = 0; j < fieldCount; j++){
                Field field = fields.get(j);
                if (field != null) {
                    context.setCurrentField(field);
                    try {
                        field.set(obj,this.readValue(field.getType(),in,context, SituationEnum.POSSIBLE_BE) );

                    } catch (IllegalArgumentException e) {
                        LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
                        throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
                    } catch (IllegalAccessException e) {
                        LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
                        throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
                    }
                }else{
                    System.out.println("a");
                }
            }

        }
        context.setCurrentField(null);
        return obj;
    }
}
