package com.tuling.builder;

import com.tuling.domain.User;
import com.tuling.serialize.Builder;
import java.util.Map;

/**
 * @author  ljt
 * @date  2020-06-20
 */
public class UserBuilder implements Builder<User>{

    /**
     * 根据参数集合创建特定类型的对象
     * @param paramMap  该map 中的key为类的属性名，value为属性对应的值；如果该类有父类，则父类相关的属性存放在key为next的map中
     *                    如果父类还有父类，以此类推
     * @return
     */
    public User create(Map<String,Object> paramMap){
        return new User("");
    }


    /**
     * 获得要创建的对象的类型
     * @return
     */
    public Class getType(){
        return User.class;
    }
}
