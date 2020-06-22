package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;

import com.sun.corba.se.impl.ior.OldJIDLObjectKeyTemplate;
import com.tuling.domain.User;
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


	//反序列化的时候，是否需要对对象属性进行排序，按序读入属性值
	private boolean needOrder;
	//是否缓存类的字段信息
	private boolean isCacheField = false;


	public AbstractObjectInputStream(boolean needOrder,boolean isCacheField){
		this.needOrder = needOrder;
		this.isCacheField = isCacheField;
	}
	
	public Object readObject(InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException {
		Object obj = null;
		if(!this.isNull(in)){
			Context context = threadLocal.get();
			if(context == null){
				context = new Context();
				threadLocal.set(context);
			}
			context.enter();
			//1.读取序列化对象所对应的类名
			String className = this.readClassName(in);
			//2.判断是否是数组类型
			if(className.endsWith("[]")){
				Class type = ReflectUtil.get(className.substring(0,className.length() - 2));
				obj = readArray(context,type,in);
			}else
			{
				obj = readObjectWithOutArray(context, ReflectUtil.getComplexClass(className),in);
			}
			context.leave();
			if(context.isFinish()){
				threadLocal.remove();
			}
		}
		return obj;
	}


	/**
	 * 读取数组对象
	 * @param context  序列化上下文
	 * @param type 数组类型名，如String,Integer
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	private Object readArray(Context context,Class type,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		Object obj = null;
		//3.读取数组长度
		int length = this.readArrayLength(in);
		Object[] array = new Object[length];
		obj = Array.newInstance(type, length);
		context.put(obj);
		for(int i = 0;i < length;i++){
			Array.set(obj , i , this.readValue(type,in));
		}
		return obj;
	}

	/**
	 * 读取集合对象中的元素，将其放入集合中
	 * @param obj  要装入元素的集合对象
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	private Object readCollection(Collection obj,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		int size = this.readCollectionSize(in);
		for(int i = 0; i < size; i++){
			obj.add(this.readValue(Object.class,in));
		}
		return obj;
	}

	/**
	 * 读取key,value,将其存入Map对象
	 * @param obj  要装入key,value的对象
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	private Object readMap(Map obj,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		int size = this.readMapSize(in);
		for(int i = 0; i < size; i++){
			((Map)obj).put(this.readValue(Object.class,in),this.readValue(Object.class,in));
		}
		return obj;
	}

	/**
	 * 当不能调用无参构造方法创建对象时，就用该方法来完成对象的创建及设值工作
	 * @param objectClass
	 * @param context
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	private Object readObjectWithAnother(Class objectClass,Context context,InputStream in) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,IllegalAccessException,InvalidAccessException,BuilderNotFoundException{
		Object obj = null;
		Map<String,Object> map = new HashMap<>();
//		if(Collection.class.isAssignableFrom(objectClass) || Map.class.isAssignableFrom(objectClass)){
//			int size = this.readCollectionSize();
//			List list = new ArrayList<>();
//			for(int i = 0; i < size; i++){
//				if(Collection.class.isAssignableFrom(objectClass)){
//					list.add(this.readValue(Object.class));
//				}else{
//					list.add(new Entry(this.readValue(Object.class),this.readValue(Object.class)));
//				}
//
//			}
//			map.put(Builder.LIST, list);
//		}else{
			map = (Map)readValue(obj,objectClass,context,in);
//		}

		Builder builder = BuilderUtil.get(objectClass);
		obj = builder.create(map);
		if(!builder.isFinish()){
			if(obj instanceof Collection){
				finishCollection((Collection) obj,map);
			}else if(obj instanceof Map){
				finishMap((Map)obj,map);
			}else{
				finishObject(obj,map);
			}
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
			Field[] fields = ReflectUtil.getAllInstanceField(currentType,needOrder,isCacheField);
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
	 * @param in  包含序列化数据的输入流
	 * @return  存储了读取值的对象
	 */
	private Object readValue(Object obj,Class objectClass,Context context,InputStream in) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,BuilderNotFoundException{
//		Class currentType = objectClass;
		//存放字段的值，key为字段名称
		Map<String,Object> valueMap = new HashMap<>();
		Map<String,Object> currentMap = valueMap;
		List<Class> selfAndSuperList = ReflectUtil.getSelfAndSuperClass(objectClass);
		int count = 0;
		for(Class currentType : selfAndSuperList){
			Field[] fields = ReflectUtil.getAllInstanceField(currentType,needOrder,isCacheField);
			short fieldCount = this.readFieldCount(in);
			if(fieldCount != fields.length){
				throw new ClassNotSameException("属性个数不一致");
			}

			//循环读取属性
			if(obj == null){
				for(int i = 0; i < fieldCount; i++){
					context.setCurrentField(fields[i]);
					this.readField(currentMap, fields[i],in);
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
					this.readField(obj,objectClass,fields[i],in);
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
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected abstract boolean start(InputStream in) throws IOException;
	
	/**
	 * 判断对象数据的读取是否已经结束
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected abstract boolean end(InputStream in) throws IOException;

	/**
	 * 判断当前要读取的值是否为空
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected abstract boolean isNull(InputStream in) throws IOException;
	
	/**
	 * 读取属性总个数
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected abstract short readFieldCount(InputStream in) throws IOException;

	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param type 属性定义所在的类
	 * @param field 当前在读取的字段
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected abstract void readField(Object obj,Class type,Field field,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException;

	/**
	 * 从输入流中读取字段的值，将值放到map中
	 * @param map  存放值的map
	 * @param field 当前即将要读取的字段
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected abstract Map readField(Map map,Field field,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException;


	/**
	 * 读取当前要反序列化的对象的类名
	 * @param in  包含序列化数据的输入流
	 * @return  读取的类名
	 * @throws IOException
	 * @throws ClassNotFoundException 当类名对应的类不存在时，抛出此异常
	 */
	protected abstract String readClassName(InputStream in) throws IOException, ClassNotFoundException;



	/**
	 * 读取数组长度
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected abstract int readArrayLength(InputStream in) throws IOException;

	/**
	 * 读取集合元素个数
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected  abstract int readCollectionSize(InputStream in) throws IOException;

	/**
	 * 读取Map元素个数
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected  abstract int readMapSize(InputStream in) throws IOException;

	/**
	 * 从流中当前位置读取指定类型的值
	 * @param type  要读取数据的类型
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	protected abstract Object readValue(Class baseType,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException;

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
	public Object  readObject(Class objectClass,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException , ClassNotSameException,BuilderNotFoundException{
		if(objectClass == null){
			return readObject(in);
		}
		Object obj = null;
		if(!this.isNull(in)){
			Context context = threadLocal.get();
			if(context == null){
				context = new Context();
				threadLocal.set(context);
			}
			context.enter();

			//2.判断是否是数组类型
			if(objectClass.isArray()){
				obj = readArray(context,objectClass.getComponentType(),in);
			}else
			{
				obj = readObjectWithOutArray(context,objectClass,in);
			}
			context.leave();
			if(context.isFinish()){
				threadLocal.remove();
			}
		}
		return obj;
	}

	/**
	 * 读取数组类型之外的对象
	 * @param context
	 * @param objectClass
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException
	 * @throws InvalidAccessException
	 * @throws ClassNotSameException
	 * @throws BuilderNotFoundException
	 */
	private Object readObjectWithOutArray(Context context,Class objectClass,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		Object obj = null;
		try {

			if(ReflectUtil.isBaseType(objectClass) || objectClass.isEnum()){
				obj = readValue(objectClass,in);
			}else{
				try {
					obj = createObject(objectClass);
					if(obj != null){
						//将当前对象放入上下文中
						context.put(((Object) obj));
//						if(Collection.class.isAssignableFrom(objectClass)){
//							this.readCollection((Collection)obj);
//						}else if(Map.class.isAssignableFrom(objectClass)){
//							this.readMap((Map)obj);
//						}else{
							readValue(obj,objectClass,context,in);
						//}

					}else if(!BuilderUtil.isSpecifyBuilder(objectClass)){
						throw new BuilderNotFoundException(objectClass);
					}else{
						obj = readObjectWithAnother(objectClass,context,in);
						context.put(obj);
					}
				}catch (Exception e) {
					LOGGER.error(e.getCause(), e);
					throw new InvalidDataFormatException(e.getMessage(), e);
				}
			}
		} catch (ClassNotFoundException e) {
			LOGGER.error(e.getMessage(), e);
			throw e;
		}
		return  obj;
	}

	/**
	 * 创建一个objectClass类型的对象
	 * @param objectClass
	 * @return
	 */
	private Object createObject(Class objectClass){
		Object result = null;
		try{
			result = objectClass.newInstance();
		}catch (Exception ex){
			Constructor[] constructors = objectClass.getDeclaredConstructors();
			if(constructors.length > 0){
				Constructor constructor = getProperConstructor(constructors);
				constructor.setAccessible(true);
				Class[] paramTypes = constructor.getParameterTypes();
				Object[] args = new Object[paramTypes.length];
				for(int i = 0; i < args.length; i++){
					args[i] = getDefaultValue(paramTypes[i]);
				}
				try {
					result = constructor.newInstance(args);
				}catch (Exception ex2){
					LOGGER.error("通过反射调用构造方法创建对象失败|" + ex2.getMessage(),ex2);
				}

			}

		}


		return  result;
	}

	/**
	 * 从指定的构造方法列表中寻找到参数最少的构造方法
	 * @param constructors
	 * @return
	 */
	public Constructor getProperConstructor(Constructor[] constructors){
		Constructor result = constructors[0];
		if(constructors.length > 1){
			for(int i = 1;i < constructors.length; i++){
				if(constructors[i].getParameterCount() < result.getParameterCount()){
					result = constructors[i];
				}
			}

		}
		return result;
	}

	/**
	 * 用简易的方式创建对象,即调用无参构造方法创建，如果创建失败，返回null
	 * @return
	 */
	private Object createObjectWithSimple(Class type){
		try {
			return type.newInstance();
		}catch (Exception ex){
			return null;
		}
	}

	/**
	 * 获取指定类型参数对应的默认值
	 * @param type
	 * @return
	 */
	protected Object getDefaultValue(Class type){
		if(ReflectUtil.isBaseType(type)){
			switch (type.getSimpleName()) {
				case "boolean":
				case "Boolean":
					return true;
				case "byte":
				case "Byte":
					return (byte)0;
				case "char":
				case "Character":
					return (char)0;
				case "short":
				case "Short":
					return (short)0;
				case "int":
				case "Integer":
					return 0;
				case "long":
				case "Long":
					return 0L;
				case "float":
				case "Float":
					return 0.0f;
				case "double":
				case "Double":
					return 0.0d;
				default:
					return "";
			}
		}else if(type.isArray()){
			return Array.newInstance(type.getComponentType(),0);
		}else{
			return createObjectWithSimple(type);
		}
	}
}
