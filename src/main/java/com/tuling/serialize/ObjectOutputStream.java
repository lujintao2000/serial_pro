package com.tuling.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.sql.Ref;
import java.util.*;
import com.tuling.serialize.util.ReflectUtil;
import com.tuling.serialize.util.NumberUtil;
import org.apache.log4j.Logger;
import org.msgpack.io.Output;

public interface ObjectOutputStream {

	/**
	 * 序列化某个对象,序列化的时候会写入对象的类名
	 * @param obj 要序列化的对象
	 * @param out 写出序列化对象的输出流
	 * @throws IOException
	 */
	public void write(Object obj,OutputStream out) throws IOException;

	/**
	 * 序列化某个对象,序列化的时候是否写入类名由isWriteClassName决定
	 * @param obj 要序列化的对象
	 * @param isWriteClassName  序列化时是否写入对象所属类的类名
	 * @param out 写出序列化对象的输出流
	 * @throws IOException
	 */
	public void write(Object obj, boolean isWriteClassName, OutputStream out) throws IOException;

}
