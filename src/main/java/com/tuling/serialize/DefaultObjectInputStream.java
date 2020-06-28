package com.tuling.serialize;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.tuling.serialize.exception.BuilderNotFoundException;
import com.tuling.serialize.exception.ClassNotSameException;
import com.tuling.serialize.exception.InvalidAccessException;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.ByteBuf;
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
		this( true);
	}

	/**
	 *
	 * @param isCacheField 表示是否缓存类的字段信息
	 */
	public DefaultObjectInputStream(boolean isCacheField){
		super( isCacheField);
	}
	

//	/**
//	 * 从输入流中读取属性的值并给属性设置值
//	 * @param obj 要读取属性值的对象
//	 * @param type 属性定义所在的类
//	 * @param field 当前在读取的字段
//	 * @param in  包含序列化数据的输入流
//	 * @throws IOException
//	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
//	 */
//	protected  void readField(Object obj,Class type,Field field,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
//		try {
//			field.setAccessible(true);
//			try {
//				field.set(obj,this.readValue(field.getType(),in) );
//
//			} catch (IllegalArgumentException e) {
//				LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
//				throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
//			} catch (IllegalAccessException e) {
//				LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
//				throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
//			}
//		} catch (SecurityException e) {
//			LOGGER.error(String.format("属性 %s 访问受限", field.getName()), e);
//			throw new InvalidAccessException(String.format("属性 %s 访问受限", field.getName()), e);
//		}
//	}

	/**
	 * 从输入流中读取字段的值，将值放到map中
	 * @param map  存放值的map
	 * @param field 当前即将要读取的字段
	 * @param in  包含序列化数据的输入流
	 * @throws IOException
	 * @throws InvalidDataFormatException 如果反序列化数据的格式和具体序列化实现的要求不一致，抛出该异常
	 */
//	protected  Map readField(Map map, Field field,InputStream in) throws IOException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,ClassNotSameException,BuilderNotFoundException{
//		if(map == null){
//			throw new IllegalArgumentException("map can't be null");
//		}
//		map.put(field.getName(),this.readValue(field.getType(),in));
//		return map;
//	}


	/**
	 * 读取值，将值存入指定的对象中,如果obj为空,则将读取的值存入Map
	 * @param obj    需要读入值的对象
	 * @param objectClass  要读取对象的类型
	 * @param context 序列化上下文
	 * @param in  包含序列化数据的输入流
	 * @return  存储了读取值的对象
	 */
	@Override
	protected Object readValue(Object obj,Class objectClass,Context context,ByteBuf in) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,BuilderNotFoundException{
		//存放字段的值，key为字段名称
		Map<String,Object> valueMap = new HashMap<>();
		Map<String,Object> currentMap = valueMap;
		List<Class> selfAndSuperList = ReflectUtil.getSelfAndSuperClass(objectClass);
		int count = 0;
		for(Class currentType : selfAndSuperList){
			Field[] fields = ReflectUtil.getAllInstanceField(currentType,isCacheField);
			short fieldCount = this.readFieldCount(in);
			if(fieldCount != fields.length){
				throw new ClassNotSameException("属性个数不一致");
			}

			//循环读取属性
			if(obj == null){
				for(int i = 0; i < fieldCount; i++){
					context.setCurrentField(fields[i]);
					this.readField(currentMap, fields[i],in,context);
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
					this.readField(obj,objectClass,fields[i],in,context);
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

}
