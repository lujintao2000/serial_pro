package com.tuling.serialize;

/**
 * 该类的对象主要是用来存放Map中的key,value
 * Created by lujintao on 2020-06-17.
 */
public class Entry {

    private Object key;

    private Object value;

    public Entry(Object key,Object value){
        this.key = key;
        this.value = value;
    }

    public Object getKey() {
        return key;
    }

    public void setKey(Object key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
