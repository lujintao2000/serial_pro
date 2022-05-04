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

    @Override
    protected Object readValue(Object obj, List<Field> fields, Context context, ByteBuf in) throws IOException, ClassNotSameException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, BuilderNotFoundException {

        //存放字段的值，key为字段名称
        List<Class> selfAndSuperList = ReflectUtil.getSelfAndSuperClass(obj.getClass());
        int count = 0;
        for (Class currentType : selfAndSuperList) {
            short fieldCount = this.readLengthOrIndex(in);
            //循环读取属性
            for (int i = 0; i < fieldCount; i++) {
                String fieldName = this.readString(in);
                //读取该字段值的长度
                int length = this.readInt(in);
                //根据属性名，有可能找不到对应的属性，因为两个类的版本不一样
                Field field = ReflectUtil.getField(currentType, fieldName);
                if (field != null) {
                    context.setCurrentField(field);
                    this.readField(obj, obj.getClass(), field, in, context);
                } else {
                    in.skip(length);
                }
            }

        }
        context.setCurrentField(null);
        return obj;
    }
}
