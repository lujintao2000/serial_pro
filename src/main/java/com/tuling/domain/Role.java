package com.tuling.domain;

import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * Created by Administrator on 2020-06-13.
 */
@Message
public class Role  implements Serializable {
    private Role role = null;
    private String name;

    public Role(){

    }

    public Role(String name){
        this.name = name;
    }


//    public String getName() {
//        return name;
//    }

    public void setName(String name) {
        this.name = name;
        role = this;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Role){
            if((this.name == null && ((Role)obj).name == null) || (this.name != null && this.name.equals(((Role)obj).name))){
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
