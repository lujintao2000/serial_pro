package com.tuling.domain;

import java.io.Serializable;

public class User extends Person implements Serializable{

    private String name;
    private int age;
    private Company company = null;


    public User(){
        super(170.0f,72.0f);
    }

    public User(String name){
        super(170.0f,72.0f);
        this.name =  name;
    }

    public User(String name,int age){
        this(name,age,170.0f, 72.0f);
    }

    public User(String name,int age,float height,float weight){
        super(height,weight);
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public void setCompany(Company company){
        this.company = company;
        this.company.setUser(this);
    }

}
