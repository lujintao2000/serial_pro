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
    private static final Map<String, Class> baseTypeNameMap = new HashMap<>();
    private static final Map<Class, Class> baseTypeMap = new HashMap<>();
    //该集合用于存放类的未排序字段
    private static Map<Class, Field[]> fieldMap = new HashMap<>();
    //该集合用于存放类的已排序字段
    private static Map<Class, Field[]> orderedFieldMap = new HashMap<>();
    //存放类信息，key为类名
    private static Map<String, Class> classMap = new HashMap<>();
    //存储序列化对象所属类的父类信息
    private static Map<Class,List<Class>> superClassMap = new HashMap<>();

    static {
        Arrays.stream(BaseTypeEnum.values()).forEach(x -> baseTypeNameMap.put(x.getValue(), x.getType()));
        Arrays.stream(BaseTypeEnum.values()).forEach(x -> baseTypeMap.put(x.getType(), x.getType()));
        baseTypeMap.put(boolean.class,boolean.class);
        baseTypeMap.put(byte.class,byte.class);
        baseTypeMap.put(char.class,char.class);
        baseTypeMap.put(short.class,short.class);
        baseTypeMap.put(int.class,int.class);
        baseTypeMap.put(long.class, long.class);
        baseTypeMap.put(float.class, float.class);
        baseTypeMap.put(double.class, double.class);
        baseTypeMap.put(Void.class,Void.class);
        classMap.put("boolean", boolean.class);
        classMap.put("byte", byte.class);
        classMap.put("char", char.class);
        classMap.put("short", short.class);
        classMap.put("int", int.class);
        classMap.put("long", long.class);
        classMap.put("float", float.class);
        classMap.put("double", double.class);

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
    public static Field[] getAllInstanceFieldNoOrder(Class targetClass, boolean isCacheField) {
        Field[] result = null;
        if (isCacheField) {
            if (fieldMap.containsKey(targetClass)) {
                result = fieldMap.get(targetClass);
            } else {
                result = getFields(targetClass);
                fieldMap.put(targetClass, result);
            }
        } else {
            result = getFields(targetClass);
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
    public static Field[] getAllInstanceField(Class targetClass, boolean needOrder, boolean isCacheField) {
        Field[] result = null;
        if (needOrder) {
            if (isCacheField) {
                if (orderedFieldMap.containsKey(targetClass)) {
                    return orderedFieldMap.get(targetClass);
                } else {
                    result = getFields(targetClass);
                    orderedFieldMap.put(targetClass, result);
                }
            } else {
                return getFields(targetClass);
            }
        } else {
            result = getAllInstanceFieldNoOrder(targetClass, isCacheField);
        }
        return result;
    }

    /**
     * 获得指定类的所有实例字段,包括父类的字段，数组中排在前的是当前类的字段，然后是父类的字段
     *
     * @param type
     * @return
     */
    private static Field[] getFields(Class type) {
        Field[] fields = Arrays.asList(type.getDeclaredFields())
                .stream()
                .filter(x -> !Modifier.isStatic(x.getModifiers()))
                .collect(Collectors.toList()).toArray(new Field[0]);
        List<Field> list = Arrays.asList(fields);
        list.forEach(x -> x.setAccessible(true));
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
        return baseTypeMap.containsKey(targetClass);
    }

    /**
     * 判断指定的类名是否是原类名的简写。这里要注意：基本数据类型的包装类在序列化流中是用标记字符表示,请参阅BaseTypeEnum
     *
     * @param className 类名
     * @return 如果指定的类名是否是原类名的简写，返回true;否则，返回false
     */
    public static boolean isShortName(String className) {
        return baseTypeNameMap.containsKey(className);
    }

    /**
     * 获取与指定名称相关的Class对象
     *
     * @param className
     * @return
     */
    public static Class get(String className) throws ClassNotFoundException {
        switch (className) {
            case "boolean":
                return boolean.class;
            case "byte":
                return byte.class;
            case "char":
                return char.class;
            case "short":
                return short.class;
            case "int":
                return int.class;
            case "long":
                return long.class;
            case "float":
                return float.class;
            case "double":
                return double.class;
            default:
                return getComplexClass(className);
        }
//        if(className == null){
//            throw new IllegalArgumentException("className can't be empty");
//        }
//        Class result = classMap.get(className);
//        if(result == null){
//            result = Class.forName(className);
//            classMap.put(className, result);
//        }
//        return result;
    }

    /**
     * 获得非基本类型的Class对象
     * @param className
     * @return
     * @throws ClassNotFoundException
     */
    public static Class getComplexClass(String className) throws ClassNotFoundException{
        Class result = classMap.get(className);
        if (result == null) {
            result = Class.forName(className);
            classMap.put(className, result);
        }
        return result;
    }

    /**
     * 根据类的简称，获取类的全名
     *
     * @param shortNameForClass 类的简称
     * @return
     */
    public static String getFullName(String shortNameForClass) {
        if (isShortName(shortNameForClass)) {
            return baseTypeNameMap.get(shortNameForClass).getTypeName();
        } else {
            return shortNameForClass;
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

    /**
     * 获得一个集合，这个集合中装有这个类自己和它的所有父类,自己放在第一个元素位置，最近的父类放在第二个元素位置，越近的父类放的位置越靠前
     * @param target
     * @return
     */
    public static List<Class> getSelfAndSuperClass(Class targetClass){
        if(targetClass == null){
            throw new IllegalArgumentException("param targetClass can't be null");
        }
        List<Class> result = superClassMap.get(targetClass);
        if(result == null){
            result = new ArrayList<>();
            result.add(targetClass);
            Class currentType = targetClass.getSuperclass();
            while(currentType != null && currentType != Object.class){
                result.add(currentType);
                currentType = currentType.getSuperclass();
            }
            superClassMap.put(targetClass,result);
        }
        return result;
    }
}
