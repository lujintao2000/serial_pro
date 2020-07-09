package com.tuling.serialize;

import java.util.Map;

/**
 * 该接口中定义了一个创建特定类型对象的方法
 * 约定：该接口的实现类要提供一个无参的构造方法
 * @author  lujintao
 * @date    2020-06-16
 */
public interface Builder<T> {
    /**
     * 创建一个特定类的对象
     * @return
     */
    public T newInstance();

}
