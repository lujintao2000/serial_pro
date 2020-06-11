package com.tuling.serialize.exception;

/**
 * 该异常表示非法的数据格式;如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
 * @author lujintao
 * @date 2017-04-20
 *
 */
public class InvalidDataFormatException extends Exception{

	public InvalidDataFormatException(Exception ex){
		super(ex);
	}

	public InvalidDataFormatException(String msg, Exception ex){
		super(msg,ex);
	}
}
