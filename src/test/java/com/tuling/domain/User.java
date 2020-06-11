package com.tuling.domain;

import java.io.Serializable;

public class User extends Person implements Serializable{

    private String name;
    private Integer age;
    private Company company = null;


    public User(){
        super(null, 0.0f);
    }

    public User(String name){
        super(null,0.0f);
        this.name =  name;
    }

    public User(String name,Integer age){
        this(name,age,170.0f, 72.0f);
    }

    public User(String name,Integer age,Float height,float weight){
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

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCompany(Company company){
        this.company = company;
        this.company.setUser(this);
    }

    public Company getCompany(){
        return this.company;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  User){
            User another = (User)obj;
            if(super.equals(obj)){
                if((this.name == another.getName() || (this.name != null && this.name.equals(another.getName()))) && this.age == another.getAge()
                        && (this.company == another.getCompany() || (this.company != null && another.getCompany() != null && this.company.getName().equals(another.getCompany().getName())) )){
                   return  true;
                }
            }
        }
        return false;
    }
}
