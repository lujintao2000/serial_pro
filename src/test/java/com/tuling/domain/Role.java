package com.tuling.domain;

/**
 * Created by Administrator on 2020-06-13.
 */
public class Role {
    private String name;

    public Role(){

    }

    public Role(String name){
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
        if(obj != null && obj instanceof  Role){
            if((this.name == null && ((Role)obj).getName() == null) || (this.name != null && this.name.equals(((Role)obj).getName()))){
                return true;
            }
        }
        return false;
    }
}
