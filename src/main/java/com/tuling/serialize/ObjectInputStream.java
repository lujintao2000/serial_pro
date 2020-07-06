package com.tuling.serialize;

import com.tuling.serialize.exception.BuilderNotFoundException;
import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.exception.InvalidDataFormatException;
import java.io.IOException;
import java.io.InputStream;


/**
 * 该接口用于对象的反序列化，定义了一个从输入流中读取对象的方法
 * @author Administrator
 *
 */
public interface ObjectInputStream {

/**
	 * @return 反序列化出的对象
     * @param in 包含序列化数据的输入流
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 * @throws InvalidAccessException  如果方法或字段不让访问或方法传递参数不对，抛出该异常
	 * @throws ClassNotSameException  当反序列化时加载的类的属性与序列化时类的属性不一致时，抛出此异常
	 */
	public Object readObject(InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException , ClassNotSameException,BuilderNotFoundException;

	/**
	 * @param type 需要反序列化对象的类型
	 * @param in 包含序列化数据的输入流
	 * @return 反序列化出的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 * @throws InvalidAccessException  如果方法或字段不让访问或方法传递参数不对，抛出该异常
	 * @throws ClassNotSameException  当反序列化时加载的类的属性与序列化时类的属性不一致时，抛出此异常
	 */
	public Object readObject(Class type,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException , ClassNotSameException,BuilderNotFoundException;

	/**
	 * 将指定的字节数组反序列化为对象
	 * @param value
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException
	 * @throws InvalidAccessException
	 * @throws ClassNotSameException
	 * @throws BuilderNotFoundException
	 */
	public Object readObject(byte[] value) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException , ClassNotSameException,BuilderNotFoundException;

	/**
	 * 将指定的字节数组反序列化为对象
	 * @param value 包含序列化数据的字节数组
	 * @param type  反序列化的类型
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException
	 * @throws InvalidAccessException
	 * @throws ClassNotSameException
	 * @throws BuilderNotFoundException
	 */
	public Object readObject(byte[] value,Class type) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException , ClassNotSameException,BuilderNotFoundException;
}
