package com.tuling.serialize.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author ljt
 * @date 2020-06-25
 */
public class IdGenerator {
    private final AtomicInteger counter;

    public IdGenerator(){
        counter = new AtomicInteger(1);
    }

    public IdGenerator(int initial){
        counter = new AtomicInteger(initial);
    }

    /**
     * 获得一个ID
     * @return
     */
    public  int getId(){
        return counter.getAndIncrement();
    }

}
