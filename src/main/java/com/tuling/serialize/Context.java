package com.tuling.serialize;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 该类保存了对象序列化时的相关信息，如对某个对象进行序列化时，相关的哪些对象也正在进行序列化
 * @author  lujintao
 * @date 2020-06-07
 */
public class Context {
    //表示在序列化时，当前与该context相关的对象的个数
    private Integer counter = 0;
    private Map<Class,List> map = new HashMap<>();


    public void enter(){
        counter++;
    }

    public void leave(){
        counter--;
    }

    /**
     * 表示会使用到该上下文的序列化工作是否已完成
     * @return
     */
    public boolean isFinish(){
        return counter.intValue() == 0;
    }

    /**
     * 将某个对象放入当前上下文中
     * @param obj
     */
    public void put(Object obj){
        if(obj != null){
            List list = map.get(obj.getClass());
            if(list == null){
                list = new ArrayList();
                map.put(obj.getClass(),list);
            }
            list.add(obj);
        }
    }

    /**
     * 判断当前上下文是否包含指定对象；
     * @param obj
     * @return   包含返回true,否则返回false
     */
    public boolean contains(Object obj){
        return obj == null ? false : map.containsKey(obj.getClass()) && map.get(obj.getClass()).contains(obj);
    }

    /**
     * 获得指定对象在序列上下文存储同类型元素集合中的序号,如果不存在，返回-1
     * @param obj
     * @return  对象在序列上下文存储同类型元素集合中的序号
     */
    public int getIndex(Object obj){
        if(obj == null){
            return -1;
        }
        List list = map.get(obj.getClass());
        if(list != null && list.contains(obj)){
            return list.indexOf(obj);
        }else{
            return -1;
        }
    }

    /**
     * 从反序列化上下文中获取指定类型、指定引用序号的元素
     * @param type   要获取对象的类型
     * @param index  要获取对象在集合中的序号
     * @return
     */
    public Object get(Class type,int index){
        List list = map.get(type);
        if(list != null && list.size() -1 >= index){
            return list.get(index);
        }else{
            return null;
        }
    }
}
