package com.tuling.builder;

import com.tuling.domain.AboardDepartment;
import com.tuling.serialize.AbstractObjectInputStream;
import com.tuling.serialize.Builder;
import com.tuling.serialize.util.ReflectUtil;
import org.apache.log4j.Logger;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2020-06-16.
 */
public class ArrayListBuilder implements Builder<List> {
    private static final Logger LOGGER = Logger.getLogger(AbstractObjectInputStream.class);

    public List create(Map<String,Object> paramMap){
        if(paramMap != null && paramMap.size() > 0){
            List list = (List)paramMap.get(Builder.LIST);
            if(list != null){
                return Arrays.asList(list.toArray());
            }
        }
        return null;
    }

    /**
     * 获得要创建的对象的类型
     * @return
     */
    public Class getType(){
        try{
            return ReflectUtil.get("java.util.Arrays$ArrayList");
        }catch (ClassNotFoundException ex){
            LOGGER.error("class java.util.Arrays$ArrayList not found",ex);
            return null;
        }
    }
}
