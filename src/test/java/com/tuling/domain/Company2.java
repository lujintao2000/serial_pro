package com.tuling.domain;

import org.msgpack.annotation.Message;

import java.io.Serializable;

@Message
public class Company2 implements Serializable{

    private String name;

    private User2 user;

    public Company2(){

    }

    public Company2(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User2 getUser2() {
        return user;
    }

    public void setUser2(User2 user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Company2){
            if(((this.name == null && ((Company2)obj).getName() == null) || (this.name != null && this.name.equals(((Company2)obj).getName())))
                    && ((this.user == null && ((Company2)obj).getUser2() == null)) || (this.user != null && this.user.equals(((Company2)obj).getUser2()))){
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int result = 1;
        if(this.name != null){
            result *= this.name.hashCode();
        }
        return result;
    }
}
