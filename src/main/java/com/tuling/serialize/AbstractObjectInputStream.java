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
import com.tuling.serialize.exception.*;
import com.tuling.serialize.util.*;
import org.apache.log4j.Logger;

/**
 * 该类主要实现了对象的反序列化
 * @author lujintao
 * @date 2017-04-16
 */
public abstract class AbstractObjectInputStream implements ObjectInputStream{

	private static final Logger LOGGER = Logger.getLogger(AbstractObjectInputStream.class);
	protected static final ThreadLocal<Context> threadLocal = new ThreadLocal();


	//是否缓存类的字段信息
	protected boolean isCacheField = true;


	public AbstractObjectInputStream(){
	}

	public AbstractObjectInputStream(boolean isCacheField){
		this.isCacheField = isCacheField;
	}
	
	public Object readObject(InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException {
		return readObject(null,in);
	}


	/**
	 * 读取数组对象
	 * @param context  序列化上下文
	 * @param type 数组类型名，如String,Integer
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	protected Object readArray(Context context,Class type,ByteBuf in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		Object obj = null;
		//3.读取数组长度
		int length = this.readArrayLength(in);
		Object[] array = new Object[length];
		obj = Array.newInstance(type, length);
		context.put(obj);
		for(int i = 0;i < length;i++){
			Array.set(obj , i , this.readValue(type,in,context));
		}
		return obj;
	}

	/**
	 * 读取集合对象中的元素，将其放入集合中
	 * @param obj  要装入元素的集合对象
	 * @param in  包含序列化数据的输入流
	 * @param context 序列化上下文
	 * @return
	 */
	protected Object readCollection(Collection obj,ByteBuf in,Context context) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		int size = this.readCollectionSize(in);
		for(int i = 0; i < size; i++){
			obj.add(this.readValue(Object.class,in,context));
		}
		return obj;
	}

