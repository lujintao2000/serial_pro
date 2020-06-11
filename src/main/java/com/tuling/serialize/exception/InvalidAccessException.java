package com.tuling.serialize.exception;

/**
 * @author lujintao
 * @date  2020-06-11
 * 该异常表示由于对某个方法的不合法调用引起的异常，如类的构造方法不是public的或没有无参构造方法，这个时候调用class.newInstance()就会抛出该异常。
 * 还要就是一个Field由于自身安全原因，不允许外界通过反射改变其不可被外访问性，这种情况下调用Field.setAccessible(true)也会抛出该异常
 */
public class InvalidAccessException extends Exception{
    public InvalidAccessException(Exception ex){
        super(ex);
    }

    public InvalidAccessException(String msg, Exception ex){
        super(msg,ex);
    }

}
