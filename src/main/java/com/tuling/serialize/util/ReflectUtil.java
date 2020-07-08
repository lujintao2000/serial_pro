package com.tuling.serialize.util;

import com.sun.org.apache.xpath.internal.operations.Bool;
import com.tuling.serialize.BaseTypeEnum;
import com.tuling.serialize.DefaultObjectInputStream;
import org.apache.log4j.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 定义了与反射相关的一些工具方法
 *
 * @author lujintao
 * @date 2017-04-20
 */
public class ReflectUtil {
    //表示普通类型,即数组、枚举、基本类型之外的类型
    public static final int GENERAL = 0;
    //标识数组类型
    public static final int ARRAY = 1;
    //标识枚举类型
    public static final int ENUM = 2;
    //标识基本类型(包含String)
    public static final int BASETYPE = 3;

    private static final Logger LOGGER = Logger.getLogger(ReflectUtil.class);

    private static final Map<String, Class> baseTypeNameMap = new HashMap<>();
    private static final Map<Class, Class> baseTypeMap = new HashMap<>();

    //该集合用于存放类的未排序字段
    private static final Map<Class,Map<String,Field>> fieldMap = new ConcurrentHashMap<>();
    //该集合用于存放类的已排序字段
    private static final Map<Class, Field[]> orderedFieldMap = new ConcurrentHashMap<>();
    //存放类信息，key为类名
    private static final Map<String, Class> classMap = new ConcurrentHashMap<>();
    //存储序列化对象所属类的父类信息
    private static final Map<Class,List<Class>> superClassMap = new ConcurrentHashMap<>();
    //存储类的标识信息，标识类是数组还是枚举，还是普通类
    private static final Map<Class,Integer> typeMap = new ConcurrentHashMap<>();
    //存储每个类的最适合创建对象的构造方法
    private static final Map<Class,Constructor> constructorMap = new ConcurrentHashMap<>();

    //存储构造方法的默认参数值
    private static final Map<Constructor,Object[]> parameterValueMap = new ConcurrentHashMap<>();

    //存储不包含无参公共构造方法的类
    private static final Map<Class,Boolean> classWithoutZeroConstructorMap = new ConcurrentHashMap<>();
    //存储不能够通过程序调用构造方法创建对象的类
    private static final Map<Class,Boolean> cannotCreateObjectMap = new ConcurrentHashMap<>();
    //存储每一种类型的默认值
    private static final Map<Class,Object> defaultValueMap = new HashMap<>();

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

