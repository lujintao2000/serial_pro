package com.tuling.serialize.util;

/**
 * Created by ljt on 2020-11-18.
 */
public class Assert {

    /**
     * 判断指定引用是否为空
     * @param obj 需要判断的引用
     */
    public static void notNull(Object obj){
        if(obj == null){
            throw new IllegalArgumentException("The value of argument can't not be null");
        }
    }
}
