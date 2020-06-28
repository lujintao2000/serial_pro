package com.tuling.serialize;


import com.tuling.serialize.exception.BuilderNotFoundException;
import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.ReflectUtil;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该实现在反序列化的时候，会根据读取的字段名称，进行相应的反射设值；如果读取的字段名称在反序列化类中不存在，则忽略该字段。
 * 它只对反序列化类中存在的字段进行设值。它不要求反序列化类和序列化类是一致的，它们可以不同。
 * @author ljt
 * @date 2020-06-22
 */
public class CompatibleObjectInputStream extends AbstractObjectInputStream {

    public CompatibleObjectInputStream(){

    }

    public CompatibleObjectInputStream( boolean isCacheField){
        super(isCacheField);
    }

    @Override
    protected Object readValue(Object obj, Class objectClass, Context context, ByteBuf in) throws IOException, ClassNotSameException, ClassNotFoundException, InvalidDataFormatException, InvalidAccessException, BuilderNotFoundException {
        //存放字段的值，key为字段名称
        Map<String,Object> valueMap = new HashMap<>();
        Map<String,Object> currentMap = valueMap;
        List<Class> selfAndSuperList = ReflectUtil.getSelfAndSuperClass(objectClass);
        int count = 0;
        for(Class currentType : selfAndSuperList){
                short fieldCount = this.readFieldCount(in);
                    //循环读取属性
                    if(obj == null){
                        for(int i = 0; i < fieldCount; i++){
                            String fieldName = this.readString(in);
                            //读取该字段值的长度
                            int length = this.readInt(in);
                            Field field = ReflectUtil.getField(currentType,fieldName);
                            if(field != null){
                                context.setCurrentField(field);
                                this.readField(currentMap, field,in,context);
                            }
                        }
                        count++;
                        if(count < selfAndSuperList.size()){
                            Map<String,Object> tempMap = currentMap;
                            currentMap = new HashMap<String,Object>();
                            tempMap.put(com.tuling.serialize.Builder.NEXT,currentMap);
                        }
                    }else{
                        for(int i = 0; i < fieldCount; i++){
                            String fieldName = this.readString(in);
                            //读取该字段值的长度
                            int length = this.readInt(in);
                            Field field = ReflectUtil.getField(currentType,fieldName);
                            if(field != null) {
                                context.setCurrentField(field);
                                this.readField(obj, objectClass, field, in,context);
                            }else{
                                in.skip(length);
                                in.readerIndex(in.readerIndex() + length);
                            }
                        }
                    }

        }
        context.setCurrentField(null);
        if(obj == null){
            return valueMap;
        }else{
            return obj;
        }
    }

}
