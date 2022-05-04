package com.tuling.serialize;

import com.tuling.serialize.exception.SerializationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 序列化/反序列化对象的便捷操作入口
 * @author  lujintao
 * @date 2020-06-23
 */
public class Serial {
    //默认的序列化实现
    private static  ObjectOutputStream defaultOutput = new DefaultObjectOutputStream();
    //兼容模式的序列化实现
    private static  ObjectOutputStream compatibleOutput = new CompatibleObjectOutputStream();
    //默认的反序列化实现
    private static  ObjectInputStream defaultInput = new DefaultObjectInputStream();
    //兼容模式的反序列化实现
    private static  ObjectInputStream compatibleInput = new CompatibleObjectInputStream();
    //默认是以非兼容模式进行序列化、反序列化
    private static final boolean IS_Compatible = false;

    /**
     * 以非兼容模式序列化指定对象到指定输出流中,序列化的时候会写入当前写入对象obj所属类的类名
     * @param obj   要序列化的对象
     * @param out  序列化数据需要写出的流
     * @throws IOException   遇到IO错误，抛出此异常
     * @throws SerializationException  序列化的时候出错，抛出此异常
     */
    public static void write(Object obj, OutputStream out) throws IOException,SerializationException{
        defaultOutput.write(obj,out);
    }

    /**
     * 以非兼容模式序列化指定对象到指定输出流中,序列化的时候会写入当前写入对象obj所属类的类名
     * @param obj   要序列化的对象
     * @param out  序列化数据需要写出的流
     * @param buffer_size  指定存储对象数据的缓冲数组的初始容量
     * @throws IOException   遇到IO错误，抛出此异常
     * @throws SerializationException  序列化的时候出错，抛出此异常
     */
    public static void write(Object obj, OutputStream out,int buffer_size) throws IOException,SerializationException{
        defaultOutput.write(obj,out,buffer_size);
    }

    /**
     * 以非兼容模式序列化指定对象到指定输出流中
     * @param obj   要序列化的对象
     * @param isWriteClassName  序列化的时候，是否写入对象obj所属类的类名;如果不写入类名，在反序列化的时候，需要提供反序列化对象的类型信息
     * @param out  序列化数据需要写出的流
     * @throws IOException  遇到IO错误，抛出此异常
     * @throws SerializationException  序列化的时候出错，抛出此异常
     */
    public static void write(Object obj,  OutputStream out,boolean isWriteClassName) throws IOException,SerializationException{
        defaultOutput.write(obj,isWriteClassName,out);
    }

    /**
     * 以非兼容模式序列化指定对象到指定输出流中
     * @param obj   要序列化的对象
     * @param isWriteClassName  序列化的时候，是否写入对象obj所属类的类名;如果不写入类名，在反序列化的时候，需要提供反序列化对象的类型信息
     * @param out  序列化数据需要写出的流
     * @param buffer_size  指定存储对象数据的缓冲数组的初始容量
     * @throws IOException  遇到IO错误，抛出此异常
     * @throws SerializationException  序列化的时候出错，抛出此异常
     */
    public static void write(Object obj,  OutputStream out,boolean isWriteClassName,int buffer_size) throws IOException,SerializationException{
        defaultOutput.write(obj,isWriteClassName,out,buffer_size);
    }


    /**
     * 序列化指定对象到指定输出流中
     * @param obj   要序列化的对象
     * @param out  序列化数据需要写出的流
     * @param isWriteClassName  序列化的时候，是否写入对象obj所属类的类名;如果不写入类名，在反序列化的时候，需要提供反序列化对象的类型信息
     * @param isCompatible 序列化的时候，是否开启兼容模式
     * @throws IOException
     * @throws SerializationException
     */
    public static void write(Object obj,  OutputStream out,boolean isWriteClassName,boolean isCompatible) throws IOException,SerializationException{
        ObjectOutputStream output = isCompatible ? compatibleOutput : defaultOutput;
        try {
            output.write(obj,isWriteClassName, out);
        }catch (IOException ex){
            throw  ex;
        }catch (Exception ex){
            throw  new SerializationException(ex);
        }
    }

    /**
     * 序列化指定对象到指定输出流中
     * @param obj   要序列化的对象
     * @param out  序列化数据需要写出的流
     * @param isWriteClassName  序列化的时候，是否写入对象obj所属类的类名;如果不写入类名，在反序列化的时候，需要提供反序列化对象的类型信息
     * @param isCompatible 序列化的时候，是否开启兼容模式
     * @param buffer_size  指定存储对象数据的缓冲数组的初始容量
     * @throws IOException
     * @throws SerializationException
     */
    public static void write(Object obj,  OutputStream out,boolean isWriteClassName,boolean isCompatible,int buffer_size) throws IOException,SerializationException{
       ObjectOutputStream output = isCompatible ? compatibleOutput : defaultOutput;
       try {
           output.write(obj,isWriteClassName, out,buffer_size);
       }catch (IOException ex){
           throw  ex;
       }catch (Exception ex){
           throw  new SerializationException(ex);
       }
    }

    /**
     * 以默认模式从指定输入流读取数据，将数据反序列化为对象
     * @param isCompatible 反序列化的时候，是否开启兼容模式
     * @param in  包含序列化数据的输入流
     * @throws IOException
     * @throws SerializationException
     */
    public static Object read(InputStream in) throws IOException,SerializationException{
       return read(in,null,IS_Compatible);
    }

    /**
     * 从指定输入流读取数据，将数据反序列化为指定类型的对象
     * @param in  包含序列化数据的输入流
     * @param type 要读取对象的类型
     * @throws IOException
     * @throws SerializationException
     */
    public static Object read(InputStream in, Class type) throws IOException,SerializationException{
        return read(in ,type,IS_Compatible);
    }

    /**
     * 从指定输入流读取数据，将数据反序列化为对象
     * @param isCompatible 反序列化的时候，是否开启兼容模式
     * @param in  包含序列化数据的输入流
     * @throws IOException
     * @throws SerializationException
     */
    public static Object read(InputStream in, boolean isCompatible) throws IOException,SerializationException{
        return read(in,null,isCompatible);
    }

    /**
     * 从指定输入流读取数据，将数据反序列化为指定类型的对象
     * @param in  包含序列化数据的输入流
     * @param isCompatible 反序列化的时候，是否开启兼容模式
     * @param type 要读取对象的类型
     * @throws IOException
     * @throws SerializationException
     */
    public static Object read(InputStream in, Class type,boolean isCompatible) throws IOException,SerializationException{
        ObjectInputStream objectInput = isCompatible ? compatibleInput : defaultInput;
        try {
            if(type == null){
                return  objectInput.readObject(in);
            }else{
                return  objectInput.readObject(type,in);
            }

        }catch (IOException ex){
            throw  ex;
        }catch (Exception ex){
            throw  new SerializationException(ex);
        }
    }

}
