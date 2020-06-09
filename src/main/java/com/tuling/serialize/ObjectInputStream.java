package com.tuling.serialize;

import com.tuling.serialize.exception.InvalidDataFormatException;
import java.io.IOException;


/**
 * 该接口用于对象的反序列化，定义了一个从输入流中读取对象的方法
 * @author Administrator
 *
 */
public interface ObjectInputStream {

	/**
	 * @return 反序列化出的对象
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	public Object readObject() throws IOException,ClassNotFoundException,InvalidDataFormatException;
	
	/**
	 * 关闭输入流
	 */
	public void close();
}
