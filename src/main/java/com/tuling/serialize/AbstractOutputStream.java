package com.tuling.serialize;

import com.tuling.serialize.util.Constant;
import com.tuling.serialize.util.NumberUtil;
import com.tuling.serialize.util.ReflectUtil;
import org.apache.log4j.Logger;
import org.msgpack.io.Output;

import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Created by Administrator on 2020-06-12.
 */
public abstract class AbstractOutputStream implements ObjectOutputStream{

    private static final Logger LOGGER = Logger.getLogger(ObjectOutputStream.class);

    protected static final ThreadLocal<Context> threadLocal = new ThreadLocal();


    //序列化的时候，是否需要对对象属性进行排序，按序写入流中
    private boolean needOrder = false;
    //默认不缓存类的字段信息
    private boolean isCacheField = false;


    public AbstractOutputStream(){
        this(false,false);
    }

    public AbstractOutputStream( boolean needOrder,boolean isCacheField){
        this.needOrder = needOrder;
        this.isCacheField = isCacheField;
    }

    @Override
    public void write(Object obj,OutputStream out) throws IOException{
        write(obj,true,out);
    }

    /**
     * 序列化某个对象
     * @param obj 要序列化的对象
     * @param isWriteClassName  序列化时是否写入对象所属类的类名
     * @throws IOException
     */
    @Override
    public void write(Object obj, boolean isWriteClassName, OutputStream out) throws IOException{
        if(obj == null){
            this.writeNull(out);
        }else{
            this.writeNotNull(out);
            Context context = threadLocal.get();
            if(context == null){
                context = new Context();
                threadLocal.set(context);
            }
            context.enter();
            context.put(obj);
            //1. 写入对象类型
            if(isWriteClassName){
                this.writeClassName(obj.getClass(),out);
            }

            //判断是否是数组类型或集合类型
            if(obj.getClass().isArray()){
                //2.写入元素个数
                int length = obj.getClass().isArray() ? Array.getLength(obj) : ((obj instanceof Collection) ? ((Collection)obj).size() : ((Map)obj).size());
                out.write(NumberUtil.getByteArray(length));
                //3. 循环写入数组中的元素
                if(obj.getClass().isArray()){
                    for(int i = 0; i < length; i++){
                        Object value = Array.get(obj, i);
                        this.writeValue(value, obj.getClass().getComponentType(),out);
                    }
                }
            }else{
                //先写入该类类名及该类自定义的属性，然后写入父类名及父类定义的属性
                Class targetClass = obj.getClass();
                //判断是否是基本数据类型对应的包装类型
                if(ReflectUtil.isBaseType(targetClass)){
                    this.writeValue(obj, targetClass, out);
                }else if(targetClass.isEnum()){
                    this.writeValue(obj.toString(),String.class,out);
                }else{
                    //获得包含该类以及该类的所有父类的集合
                    List<Class> superClassAndSelfList = ReflectUtil.getSelfAndSuperClass(targetClass);
                    for(Class item : superClassAndSelfList){
                        Field[] fields = ReflectUtil.getAllInstanceField(item,needOrder,isCacheField);
                        //2. 写入属性个数  2字节
                        out.write(NumberUtil.getByteArray( ((short)fields.length) ));
                        //3. 循环写入属性
                        for(Field field : fields){
                            context.setCurrentField(field);
                            this.writeField(field, obj,out);
                        }
                        context.setCurrentField(null);
                    }
                }
            }
            context.leave();
            if(context.isFinish()){
                threadLocal.remove();
                out.flush();
            }
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

    /**
     * 将指定类的类名输出到流中
     * @param type
     * @param  out 输出流
     * @throws IOException
     */
    protected abstract void writeClassName(Class type,OutputStream out) throws IOException;


    /**
     * 将指定对象 指定的属性写入输出流
     * @param field
     * @param obj
     * @param out 输出流
     * @throws IOException
     */
    protected abstract void writeField(Field field,Object obj,OutputStream out) throws IOException;

    protected void writeStart(OutputStream out) throws IOException{
        out.write(Constant.BEGIN_FLAG);
    }

    protected void writeEnd(OutputStream out) throws IOException{
        out.write(Constant.END_FLAG);
    }

    protected void writeNull(OutputStream out) throws IOException{
        out.write(Constant.NULL_FLAG);
    }

    protected void writeNotNull(OutputStream out) throws IOException{
        out.write(Constant.NOT_NULL_FLAG);
    }

    protected void writeContine(OutputStream out) throws IOException{
        out.write(Constant.CONTINUE_FLAG);
    }

    /**
     * 写入引用标记
     * @throws IOException
     */
    protected void writeReference(OutputStream out) throws IOException{
        out.write(Constant.REFERENCE_FLAG);
    }

    /**
     * 写入普通标记
     * @throws IOException
     */
    protected void writeNormal(OutputStream out) throws IOException{
        out.write(Constant.NORMAL_FLAG);
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

    /**
     * 写入基本数据类型对应包装类对象的值
     * @param value  要写入的值
     * @param  type 值所属字段的类型
     * @param out 输出流
     * @throws IOException
     */
    protected abstract void writeValue(Object value, Class type,OutputStream out) throws IOException;

}
