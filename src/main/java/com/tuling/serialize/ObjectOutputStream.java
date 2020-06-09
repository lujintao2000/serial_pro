package com.tuling.serialize;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;
import com.tuling.serialize.util.ReflectUtil;
import com.tuling.serialize.util.NumberUtil;
import org.apache.log4j.Logger;

public class ObjectOutputStream {
	private static final Logger LOGGER = Logger.getLogger(ObjectOutputStream.class);
	//代表写对象开始
	public static final int BEGIN_FLAG = 255;
	//代表写对象结束
	public static final int END_FLAG = 0;
	//代表写入了NULL
	public static final int NULL_FLAG = 254;
	//代表写入了CONTINUE,循环还要进行
	public static final int CONTINUE_FLAG = 253;
	//代表当前要写入的值是之前已经写入流中的对象的引用
	public static final int REFERENCE_FLAG = 252;
	//表示数组
	public static final String ARRAY = "Array";

	private static final ThreadLocal<Context> threadLocal = new ThreadLocal();

	private static final ThreadLocal<Integer> counterThreadLocal = new ThreadLocal<>();
	
	private OutputStream out;
	
	
	public ObjectOutputStream(OutputStream output){
		this.out = output;
	}
	
	public void write(Object obj) throws IOException{
		if(obj == null){
			this.writeNull();
		}else{
			this.writeStart();
			Context context = threadLocal.get();
			if(context == null){
				context = new Context();
				threadLocal.set(context);
			}
			context.enter();
			context.put(obj);

			//判断是否是数组类型或集合类型
			if(obj.getClass().isArray() || obj instanceof Collection || obj instanceof Map){
				//1.写入对象类型
				this.writeClassName(obj.getClass().getTypeName());
				//2.写入元素个数
				int length = obj.getClass().isArray() ? Array.getLength(obj) : ((obj instanceof Collection) ? ((Collection)obj).size() : ((Map)obj).size());
				this.out.write(NumberUtil.getByteArray(length));
				//3. 循环写入数组中的元素
				if(obj.getClass().isArray()){
					for(int i = 0; i < length; i++){
						this.write(Array.get(obj, i));
					}
				}else if(obj instanceof Collection){
					for(Object item : (Collection)obj){
						this.write(item);
					}
				}else{
					for(Object item : ((Map)obj).entrySet()){
						Map.Entry entry = (Map.Entry)item;
						this.write(entry.getKey());
						this.write(entry.getValue());
					}
				}

			}else{
				//先写入该类类名及该类自定义的属性，然后写入父类名及父类定义的属性
				Class targetClass = obj.getClass();
				//判断是否是基本数据类型对应的包装类型
				if(ReflectUtil.isBaseType(targetClass)){
					this.writeValue(obj, obj.getClass());
				}else{
					//1. 写入类名
					this.writeClassName(targetClass.getName());
					while(targetClass != null){
						Field[] fields = ReflectUtil.getAllInstanceField(targetClass);
						//2. 写入属性个数  2字节
						this.out.write(NumberUtil.getByteArray( ((short)fields.length) ));
						//3. 循环写入属性
						for(Field field : fields){
							this.writeField(field, obj);
						}
						targetClass = targetClass.getSuperclass();
						if(targetClass != null && targetClass != Object.class){
							this.writeContine();
						}else{
							break;
						}
					}
				}
			}
			context.leave();
			if(context.isFinish()){
				threadLocal.remove();
			}
			this.writeEnd();
		}
	}
	
	/**
	 * 获得指定类的所有字段，将这些字段放入一个栈中。先压入该类自己定义的字段，然后依次压入上一级父类定义的字段
	 * @param target
	 * @return 包含所有字段的栈
	 */
	private Stack<Field> getAllFields(Class targetClass){
		Stack<Field> stack = new Stack<Field>();
		while(targetClass != null){
			this.putField(targetClass, stack);
			targetClass = targetClass.getSuperclass();
		}
		
		return stack;
	}
	
	/**
	 * 将param 指定类自身定义的所有字段(不包括父类的字段)压入栈中
	 * @param param
	 * @param stack
	 */
	private void putField(Class targetClass,Stack stack){
		Field[] fields = targetClass.getDeclaredFields();
		for(Field item : fields){
			stack.push(item);
		}
	}
	