	/**
	 * 读取key,value,将其存入Map对象
	 * @param obj  要装入key,value的对象
	 * @param in  包含序列化数据的输入流
	 * @param context 序列化上下文
	 * @return
	 */
	protected Object readMap(Map obj,ByteBuf in,Context context) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		int size = this.readMapSize(in);
		for(int i = 0; i < size; i++){
			((Map)obj).put(this.readValue(Object.class,in,context),this.readValue(Object.class,in,context));
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
	protected Object readObjectWithAnother(Class objectClass,Context context,ByteBuf in) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,IllegalAccessException,InvalidAccessException,BuilderNotFoundException{
		Object obj = null;
		Map<String,Object> map = new HashMap<>();
		map = (Map)readValue(obj,objectClass,context,in);
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
	protected Object finishObject(Object obj,Map<String,Object> valueMap) throws IllegalAccessException{
		Map<String,Object> currentMap = valueMap;
		List<Class> superAndSelfClassList = ReflectUtil.getSelfAndSuperClass(obj.getClass());
		for(Class currentType : superAndSelfClassList){
			Field[] fields = ReflectUtil.getAllInstanceField(currentType,isCacheField);
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
	protected void finishCollection(Collection obj,Map<String,Object> valueMap) throws IllegalAccessException{
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
	protected void finishMap(Map obj,Map<String,Object> valueMap) throws IllegalAccessException{
		if (valueMap != null && valueMap.size() > 0) {
			List<Entry> list = (List) valueMap.get(Builder.LIST);
			if (list != null) {
				list.stream().forEach(x -> obj.put(x.getKey(), x.getValue()));
			}
		}
	}

	/**
	 * 读取值，将值存入指定的对象中,如果obj为空,则将读取的值存入Map
	 * @param obj    需要读入值的对象
	 * @param objectClass  要读取对象的类型
	 * @param context 序列化上下文
	 * @param in  包含序列化数据的输入流
	 * @return  存储了读取值的对象
	 */
	protected abstract Object readValue(Object obj,Class objectClass,Context context,ByteBuf in) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,BuilderNotFoundException;



	/**
	 * 判断是否是对象读取的开始位置
	 * @param in  包含序列化数据的输入流
	 */
	protected boolean start(ByteBuf in) throws IOException{
//		return in.read() == Constant.BEGIN_FLAG;
		return in.readByte() == (byte) Constant.BEGIN_FLAG;
	}


	/**
	 * 判断是否是对象读取的结束位置
	 * @param in  包含序列化数据的输入流
	 */
	protected boolean end(ByteBuf in) throws IOException{
//		return in.read() == Constant.END_FLAG;
		return in.readByte() == (byte) Constant.END_FLAG;
	}

	/**
	 * 判断当前要读取的值是否为空
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected  boolean isNull(ByteBuf in) throws IOException{
//		return in.read() == Constant.NULL_FLAG;
		return in.readByte() == (byte) Constant.NULL_FLAG;
	}
	
	/**
	 * 读取属性总个数
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected  short readFieldCount(ByteBuf in) throws IOException{
		return this.readShort(in);
	}

	/**
	 * 读取短整型值
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected Short readShort(ByteBuf in) throws IOException{
//		return (short)(in.read() * 256 + in.read());
		return in.readShort();
	}

	/**
	 * 读取整型值
	 * @param in  包含序列化数据的缓冲
	 * @return
	 * @throws IOException
	 */
	protected Integer readInt(ByteBuf in) throws IOException{
//		byte[] array = new byte[4];
//		for(int i = 0; i < 4;i++){
//			array[i] = NumberUtil.convertIntToByte(in.read());
//		}
//		return NumberUtil.getInteger( array );
		return in.readInt();
	}

	/**
	 * 读取整型值
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected Integer readInt(InputStream in) throws IOException{
		byte[] array = new byte[4];
		for(int i = 0; i < 4;i++){
			array[i] = NumberUtil.convertIntToByte(in.read());
		}
		return NumberUtil.getInteger( array );
	}

	/**
	 * 读取布尔值
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected Boolean readBoolean(ByteBuf in) throws IOException{
//		return (in.read() == 1) ? true : false;
		return in.readBoolean();
	}

	/***
	 * 读取字符类型数据
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected Character readCharacter(ByteBuf in) throws IOException{
//		byte[] array = new byte[2];
//		in.read(array);
//		char result = (char)(NumberUtil.converByteToInt(array[0]) * 256 + NumberUtil.converByteToInt(array[1]));
//		return  result;

		return in.readChar();
	}

	/**
	 * 读取字节型数据
	 * @return
	 * @throws IOException
	 */
	protected Byte readByte(ByteBuf in) throws IOException{
//		return NumberUtil.convertIntToByte(in.read());
		return in.readByte();
	}

	/**
	 * 读取长整型数据
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected Long readLong(ByteBuf in) throws IOException{
//		byte[] array = new byte[8];
//		for(int i = 0; i < 8;i++){
//			array[i] = NumberUtil.convertIntToByte(in.read());
//		}
//		return NumberUtil.getLong( array );
		return in.readLong();
	}

	protected Float readFloat(ByteBuf in) throws IOException{
//		return Float.intBitsToFloat(this.readInt(in));
		return in.readFloat();
	}

	protected Double readDouble(ByteBuf in) throws IOException{
//		Long num = this.readLong(in);
//		return Double.longBitsToDouble(num);
		return in.readDouble();
	}

	/**
	 * 读取字符串
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	protected String readString(ByteBuf in) throws IOException{
		//1. 读取字符串对应字节长度
		int length = this.readInt(in);
//		byte[] array = new byte[length];
		//读取字符串内容对应的字节数据
//		in.read(array);
//		in.readBytes(array);
//		return new String(array,"unicode");
//		return in.readCharSequence()
		return in.readString(length);
	}

	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param type 属性定义所在的类
	 * @param field 当前在读取的字段
	 * @param in  包含序列化数据的缓冲
	 * @param context 序列化上下文
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected  void readField(Object obj,Class type,Field field,ByteBuf in,Context context) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		try {
			field.setAccessible(true);
			try {
				field.set(obj,this.readValue(field.getType(),in,context) );

			} catch (IllegalArgumentException e) {
				LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
				throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
			} catch (IllegalAccessException e) {
				LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
				throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
			}
		} catch (SecurityException e) {
			LOGGER.error(String.format("属性 %s 访问受限", field.getName()), e);
			throw new InvalidAccessException(String.format("属性 %s 访问受限", field.getName()), e);
		}
	}
	/**
	 * 从输入流中读取字段的值，将值放到map中
	 * @param map  存放值的map
	 * @param field 当前即将要读取的字段
	 * @param in  包含序列化数据的缓冲
	 * @param context 序列化上下文
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected  Map readField(Map map, Field field,ByteBuf in,Context context) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		if(map == null){
			throw new IllegalArgumentException("map can't be null");
		}
		map.put(field.getName(),this.readValue(field.getType(),in,context));
		return map;
	}

	/**
	 * 读取当前要反序列化的对象的类名
	 * @param in  包含序列化数据的输入流
	 * @param context 序列化上下文
	 * @return  读取的类名
	 * @throws IOException
	 * @throws ClassNotFoundException 当类名对应的类不存在时，抛出此异常
	 */
	protected String readClassName(ByteBuf in,Context context) throws IOException, ClassNotFoundException {
		//1. 读入类名字节长度
		short length = this.readShort(in);
		if(length > 0){
			//2. 读入类名
//			byte[] classNameByteArray = new byte[length];
//			in.readBytes(classNameByteArray);
			String fullClassName = ReflectUtil.getFullName(in.readString(length));
			if(fullClassName.equals(BaseTypeEnum.VOID.getType().getTypeName())){
				fullClassName = context.getCurrentField().getType().getTypeName();
			}
			context.addClassName(fullClassName);
			return fullClassName;
		}else{
			//读取引用序号
			short index = this.readShort(in);
			return context.getClassName(index);
		}
	}


	/**
	 * 读取数组长度
	 * @param in  包含序列化数据的缓冲
	 * @return
	 * @throws IOException
	 */
	protected int readArrayLength(ByteBuf in) throws IOException{
//		return this.readInt(in);
		return in.readInt();
	}

	/**
	 * 读取集合元素个数
	 * @param in  包含序列化数据的缓冲
	 * @return
	 * @throws IOException
	 */
	protected  int readCollectionSize(ByteBuf in) throws IOException{
//		return this.readInt(in);
		return in.readInt();
	}

	/**
	 * 读取Map元素个数
	 * @param in  包含序列化数据的缓冲
	 * @return
	 * @throws IOException
	 */
	protected  int readMapSize(ByteBuf in) throws IOException{
//		return this.readInt(in);
		return in.readInt();
	}

	/**
	 * 判断当前即将要读取到的对象是否为已反序列化对象的引用
	 * @param in  包含序列化数据的缓冲
	 * @return
	 * @throws IOException
	 */
	protected boolean isReference(ByteBuf in) throws IOException{
		//return in.read() == Constant.REFERENCE_FLAG;
		return in.readByte() == (byte) Constant.REFERENCE_FLAG;
	}

	/**
	 * 从流中当前位置读取指定类型的值
	 * @param type  要读取数据的类型
	 * @param in  包含序列化数据的缓冲
	 * @param context 序列化上下文
	 * @return
	 */
	protected Object readValue(Class type,ByteBuf in,Context context) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		if(isNull(in)){
			return null;
		}
		Object value = null;
		if(type == boolean.class || type == Boolean.class){
			value = this.readBoolean(in);
		}else if(type == char.class || type == Character.class){
			value = this.readCharacter(in);
		}else if(type == byte.class || type == Byte.class){
			value = this.readByte(in);
		}else if(type == short.class || type == Short.class){
			value = this.readShort(in);
		}else if(type == int.class || type == Integer.class){
			value = this.readInt(in);
		}else if(type == long.class || type == Long.class){
			value = this.readLong(in);
		}else if(type == float.class || type == Float.class){
			value = this.readFloat(in);
		}else if(type == double.class || type == Double.class){
			value = this.readDouble(in);
		}else if(type == String.class){
			value = this.readString(in);
		}else if(type.isEnum()){
			String name = this.readString(in);
			try {
				Method method = type.getMethod("valueOf",String.class);
				value = method.invoke(null,name);
			} catch (Exception e) {

			}
		}
		else{
			if(isReference(in)){

				String className = this.readClassName(in,context);
				int index = this.readShort(in);
				Class valueType = null;
				if(className.endsWith("[]")){
					valueType = Array.newInstance(ReflectUtil.get(className.substring(0,className.length() - 2)),0).getClass();
				}else{
					valueType = ReflectUtil.get(className);
				}
				value = context.get(valueType, index);
			}else{
				value = this.readObject(null,in,context);
			}
		}
		return value;
	}

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
		//判断序列化格式版本是否支持
		int version = readVersion(in);
		if (version < Constant.MIN_VERSION || version > Constant.MAX_VERSION) {
			throw new VersionNotSupportException("Current version of serialization is not supported.Current version is " + version + ",but " + (version < Constant.MIN_VERSION ? "the mininum supported version is " + Constant.MIN_VERSION : "the maximum supported version is " + Constant.MAX_VERSION));
		}
		Context context = Context.create();

		//读取对象长度
		int length = this.readInt(in);
		byte[] objectData = new byte[length];
		in.read(objectData);
		//先将对象数据读到缓冲
		ByteBuf buf = new ByteBuf(objectData);
		Object result = readObject(objectClass,buf,context);
		buf.release();
		context.destory();
		return result;
	}


		/**
         * @param type 需要反序列化对象的类型
         * @param in 包含序列化数据的输入流
		 * @param context 序列化上下文
         * @return 反序列化出的对象
         * @throws IOException
         * @throws ClassNotFoundException
         * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
         * @throws InvalidAccessException  如果方法或字段不让访问或方法传递参数不对，抛出该异常
         * @throws ClassNotSameException  当反序列化时加载的类的属性与序列化时类的属性不一致时，抛出此异常
         */
	public Object  readObject(Class objectClass,ByteBuf in,Context context) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException , ClassNotSameException,BuilderNotFoundException{
		Object obj = null;
		if(!this.isNull(in)){
			if(objectClass == null){
				//1.读取序列化对象所对应的类名
				String className = this.readClassName(in,context);
				//2.判断是否是数组类型
				if(className.endsWith("[]")){
					Class type = ReflectUtil.get(className.substring(0,className.length() - 2));
					objectClass = Array.newInstance(type,0).getClass();
				}else
				{
					objectClass =  ReflectUtil.getComplexClass(className);
				}
			}

			//2.判断是否是数组类型
			if(objectClass.isArray()){
				obj = readArray(context,objectClass.getComponentType(),in);
			}else
			{
				obj = readObjectWithOutArray(context,objectClass,in);
			}
		}
		return obj;
	}


	/**
	 * 读取当前对象数据的序列化格式版本
	 * @param in
	 * @return
	 */
	private int readVersion(InputStream in) throws IOException{
		return in.read();
	}

	/**
	 * 读取数组类型之外的对象
	 * @param context
	 * @param objectClass
	 * @param in  包含序列化数据的缓冲
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 * @throws InvalidDataFormatException
	 * @throws InvalidAccessException
	 * @throws ClassNotSameException
	 * @throws BuilderNotFoundException
	 */
	protected Object readObjectWithOutArray(Context context,Class objectClass,ByteBuf in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		Object obj = null;
		try {
			if(ReflectUtil.isBaseType(objectClass) || objectClass.isEnum()){
				obj = readValue(objectClass,in,context);
			}else{
				try {
					obj = createObject(objectClass);
					if(obj != null){
						//将当前对象放入上下文中
						context.put(((Object) obj));
						readValue(obj,objectClass,context,in);
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
	protected Object createObject(Class objectClass){
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
	protected Object createObjectWithSimple(Class type){
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
