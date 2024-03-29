package com.tuling.domain;

import io.protostuff.Tag;
import org.msgpack.annotation.Message;

import java.io.Serializable;

/**
 * Created by Administrator on 2020-06-13.
 */
@Message
public class Profession  implements Serializable {
    @Tag(1)
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

    @Override
    public int hashCode(){
        int result = 1;
        if(this.name != null){
            result *= this.name.hashCode();
        }
        return result;
    }
}
