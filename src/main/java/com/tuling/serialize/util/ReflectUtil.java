package com.tuling.serialize.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

/**
 * 定义了与反射相关的一些工具方法
 * @author lujintao
 * @date 2017-04-20
 *
 */
public class ReflectUtil {
	
	
	/**
	 * 获得指定类的所有字段，将这些字段放入一个栈中。先压入该类自己定义的字段，然后依次压入上一级父类定义的字段
	 * @param target
	 * @return 包含所有字段的栈
	 */
	public static Stack<Field> getAllFields(Class targetClass){
		Stack<Field> stack = new Stack<Field>();
		while(targetClass != null){
			putField(targetClass, stack);
			targetClass = targetClass.getSuperclass();
		}
		
		return stack;
	}
	
	/**
	 * 将param 指定类定义的所有实例字段(不包括父类的字段)压入栈中
	 * @param param
	 * @param stack
	 */
	private static void putField(Class targetClass,Stack stack){
		Arrays.asList(targetClass.getDeclaredFields())
				            .stream()
				            .filter(x -> !Modifier.isStatic(x.getModifiers()))
				            .forEach(stack::push);
	}

	/**
	 * 获得一个类上的所有实例字段(包括其父类的)
	 * @param targetClass
	 * @return
	 */
	public static Field[] getAllInstanceField(Class targetClass){
		return 	Arrays.asList(targetClass.getDeclaredFields())
				.stream()
				.filter(x -> !Modifier.isStatic(x.getModifiers()))
				.collect(Collectors.toList()).toArray(new Field[0]);
	}

	/**
	 * 判断目标类是否是基本类型（基本类型包括原始类型的包装类和String）
	 * @param target
	 * @return
	 */
	public static boolean isBaseType(Class targetClass){
		return Number.class.isAssignableFrom(targetClass) || targetClass == Boolean.class
		   || targetClass == Character.class || targetClass == String.class;
	}
}
