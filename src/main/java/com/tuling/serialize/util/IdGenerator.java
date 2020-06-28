package com.tuling.serialize.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ljt
 * @date 2020-06-25
 */
public class IdGenerator {
    private static final AtomicInteger counter = new AtomicInteger(0);

    /**
     * 获得一个ID
     * @return
     */
    public static int getId(){
        return counter.getAndIncrement();
    }

}
