package com.tuling.serialize.exception;

/**
 * Created by Administrator on 2020-06-16.
 */
public class BuilderNotFoundException extends RuntimeException{

    public BuilderNotFoundException(String msg){
        super(msg);
    }

    public BuilderNotFoundException(String msg,Throwable ex){
        super(msg,ex);
    }

    public BuilderNotFoundException(Class type){
        super("Can't instantiate " + type + " because there is no builder and constructor with empty argument for " + type);
    }
}
