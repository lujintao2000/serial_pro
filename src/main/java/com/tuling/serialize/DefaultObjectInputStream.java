package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;

import com.tuling.serialize.exception.BuilderNotFoundException;
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

	public DefaultObjectInputStream(){
		this(false, false);
	}

	/**
	 *
	 * @param needOrder  	//反序列化的时候，是否需要对对象属性进行排序，按序读入属性值
	 * @param isCacheField 表示是否缓存类的字段信息
	 */
	public DefaultObjectInputStream( boolean needOrder,boolean isCacheField){
		super( needOrder,isCacheField);
	}
	
	
	@Override
	/**
	 * 判断是否是对象读取的开始位置
	 * @param in  包含序列化数据的输入流
	 */
	protected boolean start(InputStream in) throws IOException{
		return in.read() == Constant.BEGIN_FLAG;
	}
	
	@Override
	/**
	 * 判断是否是对象读取的结束位置
	 * @param in  包含序列化数据的输入流
	 */
	protected boolean end(InputStream in) throws IOException{
		return in.read() == Constant.END_FLAG;
	}

	/**
	 * 判断当前即将要读取到的对象是否为已反序列化对象的引用
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private boolean isReference(InputStream in) throws IOException{
		return in.read() == Constant.REFERENCE_FLAG;
	}
	
	/**
	 * 读取属性总个数
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	protected  short readFieldCount(InputStream in) throws IOException{
		return this.readShort(in);
	}

	/**
	 * 从输入流中读取属性的值并给属性设置值
	 * @param obj
	 * @param type 属性定义所在的类
	 * @param field 当前在读取的字段
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected  void readField(Object obj,Class type,Field field,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		try {
			field.setAccessible(true);
			try {
				field.set(obj,this.readValue(field.getType(),in) );

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
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
	protected  Map readField(Map map, Field field,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
		if(map == null){
			throw new IllegalArgumentException("map can't be null");
		}
		map.put(field.getName(),this.readValue(field.getType(),in));
		return map;
	}

	/**
	 * 从指定输入流中读取当前要反序列化对象的类名()
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException 
	 */
	@Override
	protected String readClassName(InputStream in) throws IOException, ClassNotFoundException {
		Context context = threadLocal.get();
		//1. 读入类名字节长度
		short length = this.readShort(in);
		if(length > 0){
			//2. 读入类名
			byte[] classNameByteArray = new byte[length];
			in.read(classNameByteArray);
			String fullClassName = ReflectUtil.getFullName(new String(classNameByteArray));
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
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	@Override
	protected int readArrayLength(InputStream in) throws IOException{
		return this.readInt(in);
	}

	/**
	 * 读取集合元素个数
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	@Override
	protected  int readCollectionSize(InputStream in) throws IOException{
		return this.readInt(in);
	}

	/**
	 * 读取Map元素个数
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	@Override
	protected  int readMapSize(InputStream in) throws IOException{
		return this.readInt(in);
	}

	/**
	 * 读取布尔值
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private Boolean readBoolean(InputStream in) throws IOException{
		return (in.read() == 1) ? true : false;
	}

	/***
	 * 读取字符类型数据
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private Character readCharacter(InputStream in) throws IOException{
		byte[] array = new byte[2];
		in.read(array);
		char result = (char)(NumberUtil.converByteToInt(array[0]) * 256 + NumberUtil.converByteToInt(array[1]));
		return  result;
	}

	/**
	 * 读取字节型数据
	 * @return
	 * @throws IOException
	 */
	private Byte readByte(InputStream in) throws IOException{
		return NumberUtil.convertIntToByte(in.read());
	}

	/**
	 * 读取长整型数据
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private Long readLong(InputStream in) throws IOException{
		byte[] array = new byte[8];
		for(int i = 0; i < 8;i++){
			array[i] = NumberUtil.convertIntToByte(in.read());
		}
		return NumberUtil.getLong( array );
	}

	private Float readFloat(InputStream in) throws IOException{
		return Float.intBitsToFloat(this.readInt(in));
	}

	private Double readDouble(InputStream in) throws IOException{
		Long num = this.readLong(in);
		return Double.longBitsToDouble(num);
	}

	/**
	 * 读取字符串
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private String readString(InputStream in) throws IOException{
		//1. 读取字符串对应字节长度
		int length = this.readInt(in);
		byte[] array = new byte[length];
		//读取字符串内容对应的字节数据
		in.read(array);
		return new String(array);
	}

	/**
	 * 读取短整型值
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private Short readShort(InputStream in) throws IOException{
		return (short)(in.read() * 256 + in.read());
	}

	/**
	 * 读取整型值
	 * @param in  包含序列化数据的输入流
	 * @return
	 * @throws IOException
	 */
	private Integer readInt(InputStream in) throws IOException{
		byte[] array = new byte[4];
		for(int i = 0; i < 4;i++){
			array[i] = NumberUtil.convertIntToByte(in.read());
		}
		return NumberUtil.getInteger( array );
	}

	/**
	 * 从流中当前位置读取指定类型的值
	 * @param type  要读取数据所对应的字段类型
	 * @param in  包含序列化数据的输入流
	 * @return
	 */
	protected Object readValue(Class type,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
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

				String className = this.readClassName(in);
				int index = this.readShort(in);
				Class valueType = null;
				if(className.endsWith("[]")){
					valueType = Array.newInstance(ReflectUtil.get(className.substring(0,className.length() - 2)),0).getClass();
				}else{
					valueType = ReflectUtil.get(className);
				}
				Context context = threadLocal.get();
				value = context.get(valueType, index);
			}else{
				value = this.readObject(in);
			}
		}
		return value;
	}

	/**
	 * 判断当前要读取的值是否为空
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 */
	@Override
	protected  boolean isNull(InputStream in) throws IOException{
		return in.read() == Constant.NULL_FLAG;
	}
}
