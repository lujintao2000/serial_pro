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

public interface ObjectOutputStream {

	/**
	 * 序列化某个对象
	 * @param obj
	 * @throws IOException
	 */
	public void write(Object obj) throws IOException;

	/**
	 * 关闭输入流
	 */
	public void close();
}
