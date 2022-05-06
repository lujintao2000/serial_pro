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
import com.tuling.serialize.util.*;
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


	/**
	 * 读取值，将值存入指定的对象中,如果obj为空,则将读取的值存入Map
	 * @param obj    需要读入值的对象
	 * @param context 序列化上下文
	 * @param in  包含序列化数据的输入流
	 * @return  存储了读取值的对象
	 */
	@Override
	protected Object readValue(Object obj,Context context,ByteBuf in) throws IOException,ClassNotSameException,ClassNotFoundException,InvalidDataFormatException,InvalidAccessException,BuilderNotFoundException{
		List<List<Field>> allFields = ReflectUtil.getAllFields(obj.getClass());
		for(int i = 0 ; i < allFields.size(); i++){
			List<Field> fields = allFields.get(i);
			for(int j = 0; j < fields.size(); j++){
				Field field = fields.get(j);
				context.setCurrentField(fields.get(j));
				try {
					field.set(obj,this.readValue(field.getType(),in,context, SituationEnum.POSSIBLE_BE) );

				} catch (IllegalArgumentException e) {
					LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
					throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
				} catch (IllegalAccessException e) {
					LOGGER.error(e.getCause() + "|field:" + field.getName(), e);
					throw new InvalidAccessException(e.getCause() + "|field:" + field.getName(), e);
				}
			}

		}
		context.setCurrentField(null);
		return obj;

	}

}
