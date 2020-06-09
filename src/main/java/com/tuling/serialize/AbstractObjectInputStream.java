package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;

import com.tuling.serialize.exception.InvalidDataFormatException;
import org.apache.log4j.Logger;
import com.tuling.serialize.util.ReflectUtil;
import com.tuling.serialize.util.NumberUtil;

/**
 * 该类主要实现了对象的反序列化
 * @author lujintao
 * @date 2017-04-16
 */
public abstract class AbstractObjectInputStream implements ObjectInputStream{

	private static final Logger LOGGER = Logger.getLogger(AbstractObjectInputStream.class);
	protected static final ThreadLocal<Context> threadLocal = new ThreadLocal();

	protected InputStream in;
	
	public AbstractObjectInputStream(InputStream in){
		this.in = in;
	}
	
	public Object readObject() throws IOException,ClassNotFoundException,InvalidDataFormatException {
		Object obj = null;
		
		if(this.start()){
			Context context = threadLocal.get();
			if(context == null){
				context = new Context();
				threadLocal.set(context);
			}
			context.enter();
			//1.读取序列化对象所对应的类名
			String className = this.readClassName();
			//2.判断是否是数组类型
			if(className.endsWith("[]")){
				//3.读取数组长度
				int length = this.readArrayLength();
				Object[] array = new Object[length];
				obj = Array.newInstance(Class.forName(className.substring(0,className.length() - 2)), length);
				context.put(obj);
				for(int i = 0;i < length;i++){
					Array.set(obj , i , this.readObject());
				}

			}else{
				try {
					Class objectClass = Class.forName(className);
					if(ReflectUtil.isBaseType(objectClass)){
						obj = readValue(objectClass);
					}else{
						try {
							//Constructor[] constructor = objectClass.getConstructors();

							obj = objectClass.newInstance();
							//将当前对象放入上下文中
							context.put(obj);
							Class currentType = objectClass;
							while(true){
								short fieldCount = this.readFieldCount();
								//循环读取属性
								for(int i = 0; i < fieldCount; i++){
									this.readField(obj,currentType);
								}
								this.in.mark(0);
								if(!this.end()){
									currentType = currentType.getSuperclass();
								}else{
									this.in.reset();
									break;
								}
							}
						} catch (InstantiationException e) {
							LOGGER.error(e.getCause(), e);
						} catch (IllegalAccessException e) {
							LOGGER.error(e.getCause(), e);
						}
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					LOGGER.error(className + "类不存在", e);
				}
			}
			context.leave();
			if(context.isFinish()){
				threadLocal.remove();
			}

			this.end();
		}
		return obj;
	}
	
	/**
	 * 关闭输入流
	 */
	public void close(){
		try {
			this.in.close();
		} catch (IOException e) {
			LOGGER.error(e.getCause(), e);
		}
	}
	
	
	
	/**
	 * 对象数据的读取开始
	 * 并判断当前位置是否为读取开始做好准备，如果这里写入的是NULL值，则该方法返回false;
	 * @throws IOException
	 */
	protected abstract boolean start() throws IOException;
	
	/**
	 * 判断对象数据的读取是否已经结束
	 * @throws IOException
	 */
	protected abstract boolean end() throws IOException;
	
	/**
	 * 读取属性总个数
	 * @throws IOException
	 */
	protected abstract short readFieldCount() throws IOException;
	
	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param type 属性定义所在的类
	 * @throws IOException  
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected abstract void readField(Object obj,Class type) throws IOException,ClassNotFoundException,InvalidDataFormatException;
	
	/**
	 * 从指定输入流中读取当前要反序列化的对象的类名
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	protected abstract String readClassName() throws IOException;
	
	/**
	 * 读取数组长度
	 * @return
	 * @throws IOException
	 */
	protected abstract int readArrayLength() throws IOException;

	/**
	 * 从流中当前位置读取指定类型的值
	 * @param type  要读取数据的类型
	 * @return
	 */
	protected abstract Object readValue(Class baseType) throws IOException,ClassNotFoundException,InvalidDataFormatException;
}
