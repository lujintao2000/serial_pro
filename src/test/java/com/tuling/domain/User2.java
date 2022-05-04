package com.tuling.domain;

/**
 * Created by ljt on 2022/5/1.
 */
public class User2  extends Person{

    private String name;

    public User2(String name,boolean sex,float height,float weight){
        super(sex,height,weight);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
