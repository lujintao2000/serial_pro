package com.tuling.domain;

/**
 * Created by Administrator on 2022/5/1.
 */
public class Role2 {

    private Object age;
    private String name;

    public Role2(){

    }

    public Role2(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getAge() {
        return age;
    }

    public void setAge(Object age) {
        this.age = age;
    }
}
