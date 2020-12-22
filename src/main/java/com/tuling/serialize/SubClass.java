package com.tuling.serialize;

/**
 * Created by Administrator on 2020-12-07.
 */
public class SubClass {

    public  static  final int VERSION = 20000000;

    static{
        System.out.println("subclass init");
    }


    public int getVersion(){
        return VERSION;
    }
}
