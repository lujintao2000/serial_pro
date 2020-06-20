package com.tuling.builder;

import com.tuling.domain.Department;
import com.tuling.serialize.Builder;

import java.util.Map;

/**
 * 该类定义了根据参数创建Department对象的细节
 * @author  lujintao
 * @date    2020-06-16
 */
public class DepartmentBuilder implements Builder<Department> {

    /**
     * 根据参数集合创建特定类型的对象
     * @param paramMap  该map 中的key为类的属性名，value为属性对应的值；如果该类有父类，则父类相关的属性存放在key为next的map中
     *                    如果父类还有父类，以此类推
     * @return
     */
    public Department create(Map<String,Object> paramMap){
        Department department = null;
        if(paramMap != null && paramMap.size() > 0){
            String name = (String)paramMap.get("name");
            if(name != null){
                department = new Department(name);
            }else{
                throw new IllegalArgumentException("paramMap can't contain value of name");
            }
        }
        return department;
    }

    /**
     * 获得要创建的对象的类型
     * @return
     */
    public Class getType(){
        return Department.class;
    }
}
