package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;

import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
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
	//反序列化的时候，是否需要对对象属性进行排序，按序读入属性值
	private boolean needOrder;
	//是否缓存类的字段信息
	private boolean isCacheField = false;

	public AbstractObjectInputStream(InputStream in,boolean needOrder,boolean isCacheField){
		this.in = in;
		this.needOrder = needOrder;
		this.isCacheField = isCacheField;
	}
	
	public Object readObject() throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException {
		Object obj = null;
		
		if(!this.isNull()){
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

			}else
			{
				try {
					Class objectClass = ReflectUtil.get(className);
					if(ReflectUtil.isBaseType(objectClass)){
						if(!isNull()){
							obj = readValue(objectClass);
						}
					}else{
						try {
							obj = objectClass.newInstance();
							//将当前对象放入上下文中
							context.put(obj);
							if(Collection.class.isAssignableFrom(objectClass)){
								int size = this.readCollectionSize();
								for(int i = 0; i < size; i++){
									Object value = this.readObject();
									((Collection)obj).add(value);
								}
							}else if(Map.class.isAssignableFrom(objectClass)){
								int size = this.readMapSize();
								for(int i = 0; i < size; i++){
									Object key = this.readObject();
									Object value = this.readObject();
									((Map)obj).put(key,value);
								}
							}else{
								Class currentType = objectClass;
								while(true){
									Field[] fields = ReflectUtil.getAllInstanceField(currentType, needOrder,isCacheField);
									short fieldCount = this.readFieldCount();
									if(fieldCount != fields.length){
										throw new ClassNotSameException("属性个数不一致");
									}
									//循环读取属性
									for(int i = 0; i < fieldCount; i++){
										this.readField(obj,currentType,fields[i]);
									}
//									this.in.mark(0);
//									if(!this.end()){
//										currentType = currentType.getSuperclass();
//									}else{
//										this.in.reset();
//										break;
//									}
									currentType = currentType.getSuperclass();
									if(currentType == null || currentType == Object.class){
										break;
									}
								}
							}



						} catch (InstantiationException e) {
							LOGGER.error(e.getCause(), e);
							throw new InvalidDataFormatException("创建对象失败:" + e.getMessage(), e);
						} catch (IllegalAccessException e) {
							LOGGER.error(e.getCause(), e);
							throw new InvalidDataFormatException("访问对象成员受限:" + e.getMessage(), e);
						}
					}
				} catch (ClassNotFoundException e) {
					LOGGER.error(className + "类不存在", e);
					throw e;
				}
			}
			context.leave();
			if(context.isFinish()){
				threadLocal.remove();
			}

//			this.end();
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
	 * 判断当前要读取的值是否为空
	 * @throws IOException
	 */
	protected abstract boolean isNull() throws IOException;
	
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
	protected abstract void readField(Object obj,Class type) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException;

	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param type 属性定义所在的类
	 * @param field 当前在读取的字段
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected abstract void readField(Object obj,Class type,Field field) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException;

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
	 * 读取集合元素个数
	 * @return
	 * @throws IOException
	 */
	protected abstract int readCollectionSize() throws IOException;

	/**
	 * 读取Map元素个数
	 * @return
	 * @throws IOException
	 */
	protected abstract int readMapSize() throws IOException;

	/**
	 * 从流中当前位置读取指定类型的值
	 * @param type  要读取数据的类型
	 * @return
	 */
	protected abstract Object readValue(Class baseType) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException;
}
