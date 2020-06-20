package com.tuling.builder;

import com.tuling.domain.AboardDepartment;
import com.tuling.domain.Department;
import com.tuling.serialize.Builder;
import com.tuling.serialize.util.Constant;

import java.util.Map;

/**
 * @author ljt
 * @date 2020-06-16
 */
public class AboardDepartmentBuilder implements Builder<AboardDepartment> {

    public AboardDepartment create(Map<String,Object> paramMap){
//        if(paramMap != null && paramMap.size() > 0){
//            if(paramMap.containsKey(Builder.NEXT)){
//                Map<String,Object> map = (Map<String,Object>)paramMap.get(Builder.NEXT);
//                return new AboardDepartment((String)map.get("name"));
//            }
//        }
//        return null;
        return new AboardDepartment("");
    }

    /**
     * 获得要创建的对象的类型
     * @return
     */
    public Class getType(){
        return AboardDepartment.class;
    }
}
