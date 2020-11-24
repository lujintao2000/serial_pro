package com.tuling.serialize;

import com.tuling.serialize.util.IdGenerator;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 该类保存了对象序列化时的相关信息，如对某个对象进行序列化时，相关的哪些对象也正在进行序列化
 * @author  lujintao
 * @date 2020-06-07
 */
public class Context {

    private static final IdGenerator idGenerator = new IdGenerator();
    //存储当前所有的context对象,键为context的id
    private static Map<Integer, Context> contextMap = new ConcurrentHashMap<>();
    //表示在序列化时，当前与该context相关的对象的个数
    private Integer counter = 0;
//    private Map<Class,List> map = new HashMap<>();

    private List list = new ArrayList();

    //表示在一次序列化的过程中，已经往流中写入类名的类的集合
    private List<Class> classList = new ArrayList<>();

    //表示在一次反序列化的过程中，已经读取过的类的集合
    private List<Class> hasReadClassList = new ArrayList<>();
    //表示当前正在读取或写入的字段
    private Field currentField = null;
    //标识
    private Integer  id;
    //当前要读取的值类型是否是枚举类型
    private boolean isEnum;

    public Context(){
        id = idGenerator.getId();
    }

    /**
     * 创建一个新的context对象
     * @return
     */
    public static Context create(){
        Context context = new Context();
        contextMap.put(context.getId(), context);
        return context;
    }

    /**
     * 销毁当前context
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
     * 序列化的时候添加新类
     * @param item
     */
    public void addClass(Class item){
        this.classList.add(item);
    }

    /**
     * 判断指定的类是否已存在于序列化上下文
     * @param target
     * @return
     */
    public boolean contains(Class target){
        return classList.contains(target);
    }

    /**
     * 获取指定类在序列化上下文已写入类集合中的序号
     * @param target
     * @return
     */
    public int getIndex(Class target){
        return classList.indexOf(target);
    }

    /**
     * 往已读取类集合中添加一个新类,方法调用发生在反序列化
     * @param type
     */
    public void addReadClass(Class type){
        if(!hasReadClassList.contains(type)){
            hasReadClassList.add(type);
        }
    }

    /**
     * 从已读取类名集合中获取指定序号对应的类
     * @param index 类的序号
     * @return
     */
    public Class getClass(int index){
        return hasReadClassList.get(index);
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
//            List list = map.get(obj.getClass());
//            if(list == null){
//                list = new ArrayList();
//                map.put(obj.getClass(),list);
//            }
            list.add(obj);
        }
    }

    /**
     * 判断当前上下文是否包含指定对象；
     * @param obj
     * @return   包含返回true,否则返回false
     */
    public boolean contains(Object obj){
        return obj == null ? false : list.contains(obj);
    }

    /**
     * 获得指定对象在序列上下文存储同类型元素集合中的序号,如果不存在，返回-1
     * @param obj
     * @return  对象在序列上下文存储同类型元素集合中的序号
     */
    public int getIndex(Object obj){
        int result = -1;
        if(obj != null){
            for(int i = 0; i < list.size(); i++){
                if(list.get(i) == obj){
                    result = i;
                }
            }
        }
        return result;
    }

    /**
     * 从反序列化上下文中获取指定类型、指定引用序号的元素
     * @param index  要获取对象在集合中的序号
     * @return
     */
    public Object get(int index){
        return list.get(index);
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
