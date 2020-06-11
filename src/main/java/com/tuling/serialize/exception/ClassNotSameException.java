package com.tuling.serialize.exception;

/**
 * @author lujintao
 * @date 2020-06-11
 * 当反序列化加载的类和序列化用到的类的属性不相同时，抛出该异常
 */
public class ClassNotSameException extends Exception{

    public ClassNotSameException(String msg){
        super(msg);
    }

    public ClassNotSameException(Exception ex){
        super(ex);
    }

    public ClassNotSameException(String msg, Exception ex){
        super(msg,ex);
    }
}
