package com.tuling.serialize.util;

/**
 * @author lujintao
 * @date 2020-12-14
 * 该类定义了一个获取应用程序ID的方法,程序的每一次运行都会生成不同的ID
 */
public class ApplicationIdGenerator {

    private static Long applicationId;

    /**
     * 获取ID
     * @return
     */
    public static Long getId(){
        if(applicationId == null){
            synchronized (String.class){
                if(applicationId == null){
                    applicationId = System.nanoTime();
                }
            }
        }
        return applicationId;
    }

}
