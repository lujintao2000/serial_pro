package com.tuling.domain;

import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 *
 * @author  lujintao
 * @date    2020-06-16
 */
@Message
public class Department  implements Serializable {


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

    @Override
    public int hashCode(){
        int result = 1;
        if(this.name != null){
            result *= this.name.hashCode();
        }
        return result;
    }
}
