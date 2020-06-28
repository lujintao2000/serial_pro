package com.tuling.serialize;

import com.tuling.serialize.util.IdGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类保存了对象序列化时的相关信息，如对某个对象进行序列化时，相关的哪些对象也正在进行序列化
 * @author  lujintao
 * @date 2020-06-07
 */
public class Context {
    //存储当前所有的context对象,键为context的id
    private static Map<Integer, Context> contextMap = new ConcurrentHashMap<>();
    //表示在序列化时，当前与该context相关的对象的个数
    private Integer counter = 0;
    private Map<Class,List> map = new HashMap<>();
    //表示在一次序列化的过程中，已经往流中写入类名的类的集合
    private List<Class> classList = new ArrayList<>();

    //表示在一次反序列化的过程中，已经读取过完整类名的类的集合
    private List<String> hasReadClassNameList = new ArrayList<>();
    //表示当前正在读取或写入的字段
    private Field currentField = null;
    //标识
    private Integer  id;

    public Context(){
        id = IdGenerator.getId();
    }

    /**
     * 获取一个新的context对象
     * @param id
     * @return
     */
    public static Context create(){
        Context context = new Context();
        contextMap.put(context.getId(), context);
        return context;
    }

    /**
     * 销毁当前context
     * @param id
     */
    public void destory(){
          contextMap.remove(this.id);
    }

    public void enter(){
        counter++;
    }

    public void leave(){
        counter--;
    }

    /**
     * 添加新类
     * @param item
     */
    public void addClass(Class item){
        this.classList.add(item);
    }

    /**
     * 判断指定的类是否已存在于上下文中
     * @param target
     * @return
     */
    public boolean contains(Class target){
        return classList.contains(target);
    }

    /**
     * 获取指定类在上下文已写入类集合中的序号
     * @param target
     * @return
     */
    public int getIndex(Class target){
        return classList.indexOf(target);
    }

    /**
     * 往已读取类名集合中添加一个新类名
     * @param item
     */
    public void addClassName(String className){
        if(!hasReadClassNameList.contains(className)){
            hasReadClassNameList.add(className);
        }
    }

    /**
     * 从已读取类名集合中获取指定序号对应的类名
     * @param int index
     * @return
     */
    public String getClassName(int index){
        return hasReadClassNameList.get(index);
    }

    /**
     * 根据序号获取与之对应的类
     * @param index
     * @return
     */
    public Class getClassByIndex(int index){
        return classList.get(index);
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

    public Field getCurrentField() {
        return currentField;
    }

    public void setCurrentField(Field currentField) {
        this.currentField = currentField;
    }

    public int getId() {
        return id;
    }
}
