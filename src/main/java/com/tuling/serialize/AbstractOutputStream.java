package com.tuling.serialize;

import com.tuling.serialize.util.Constant;
import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import org.apache.log4j.Logger;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Administrator on 2020-06-12.
 */
public abstract class AbstractOutputStream implements ObjectOutputStream{

    private static final Logger LOGGER = Logger.getLogger(ObjectOutputStream.class);

    protected static final ThreadLocal<Context> threadLocal = new ThreadLocal();

    protected OutputStream out;
    //序列化的时候，是否需要对对象属性进行排序，按序写入流中
    private boolean needOrder = false;
    //默认不缓存类的字段信息
    private boolean isCacheField = false;

    public AbstractOutputStream(OutputStream output){
        this(output,false,false);
    }

    public AbstractOutputStream(OutputStream output, boolean needOrder,boolean isCacheField){
        this.out = output;
        this.needOrder = needOrder;
        this.isCacheField = isCacheField;
    }

    @Override
    public void write(Object obj) throws IOException {
        if(obj == null){
            this.writeNull();
        }else{
            this.writeNotNull();
            //this.writeStart();
            Context context = threadLocal.get();
            if(context == null){
                context = new Context();
                threadLocal.set(context);
            }
            context.enter();
            context.put(obj);
            //1. 写入对象类型
            this.writeClassName(obj.getClass());
            //判断是否是数组类型或集合类型
            if(obj.getClass().isArray() || obj instanceof Collection || obj instanceof Map){
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
                    while(targetClass != null){
                        Field[] fields = ReflectUtil.getAllInstanceField(targetClass,needOrder,isCacheField);
                        //2. 写入属性个数  2字节
                        this.out.write(NumberUtil.getByteArray( ((short)fields.length) ));
                        //3. 循环写入属性
                        for(Field field : fields){
                            this.writeField(field, obj);
                        }
                        targetClass = targetClass.getSuperclass();
//                        if(targetClass != null && targetClass != Object.class){
//                            //this.writeContine();
//                        }else{
//                            break;
//                        }
                        if(targetClass == null || targetClass == Object.class){
                            break;
                        }
                    }
                }
            }
            context.leave();
            if(context.isFinish()){
                threadLocal.remove();
            }
//            this.writeEnd();
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

    protected void writeClassName(Class type) throws IOException{
        if(type == null){
            throw new IllegalArgumentException("type can't be null");
        }
        Context context = threadLocal.get();
        if(context == null || !context.contains(type)){
            String className = "";
            if(ReflectUtil.isBaseType(type)){
                className = ReflectUtil.getFlagOfBaseType(type);
            }else{
                className = type.getTypeName();
            }
            //1. 写入类名长度  2字节
            this.out.write(NumberUtil.getByteArray( ((short)className.getBytes().length) ) );
            //2. 写入类名
            this.out.write(className.getBytes());
            context.addClass(type);
        }else{
            //长度0表示该类的类名之前已写入流中，这里写入的只是对该类名的引用序号
            this.out.write(NumberUtil.getByteArray((short)0));
            this.out.write(NumberUtil.getByteArray((short) context.getIndex(type)));
        }
    }

    /**
     * 将指定对象 指定的属性写入输出流
     * @param field
     * @param obj
     * @throws IOException
     */
    protected abstract void writeField(Field field,Object obj) throws IOException;

    protected void writeStart() throws IOException{
        this.out.write(Constant.BEGIN_FLAG);
    }

    protected void writeEnd() throws IOException{
        this.out.write(Constant.END_FLAG);
    }

    protected void writeNull() throws IOException{
        this.out.write(Constant.NULL_FLAG);
    }

    protected void writeNotNull() throws IOException{
        this.out.write(Constant.NOT_NULL_FLAG);
    }

    protected void writeContine() throws IOException{
        this.out.write(Constant.CONTINUE_FLAG);
    }

    protected void writeReference() throws IOException{
        this.out.write(Constant.REFERENCE_FLAG);
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
     * @param  type 值所属字段的类型
     * @throws IOException
     */
    protected abstract void writeValue(Object value, Class type) throws IOException;

}