        defaultValueMap.put(boolean.class,true);
        defaultValueMap.put(Boolean.class,true);
        defaultValueMap.put(byte.class,(byte)0);
        defaultValueMap.put(Byte.class,(byte)0);
        defaultValueMap.put(char.class,(char)0);
        defaultValueMap.put(Character.class,(char)0);
        defaultValueMap.put(short.class,(short)0);
        defaultValueMap.put(Short.class,(short)0);
        defaultValueMap.put(int.class,0);
        defaultValueMap.put(Integer.class,0);
        defaultValueMap.put(long.class, 0L);
        defaultValueMap.put(Long.class, 0L);
        defaultValueMap.put(float.class, 0.0f);
        defaultValueMap.put(Float.class, 0.0f);
        defaultValueMap.put(double.class, 0.0d);
        defaultValueMap.put(Double.class, 0.0d);
        defaultValueMap.put(String.class, "");

    }

    /**
     * 获取类的分类信息,看它是数组，还是枚举，还是基本类型或是普通类型
     * 0表示普通类型，1表示数组类型，2表示枚举类型，3表示基本类型（包括String）
     * @param target
     * @return
     */
    public static int getTypeOfClass(Class target){
        if(target == null){
            throw new IllegalArgumentException("target cat't be null");
        }
        Integer result = typeMap.get(target);

        if(result == null){
            if(target.isArray()){
                result = ARRAY;
            }else if(target.isEnum()){
                result = ENUM;
            }else if(isBaseType(target)){
                result = BASETYPE;
            }else{
                result = GENERAL;
            }
            typeMap.put(target,result);
        }
        return result;
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
                result = fieldMap.get(targetClass).values().toArray(new Field[0]);
            } else {
                result = getFields(targetClass,false);
                Map<String,Field> map = new HashMap<>();
                for(Field item : result){
                    map.put(item.getName(),item);
                }
                fieldMap.put(targetClass, map);
            }
        } else {
            result = getFields(targetClass,false);
        }
        return result;
    }


    /**
     * 获得一个类上的所有实例字段(包括其父类的)
     *
     * @param targetClass
     * @param isCacheField   字段是否需要缓存
     * @return
     */
    public static Field[] getAllInstanceField(Class targetClass,  boolean isCacheField) {
        Field[] result = null;
        if (isCacheField) {
            if (orderedFieldMap.containsKey(targetClass)) {
                result = orderedFieldMap.get(targetClass);
            } else {
                 result = getFields(targetClass,true);
                orderedFieldMap.put(targetClass, result);
            }
        } else {
            result = getFields(targetClass,true);
        }
        return result;
    }

    /**
     * 获得指定类的所有实例字段,包括父类的字段，数组中排在前的是当前类的字段，然后是父类的字段
     *
     * @param type
     * @param needOrder 是否需要按属性名排序
     * @return
     */
    private static Field[] getFields(Class type,boolean needOrder) {
        List<Field> fields = Arrays.asList(type.getDeclaredFields())
                .stream()
                .filter(x -> !Modifier.isStatic(x.getModifiers()))
                .map(x -> {
                    x.setAccessible(true);
                    return x;
                })
                .sorted((x,y) -> x.getName().compareTo(y.getName()))
                .collect(Collectors.toList());
        if(!fieldMap.containsKey(type)){
            Map<String,Field> map = new HashMap<>();
            fields.parallelStream().forEach( x -> map.put(x.getName(),x) );
            fieldMap.put(type,map);
        }

        return  fields.toArray(new Field[fields.size()]);
    }

    /**
     * 获取指定类指定名称的字段；如果对应的字段不存在，就返回null
     * @param type   要获取字段所属的类
     * @param name   字段名称
     * @return  与属性名称对应的字段
     */
    public static Field getField(Class type,String name){
        Field result = null;
        if(fieldMap.containsKey(type)){
            result = fieldMap.get(type).get(name);
        }else{
            try {
                result = type.getDeclaredField(name);
            }catch (NoSuchFieldException ex){
                LOGGER.error(ex.getMessage(),ex);
            }
        }
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
            if(className.endsWith("[]")){
                result = Array.newInstance(get(className.substring(0,className.length() - 2)) ,0).getClass();
            }else{
                result = Class.forName(className);
            }
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
        Class temp = baseTypeNameMap.get(shortNameForClass);
        if (temp != null) {
            return temp.getTypeName();
        } else {
            return shortNameForClass;
        }
    }

    /**
     * 获得指定数据类型的标识字符串,如未找到，返回null,只有BaseTypeEnum中定义的类型才有标识字符串
     *
     * @param type
     * @return 数据类型的标识字符串
     */
    public static String getFlagOfBaseType(Class type) {
//        if (isBaseType(type)) {
//            return BaseTypeEnum.get(type).getValue();
//        } else {
//            return type.getTypeName();
//        }
        BaseTypeEnum baseTypeEnum = BaseTypeEnum.get(type);
        if(baseTypeEnum != null){
            return baseTypeEnum.getValue();
        }else{
            return null;
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

    /**
     * 获取指定类最适合创建对象的构造方法，即参数最少的构造方法
     * @param objectClass
     * @return
     */
    public static Constructor getProperConstructor(Class objectClass){
        if(objectClass == null){
            throw new IllegalArgumentException("The value of param named objectClass can't be null");
        }
        Constructor result = constructorMap.get(objectClass);
        if(result == null){
            result = getProperConstructor(objectClass.getDeclaredConstructors());
            if(!result.isAccessible()){
                result.setAccessible(true);
            }
            constructorMap.put(objectClass,result);
        }
        return result;
    }

    /**
     * 获得指定构造方法的默认参数值
     * @param constructor
     * @return
     */
    public static Object[] getDefaultParameterValue(Constructor constructor){
        Object[] result = parameterValueMap.get(constructor);
        if(result == null){
            Class[] paramTypes = constructor.getParameterTypes();
            result = new Object[paramTypes.length];
            for(int i = 0; i < result.length; i++){
                result[i] = getDefaultValue(paramTypes[i]);
            }
            parameterValueMap.put(constructor,result);
        }
        return  result;
    }

    /**
     * 判断指定类是否包含无参公共构造方法
     * @param type
     * @return
     */
    public static boolean containZeroConstructor(Class type){
        return !classWithoutZeroConstructorMap.containsKey(type);
    }

    /**
     * 添加没有无参构造公共方法的类
     * @param type
     */
    public static void addClassWithoutZeroConstructor(Class type){
        classWithoutZeroConstructorMap.put(type,true);
    }

    /**
     * 创建一个objectClass类型的对象
     * @param objectClass
     * @return
     */
    public static  Object createObject(Class objectClass){
        Object result = null;
        if(!cannotCreateObjectMap.containsKey(objectClass)){
            //是否通过调用无参构造方法之外的其它构造方法来创建对象
            boolean isCreateWithAnother = false;
            try{
                if(containZeroConstructor(objectClass)){
                    result = objectClass.newInstance();
                }else{
                    isCreateWithAnother = true;
                }
            }catch (Exception ex){
                addClassWithoutZeroConstructor(objectClass);
                isCreateWithAnother = true;

            }
            if(isCreateWithAnother){
                try {
                    Constructor constructor = ReflectUtil.getProperConstructor(objectClass);
                    result = constructor.newInstance(ReflectUtil.getDefaultParameterValue(constructor));
                }catch (Exception ex2){
                    LOGGER.error("通过反射调用构造方法创建对象失败|" + ex2.getMessage(),ex2);
                }
            }
        }
        if(result == null){
            cannotCreateObjectMap.put(objectClass,true);
        }
        return  result;
    }

    /**
     * 从指定的构造方法列表中寻找到参数最少的构造方法
     * @param constructors
     * @return
     */
    private static Constructor getProperConstructor(Constructor[] constructors){
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
     * 获取指定类型参数对应的默认值
     * @param type
     * @return
     */
    private static Object getDefaultValue(Class type){
        if(ReflectUtil.isBaseType(type)){
//            return defaultValueMap.get(type);
            //加缓存，通过缓存判断
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

    /**
     * 用简易的方式创建对象,即调用无参构造方法创建，如果创建失败，返回null
     * @return
     */
    private static Object createObjectWithSimple(Class type){
        Object result = null;
        try {
            if(containZeroConstructor(type)){
                result =  type.newInstance();
            }
        }catch (Exception ex){
            addClassWithoutZeroConstructor(type);
        }
        return  result;
    }
}
