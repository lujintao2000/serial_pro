package com.tuling.builder;

import com.tuling.domain.User;
import com.tuling.serialize.Builder;

import java.util.Collections;

/**
 * @author  ljt
 * @date  2020-06-20
 */
public class UserBuilder implements Builder{

    public Object newInstance(){
        return new User("Tomxun.Maike.Jekxun,Lilei");
    }

    public Class getType(){
        return User.class;
    }
}
