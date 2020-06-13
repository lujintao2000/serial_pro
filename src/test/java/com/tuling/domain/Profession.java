package com.tuling.domain;

/**
 * Created by Administrator on 2020-06-13.
 */
public class Profession {
    private String name;

    public Profession(){

    }

    public Profession(String name){
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Profession){
            if((this.name == null && ((Profession)obj).getName() == null) || (this.name != null && this.name.equals(((Profession)obj).getName()))){
                return true;
            }
        }
        return false;
    }
}
