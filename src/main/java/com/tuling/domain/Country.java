package com.tuling.domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2020-06-14.
 */
public class Country implements Serializable{
    private String name;

    public Country(){

    }

    public Country(String name){
        this.name = name;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Country){
            if((this.name == null && ((Country)obj).getName() == null) || (this.name != null && this.name.equals(((Country)obj).getName()))){
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
