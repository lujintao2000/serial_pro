package com.tuling.serialize;

import java.io.IOException;
import java.io.OutputStream;

public interface ObjectOutputStream {

	/**
	 * 序列化某个对象,序列化的时候会写入对象的类名
	 * @param obj 要序列化的对象
	 * @param out 写出序列化对象的输出流
	 * @throws IOException
	 */
	public void write(Object obj,OutputStream out) throws IOException;

	/**
	 * 序列化某个对象,序列化的时候会写入对象的类名
	 * @param obj 要序列化的对象
	 * @param out 写出序列化对象的输出流
	 * @param size  指定存储对象数据的缓冲数组的初始容量
	 * @throws IOException
	 */
	public void write(Object obj,OutputStream out,int size) throws IOException;

	/**
	 * 序列化某个对象,序列化的时候是否写入类名由isWriteClassName决定
	 * @param obj 要序列化的对象
	 * @param isWriteClassName  序列化时是否写入对象所属类的类名
	 * @param out 写出序列化对象的输出流
	 * @throws IOException
	 */
	public void write(Object obj, boolean isWriteClassName, OutputStream out) throws IOException;

	/**
	 * 序列化某个对象
	 *
	 * @param obj              要序列化的对象
	 * @param isWriteClassName 序列化时是否写入对象所属类的类名
	 * @param out 写出序列化对象的输出流
	 * @param size  指定存储对象数据的缓冲数组的初始容量
	 * @throws IOException
	 */

	public void write(Object obj, boolean isWriteClassName, OutputStream out,int size) throws IOException;

	/**
	 * 获得指定对象对应的序列化字节
	 * @param obj 要获取对应字节数组的对象
	 * @return
	 */
	public byte[] getBytes(Object obj);

	/**
	 * 获得指定对象对应的序列化字节
	 * @param obj 要获取对应字节数组的对象
	 * @param isWriteClassName  序列化时是否写入对象所属类的类名
	 * @return
	 */
	public byte[] getBytes(Object obj,boolean isWriteClassName);

}
