package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.Constant;
import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import org.apache.log4j.Logger;

/**
 * 从输入流中读取数据，将数据转变成对应的对象
 * @author Administrator
 *
 */
public class DefaultObjectInputStream extends AbstractObjectInputStream{
	private static final Logger LOGGER = Logger.getLogger(DefaultObjectInputStream.class);

	public DefaultObjectInputStream(InputStream in){
		this(in,false, false);
	}

	/**
	 *
	 * @param in  序列化输入流
	 * @param needOrder  	//反序列化的时候，是否需要对对象属性进行排序，按序读入属性值
	 * @param isCacheField 表示是否缓存类的字段信息
	 */
	public DefaultObjectInputStream(InputStream in, boolean needOrder,boolean isCacheField){
		super(in, needOrder,isCacheField);
	}
	
	
	@Override
	protected boolean start() throws IOException{
		return this.in.read() == Constant.BEGIN_FLAG;
	}
	
	@Override
	protected boolean end() throws IOException{
		return this.in.read() == Constant.END_FLAG;
	}

	/**
	 * 判断当前即将要读取到的对象是否为已反序列化对象的引用
	 * @return
	 * @throws IOException
	 */
	private boolean isReference() throws IOException{
		return this.in.read() == Constant.REFERENCE_FLAG;
	}
	
