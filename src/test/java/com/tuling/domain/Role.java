package com.tuling.domain;

import io.protostuff.Tag;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.Date;
import java.util.Random;

/**
 * Created by Administrator on 2020-06-13.
 */
@Message
public class Role  implements Serializable {
//    private Role role = null;
    @Tag(1)
    private String name;
    @Tag(2)
    private Integer age;
    @Tag(3)
    private boolean sex;

    public Role(){

    }

    private Role(String name){
        this.name = name;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
//        role = this;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Role){
            if(((this.name == null && ((Role)obj).name == null) || (this.name != null && this.name.equals(((Role)obj).name))) &&
                    (this.age == null &&  (((Role)obj).age == null) || this.age == (((Role)obj).age)) && (this.sex == (((Role)obj).sex))){
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
        if(this.age != null){
            result += this.age.hashCode();
        }
        result += sex ? 20 : 10;
        return result;
    }

    public static Role getInstance(){
        return new Role("");
    }

    public static Role getInstance(String name){
        return new Role(name);
    }
}
