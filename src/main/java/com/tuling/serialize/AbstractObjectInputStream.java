package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

import com.sun.corba.se.impl.ior.OldJIDLObjectKeyTemplate;
import com.tuling.serialize.exception.BuilderNotFoundException;
import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.BuilderUtil;
import com.tuling.serialize.util.Constant;
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
	
	public Object readObject() throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException {
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
				obj = readArray(context,className);
			}else
			{
				try {
//					Class objectClass = Class.forName(className);
					Class objectClass = ReflectUtil.getComplexClass(className);
					if(ReflectUtil.isBaseType(objectClass)){
							obj = readValue(objectClass);
					}else{
						try {
							try{
								obj = objectClass.newInstance();
							}catch (InstantiationException ex){
							}catch (IllegalAccessException ex){
							}
							if(obj != null){
								//将当前对象放入上下文中
								context.put(obj);
								if(Collection.class.isAssignableFrom(objectClass)){
									this.readCollection((Collection)obj);
								}else if(Map.class.isAssignableFrom(objectClass)){
									this.readMap((Map)obj);
								}else{
									readValue(obj,objectClass,context);
								}

							}else if(!BuilderUtil.isSpecifyBuilder(objectClass)){
									throw new BuilderNotFoundException(objectClass);
							}else{
								obj = readObjectWithAnother(objectClass,context);
								context.put(obj);
							}
						}catch (Exception e) {
							LOGGER.error(e.getCause(), e);
							throw new InvalidDataFormatException(e.getMessage(), e);
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
	 * 读取数组对象
	 * @param context  序列化上下文
	 * @param className 数组类名
	 * @return
	 */
	private Object readArray(Context context,String className) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		Object obj = null;
		//3.读取数组长度
		int length = this.readArrayLength();
		Object[] array = new Object[length];
		Class type = ReflectUtil.get(className.substring(0,className.length() - 2));
		obj = Array.newInstance(type, length);
		context.put(obj);
		for(int i = 0;i < length;i++){
			Array.set(obj , i , this.readValue(type));
		}
		return obj;
	}

	/**
	 * 读取集合对象中的元素，将其放入集合中
	 * @param obj  要装入元素的集合对象
	 * @return
	 */
	private Object readCollection(Collection obj) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		int size = this.readCollectionSize();
		for(int i = 0; i < size; i++){
			obj.add(this.readValue(Object.class));
		}
		return obj;
	}

	/**
	 * 读取key,value,将其存入Map对象
	 * @param obj  要装入key,value的对象
	 * @return
	 */
	private Object readMap(Map obj) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		int size = this.readMapSize();
		for(int i = 0; i < size; i++){
			((Map)obj).put(this.readValue(Object.class),this.readValue(Object.class));
		}
		return obj;
	}

	/**
	 * 当不能调用无参构造方法创建对象时，就用该方法来完成对象的创建及设值工作
	 * @param objectClass
	 * @param context
	 * @return
	 */
	private Object readObjectWithAnother(Class objectClass,Context context) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,IllegalAccessException,InvalidAccessException,BuilderNotFoundException{
		Object obj = null;
		Map<String,Object> map = new HashMap<>();
		if(Collection.class.isAssignableFrom(objectClass) || Map.class.isAssignableFrom(objectClass)){
			int size = this.readCollectionSize();
			List list = new ArrayList<>();
			for(int i = 0; i < size; i++){
				if(Collection.class.isAssignableFrom(objectClass)){
					list.add(this.readValue(Object.class));
				}else{
					list.add(new Entry(this.readValue(Object.class),this.readValue(Object.class)));
				}

			}
			map.put(Builder.LIST, list);
		}else{
			map = (Map)readValue(obj,objectClass,context);
		}
		obj = BuilderUtil.get(objectClass).create(map);
		if(!(obj instanceof Collection) && !(obj instanceof Map)){
			finishObject(obj,map);
		}
		return obj;
	}

	/**
	 * 完成对象属性的赋值
	 * @param obj 要赋值的对象
	 * @param valueMap  存储值的Map
	 */
	private Object finishObject(Object obj,Map<String,Object> valueMap) throws IllegalAccessException{
		Map<String,Object> currentMap = valueMap;
		List<Class> superAndSelfClassList = ReflectUtil.getSelfAndSuperClass(obj.getClass());
		for(Class currentType : superAndSelfClassList){
			Field[] fields = ReflectUtil.getAllInstanceFieldNoOrder(currentType,isCacheField);
			//循环读取属性
			for(int i = 0; i < fields.length; i++){
//				fields[i].setAccessible(true);
				fields[i].set(obj, currentMap.get(fields[i].getName()) );
			}
			currentMap = (Map)currentMap.get(Builder.NEXT);
		}
		return obj;
	}

	/**
	 * 完成集合元素的装载
	 * @param obj 要设值的集合对象
	 * @param valueMap  存储值的Map
	 */
	private void finishCollection(Collection obj,Map<String,Object> valueMap) throws IllegalAccessException{
		if (valueMap != null && valueMap.size() > 0) {
			List list = (List) valueMap.get(Builder.LIST);
			if (list != null) {
				if(obj.size() > 0){
					obj.clear();
				}
				obj.addAll(list);
			}
		}
	}

	/**
	 * 完成集合元素的装载
	 * @param obj 要设值的集合对象
	 * @param valueMap  存储值的Map
	 */
	private void finishMap(Map obj,Map<String,Object> valueMap) throws IllegalAccessException{
		if (valueMap != null && valueMap.size() > 0) {
			List<Entry> list = (List) valueMap.get(Builder.LIST);
			if (list != null) {
				list.stream().forEach(x -> obj.put(x.getKey(), x.getValue()));
			}
		}
	}

	/**
	 * 读取值，将值存入指定的对象中,如果obj,则将读取的值存入Map
	 * @param obj    需要读入值的对象
	 * @param objectClass  要读取对象的类型
	 * @param context 序列化上下文
	 * @return  存储了读取值的对象
	 */
	private Object readValue(Object obj,Class objectClass,Context context) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,BuilderNotFoundException{
//		Class currentType = objectClass;
		//存放字段的值，key为字段名称
		Map<String,Object> valueMap = new HashMap<>();
		Map<String,Object> currentMap = valueMap;
		List<Class> selfAndSuperList = ReflectUtil.getSelfAndSuperClass(objectClass);
		int count = 0;
		for(Class currentType : selfAndSuperList){
			Field[] fields = ReflectUtil.getAllInstanceFieldNoOrder(currentType,isCacheField);
			short fieldCount = this.readFieldCount();
			if(fieldCount != fields.length){
				throw new ClassNotSameException("属性个数不一致");
			}

			//循环读取属性
			if(obj == null){
				for(int i = 0; i < fieldCount; i++){
					context.setCurrentField(fields[i]);
					this.readField(currentMap, fields[i]);
				}
				count++;
				if(count < selfAndSuperList.size()){
					Map<String,Object> tempMap = currentMap;
					currentMap = new HashMap<String,Object>();
					tempMap.put(com.tuling.serialize.Builder.NEXT,currentMap);
				}
			}else{
				for(int i = 0; i < fieldCount; i++){
					context.setCurrentField(fields[i]);
					this.readField(obj,objectClass,fields[i]);
				}
			}
		}
		context.setCurrentField(null);
		if(obj == null){
			return valueMap;
		}else{
			return obj;
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
	 * @param field 当前在读取的字段
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected abstract void readField(Object obj,Class type,Field field) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException;

	/**
	 * 从输入流中读取字段的值，将值放到map中
	 * @param map  存放值的map
	 * @param field 当前即将要读取的字段
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected abstract Map readField(Map map,Field field) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException;


	/**
	 * 读取当前要反序列化的对象的类名
	 * @return  读取的类名
	 * @throws IOException
	 * @throws ClassNotFoundException 当类名对应的类不存在时，抛出此异常
	 */
	protected abstract String readClassName() throws IOException, ClassNotFoundException;



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
	protected abstract Object readValue(Class baseType) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException;



}
