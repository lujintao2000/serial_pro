package com.tuling.serialize;

import com.sun.rowset.WebRowSetImpl;
import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import com.tuling.serialize.util.SituationEnum;


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
     * @param context 序列化上下文
     * @throws IOException
     */
    @Override
    protected  void writeField(Field field, Object obj, ByteBuf out, Context context){
        try {
            Object value = field.get(obj);
            //写入属性值
            this.writeValue(value,field.getType(),out,context, SituationEnum.POSSIBLE_BE);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
