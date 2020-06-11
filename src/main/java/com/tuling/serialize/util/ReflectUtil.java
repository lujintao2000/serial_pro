package com.tuling.serialize.util;

import com.tuling.serialize.BaseTypeEnum;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 定义了与反射相关的一些工具方法
 *
 * @author lujintao
 * @date 2017-04-20
 */
public class ReflectUtil {
    private static Map<String, Class> baseTypeMap = new HashMap<>();
    //该集合用于存放类的未排序字段
    private static Map<Class, Field[]> fieldMap = new HashMap<>();
    //该集合用于存放类的已排序字段
    private static Map<Class, Field[]> orderedFieldMap = new HashMap<>();

    static {
        Arrays.stream(BaseTypeEnum.values()).forEach(x -> baseTypeMap.put(x.getValue(), x.getType()));
    }

    /**
     * 获得指定类的所有字段，将这些字段放入一个栈中。先压入该类自己定义的字段，然后依次压入上一级父类定义的字段
     *
     * @param target
     * @return 包含所有字段的栈
     */
    public static Stack<Field> getAllFields(Class targetClass) {
        Stack<Field> stack = new Stack<Field>();
        while (targetClass != null) {
            putField(targetClass, stack);
            targetClass = targetClass.getSuperclass();
        }

        return stack;
    }

    /**
     * 将param 指定类定义的所有实例字段(不包括父类的字段)压入栈中
     *
     * @param param
     * @param stack
     */
    private static void putField(Class targetClass, Stack stack) {
        Arrays.asList(targetClass.getDeclaredFields())
                .stream()
                .filter(x -> !Modifier.isStatic(x.getModifiers()))
                .forEach(stack::push);
    }

    /**
     * 获得一个类上的所有实例字段(包括其父类的)
     *
     * @param targetClass
     * @param isCacheField 是否缓存类的字段信息
     * @return
     */
    public static Field[] getAllInstanceField(Class targetClass,boolean isCacheField) {
        Field[] result = null;
        if(isCacheField){
            if (fieldMap.containsKey(targetClass)) {
                result = fieldMap.get(targetClass);
            } else {
                result = getSortedFields(targetClass,false);
                fieldMap.put(targetClass, result);
            }
        }else{
            result = getSortedFields(targetClass,false);
        }
        return result;
    }

    /**
     * 获得一个类上的所有实例字段(包括其父类的)
     *
     * @param targetClass
     * @param needOrder   字段是否需要排序
     * @return
     */
    public static Field[] getAllInstanceField(Class targetClass, boolean needOrder,boolean isCacheField) {
        Field[] result = null;
        if (needOrder) {
            if(isCacheField){
                if (orderedFieldMap.containsKey(targetClass)) {
                    return orderedFieldMap.get(targetClass);
                } else {
                    result = getSortedFields(targetClass, needOrder);
                    orderedFieldMap.put(targetClass, result);
                }
            }else{
                return getSortedFields(targetClass, needOrder);
            }
        } else {
            result = getAllInstanceField(targetClass, isCacheField);
        }
        return result;
    }

    /**
     * 获得指定类的所有实例字段,如果isSort为true,需要按名称对字段排序
     * @param type
     * @param isSort 是否需要排序
     * @return
     */
    private static Field[] getSortedFields(Class type,boolean isSort){
        Field[] fields = Arrays.asList(type.getDeclaredFields())
                .stream()
                .filter(x -> !Modifier.isStatic(x.getModifiers()))
                .collect(Collectors.toList()).toArray(new Field[0]);
        List<Field> list = Arrays.asList(fields);
        if(isSort){
            Collections.sort(list, (x, y) -> x.getName().compareTo(y.getName()));
        }
        Field[] result = list.toArray(new Field[0]);
        return result;
    }


    /**
     * 判断目标类是否是基本类型（基本类型包括原始类型的包装类和String）
     *
     * @param target
     * @return 如果指定目标类是基本类型，返回true;否则，返回false
     */
    public static boolean isBaseType(Class targetClass) {
        return Number.class.isAssignableFrom(targetClass) || targetClass == Boolean.class
                || targetClass == Character.class || targetClass == String.class
                || targetClass == int.class || targetClass == char.class
                || targetClass == byte.class || targetClass == boolean.class
                || targetClass == short.class || targetClass == long.class
                || targetClass == float.class || targetClass == double.class;
    }

    /**
     * 判断指定字符串是否是基本数据类型的包装类型。这里要注意：基本数据类型的包装类在序列化流中是用标记字符表示,请参阅BaseTypeEnum
     *
     * @param flagName 标识字符串
     * @return 如果指定字符串是标识基本数据类型的包装类型，返回true;否则，返回false
     */
    public static boolean isBaseType(String flagName) {
        return baseTypeMap.containsKey(flagName);
    }

    /**
     * 获取与指定名称相关的Class对象
     *
     * @param className
     * @return
     */
    public static Class get(String className) throws ClassNotFoundException {
        if (isBaseType(className)) {
            return baseTypeMap.get(className);
        } else {
            return Class.forName(className);
        }
    }

    /**
     * 获得指定数据类型的标识字符串
     *
     * @param type
     * @return 数据类型的标识字符串
     */
    public static String getFlagOfBaseType(Class type) {
        if (isBaseType(type)) {
            return BaseTypeEnum.get(type).getValue();
        } else {
            return type.getTypeName();
        }
    }


}