	/**
	 * 读取属性总个数
	 * @throws IOException
	 */
	protected  short readFieldCount() throws IOException{
		return this.readShort();
	}
	
	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param currentType 属性定义所在的类
	 * @throws IOException 
	 * @throws InvalidDataFormatException 
	 * @throws SecurityException 
	 * @throws NoSuchFieldException 
	 */
	@Override
	protected void readField(Object obj,Class currentType) throws IOException,ClassNotFoundException, InvalidDataFormatException,InvalidAccessException,ClassNotSameException {
		int length = this.in.read();
		byte[] fieldByteArray = new byte[length];
		this.in.read(fieldByteArray);
		String fieldName = new String(fieldByteArray);
		try {
			Field field = currentType.getDeclaredField(fieldName);
			field.setAccessible(true);
			try {
				Object value = null;
				if(!isNull()){
					Class valueType = field.getType();
					//如果字段不是基本数据类型
					if(!ReflectUtil.isBaseType(valueType)){
						valueType = Class.forName(this.readClassName());
					}
					value = this.readValue(valueType);
				}
				field.set(obj,value );

			} catch (IllegalArgumentException e) {
				LOGGER.error(e.getCause() + "|field:" + fieldName, e);
				throw new InvalidAccessException(e.getCause() + "|field:" + fieldName, e);
			} catch (IllegalAccessException e) {
				LOGGER.error(e.getCause() + "|field:" + fieldName, e);
				throw new InvalidAccessException(e.getCause() + "|field:" + fieldName, e);
			}
		} catch (NoSuchFieldException e) {
			LOGGER.error("没有该属性:" + fieldName, e);
			throw new InvalidDataFormatException("没有该属性:" + fieldName, e);
		} catch (SecurityException e) {
			LOGGER.error(String.format("属性 %s 访问受限", fieldName), e);
			throw new InvalidAccessException(String.format("属性 %s 访问受限", fieldName), e);
		}
	}

	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param type 属性定义所在的类
	 * @param field 当前在读取的字段
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected  void readField(Object obj,Class type,Field field) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException{
		try {
			field.setAccessible(true);
			try {
				Object value = null;
				if(!isNull()){
					Class valueType = field.getType();
					//如果字段不是基本数据类型
					if(!ReflectUtil.isBaseType(valueType)){
						valueType = Class.forName(this.readClassName());
					}
					value = this.readValue(valueType);
				}
				field.set(obj,value );
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
	 * 从指定输入流中读取当前要反序列化对象的类名
	 * @param in
	 * @return
	 * @throws IOException 
	 */
	@Override
	protected String readClassName() throws IOException, ClassNotFoundException {
		Context context = threadLocal.get();
		//1. 读入类名字节长度
		short length = this.readShort();
		if(length > 0){
			//2. 读入类名
			byte[] classNameByteArray = new byte[length];
			this.in.read(classNameByteArray);
			String className = new String(classNameByteArray);
			context.addClassName(className);
			return className;
		}else{
			//读取引用序号
			short index = this.readShort();
			return context.getClassName(index);
		}
	}


	
	/**
	 * 读取数组长度
	 * @return
	 * @throws IOException
	 */
	@Override
	protected int readArrayLength() throws IOException{
		return this.readInt();
	}

	/**
	 * 读取集合元素个数
	 * @return
	 * @throws IOException
	 */
	@Override
	protected  int readCollectionSize() throws IOException{
		return this.readInt();
	}

	/**
	 * 读取Map元素个数
	 * @return
	 * @throws IOException
	 */
	@Override
	protected  int readMapSize() throws IOException{
		return this.readInt();
	}

	/**
	 * 读取布尔值
	 * @return
	 * @throws IOException
	 */
	private Boolean readBoolean() throws IOException{
		return (this.in.read() == 1) ? true : false;
	}

	/***
	 * 读取字符类型数据
	 * @return
	 * @throws IOException
	 */
	private Character readCharacter() throws IOException{
		byte[] array = new byte[2];
		this.in.read(array);
		char result = (char)(NumberUtil.converByteToInt(array[0]) * 256 + NumberUtil.converByteToInt(array[1]));
		return  result;
	}

	/**
	 * 读取字节型数据
	 * @return
	 * @throws IOException
	 */
	private Byte readByte() throws IOException{
		return NumberUtil.convertIntToByte(this.in.read());
	}

	/**
	 * 读取长整型数据
	 * @return
	 * @throws IOException
	 */
	private Long readLong() throws IOException{
		byte[] array = new byte[8];
		for(int i = 0; i < 8;i++){
			array[i] = NumberUtil.convertIntToByte(this.in.read());
		}
		return NumberUtil.getLong( array );
	}

	private Float readFloat() throws IOException{
		return Float.intBitsToFloat(this.readInt());
	}

	private Double readDouble() throws IOException{
		Long num = this.readLong();
		return Double.longBitsToDouble(num);
	}

	private String readString() throws IOException{
		//1. 读取字符串对应字节长度
		int length = this.readInt();
		byte[] array = new byte[length];
		//读取字符串内容对应的字节数据
		this.in.read(array);
		return new String(array);
	}

	/**
	 * 读取短整型值
	 * @return
	 * @throws IOException
	 */
	private Short readShort() throws IOException{
		return (short)(this.in.read() * 256 + this.in.read());
	}

	/**
	 * 读取整型值
	 * @return
	 * @throws IOException
	 */
	private Integer readInt() throws IOException{
		byte[] array = new byte[4];
		for(int i = 0; i < 4;i++){
			array[i] = NumberUtil.convertIntToByte(this.in.read());
		}
		return NumberUtil.getInteger( array );
	}

	/**
	 * 从流中当前位置读取指定类型的值
	 * @param type  要读取数据的类型
	 * @return
	 */
	protected Object readValue(Class type) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException{
//		if(isNull()){
//			return null;
//		}
		Object value = null;
		if(type == boolean.class || type == Boolean.class){
			value = this.readBoolean();
		}else if(type == char.class || type == Character.class){
			value = this.readCharacter();
		}else if(type == byte.class || type == Byte.class){
			value = this.readByte();
		}else if(type == short.class || type == Short.class){
			value = this.readShort();
		}else if(type == int.class || type == Integer.class){
			value = this.readInt();
		}else if(type == long.class || type == Long.class){
			value = this.readLong();
		}else if(type == float.class || type == Float.class){
			value = this.readFloat();
		}else if(type == double.class || type == Double.class){
			value = this.readDouble();
		}else if(type == String.class){
			value = this.readString();
		}else{
			this.in.mark(0);
			if(isReference()){
				int index = this.readInt();
				Context context = threadLocal.get();
				value = context.get(type, index);
			}else{
				this.in.reset();
				value = this.readObject();
			}
		}
		return value;
	}

	/**
	 * 判断当前要读取的值是否为空
	 * @throws IOException
	 */
	protected  boolean isNull() throws IOException{
		return this.in.read() == Constant.NULL_FLAG;
	}
}
