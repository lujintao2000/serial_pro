package com.tuling.serialize;

import java.util.Map;

/**
 * 该接口中定义了一个根据参数创建特定类型对象的方法
 * 约定：该接口的实现类要提供一个无参的构造方法
 * @author  lujintao
 * @date    2020-06-16
 */
public interface Builder<T> {
    //当读取属性值，并将属性值放入MAP中时，会用到该值，它表示这里面存的是当前对象所属类的父类的属性值
    public static final String NEXT = "next";
    //反序列化集合或数组对象时，存储元素的集合对应的key为LIST,value的类型为List
    public static final String LIST = "list";

    /**
     * 根据参数集合创建特定类型的对象
     * @param paramMap  该map 中的key为类的属性名，value为属性对应的值；如果该类有父类，则父类相关的属性存放在key为next的map中
     *                    如果父类还有父类，以此类推
     * @return
     */
    public T create(Map<String,Object> paramMap);


    /**
     * 获得要创建的对象的类型
     * @return
     */
    public Class getType();
}