	private void writeClassName(String className) throws IOException{
		//1. 写入类名长度  2字节
		this.out.write(NumberUtil.getByteArray( ((short)className.getBytes().length) ) );
		//2. 写入类名
		this.out.write(className.getBytes());
	}
	
	/**
	 * 将指定对象 指定的属性写入输出流
	 * @param field
	 * @param obj
	 * @throws IOException 
	 */
	private void writeField(Field field,Object obj) throws IOException{
		field.setAccessible(true);
		try {
			Object value = field.get(obj);
			//1. 写入属性名 长度  1 字节      
			this.out.write(field.getName().getBytes().length);
			//2. 写入属性名对应的字节数组
			this.out.write(field.getName().getBytes());
			//3. 写入属性值
			this.writeValue(value, field.getType());
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	private void writeStart() throws IOException{
		this.out.write(BEGIN_FLAG);
	}
	
	private void writeEnd() throws IOException{
		this.out.write(END_FLAG);
	}
	
	private void writeNull() throws IOException{
		this.out.write(NULL_FLAG);
	}
	
	private void writeContine() throws IOException{
		this.out.write(CONTINUE_FLAG);
	}

	private void writeReference() throws IOException{
		this.out.write(REFERENCE_FLAG);
	}

	//将一个short类型整数 转变成对应的字节数组
	private byte[] transfer(short num){
		byte[] result = new byte[2];
		result[0] = (byte)(num / 256);
		result[1] = this.transformNumToByte(num % 256);
		return result;
	}
	
	
	/**
	 * 将一个小于256的整数 转变成对应的byte表示
	 * @param num
	 * @return
	 */
	private byte transformNumToByte(int num){
		return (num > 127) ? (byte)(num - 256) : (byte)num;
	}
	
	public void close(){
		try {
			this.out.close();
		} catch (IOException e) {
			LOGGER.error(e.getCause(), e);
		}
	}

	/**
	 * 写入基本数据类型对应包装类对象的值
	 * @param value
	 * @throws IOException
	 */
	protected void writeValue(Object value, Class type) throws IOException{
		if(value == null){
			this.writeClassName(type.getTypeName());
			this.writeNull();
			return;
		}
		this.writeClassName(value.getClass().getTypeName());

//		if(ReflectUtil.isBaseType(value.getClass())){
//			this.writeClassName(value.getClass().getTypeName());
//		}
		if(value instanceof Boolean){
			this.out.write( ((Boolean)value).equals(Boolean.TRUE) ? 1 : 0 );
		}else if(value instanceof Character){
			this.out.write(NumberUtil.getByteArray( ((Character)value).charValue() ));
		}else if(value instanceof Byte){
			this.out.write(new byte[]{(Byte)value});
		}else if(value instanceof Short){
			this.out.write(NumberUtil.getByteArray((Short)value));
		}else if(value instanceof Integer){
			this.out.write(NumberUtil.getByteArray((Integer)value));
		}else if(value instanceof Long){
			this.out.write(NumberUtil.getByteArray((Long)value));
		}else if(value instanceof Float){
			this.out.write(NumberUtil.getByteArray((Float)value));
		}else if(value instanceof Double){
			this.out.write(NumberUtil.getByteArray((Double)value));
		}else if(value instanceof String){
			//先写入字符串长度，再写入字符串对应的字节
			byte[] bytes = ((String)value).getBytes();
			this.out.write(NumberUtil.getByteArray( bytes.length ));
			this.out.write(bytes);
		}else{
			Context context = threadLocal.get();
			//如果要写入的对象已经在当前序列化上下文中，则只需要写入其引用标识
			if(context != null && context.contains(value)){
				this.writeReference();
				this.out.write(NumberUtil.getByteArray(context.getIndex(value)));
			}else{
				this.write(value);
			}
		}
	}
	
	public static void main(String[] args){
		try {
			ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("e:\\test.txt")));
			System.out.println(out.transformNumToByte(128));
			System.out.println(out.transformNumToByte(255));
			System.out.println(out.transformNumToByte(60));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
