package com.tuling.serialize.util;

/**
 * Created by Administrator on 2020-06-12.
 */
public class Constant {

    //代表写对象开始
    public static final int BEGIN_FLAG = 255;
    //代表写对象结束
    public static final int END_FLAG = 0;
    //代表写入了NULL
    public static final int NULL_FLAG = 254;

    //代表写入了非空
    public static final int NOT_NULL_FLAG = 253;
    //代表写入了CONTINUE,循环还要进行
    public static final int CONTINUE_FLAG = 252;
    //代表当前要写入的值是之前已经写入流中的对象的引用
    public static final int REFERENCE_FLAG = 251;
    //代表当前要写入的值是完整信息，并非之前已经写入流中的对象的引用
    public static final int NORMAL_FLAG = 250;

}
