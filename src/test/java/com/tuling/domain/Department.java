package com.tuling.domain;

/**
 * Created by Administrator on 2020-06-13.
 */
public class Department {

    public Department(){

    }

    public Department(String name){
        this.name = name;
    }

    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Department){
            if((this.name == null && ((Department)obj).getName() == null) || (this.name != null && this.name.equals(((Department)obj).getName()))){
                return true;
            }
        }
        return false;
    }
}
