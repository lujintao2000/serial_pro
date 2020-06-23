package com.tuling.serialize.exception;

/**
 * 该异常表示在序列化或反序列化的过程中出现了错误
 * @author  lujintao
 * @date 2020-06-23
 */
public class SerializationException extends Exception{

    public SerializationException(Throwable cause){
        super(cause);
    }

    public SerializationException(String msg,Throwable cause){
        super(msg,cause);
    }
}
