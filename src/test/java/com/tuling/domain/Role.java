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

    private String name2;

    private String name3;

    private String name4;

    private String name5;

    private String name6;

    private String name7;

    private String name8;

    private String name9;

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

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public String getName4() {
        return name4;
    }

    public void setName4(String name4) {
        this.name4 = name4;
    }

    public String getName5() {
        return name5;
    }

    public void setName5(String name5) {
        this.name5 = name5;
    }

    public String getName6() {
        return name6;
    }

    public void setName6(String name6) {
        this.name6 = name6;
    }

    public String getName7() {
        return name7;
    }

    public void setName7(String name7) {
        this.name7 = name7;
    }

    public String getName8() {
        return name8;
    }

    public void setName8(String name8) {
        this.name8 = name8;
    }

    public String getName9() {
        return name9;
    }

    public void setName9(String name9) {
        this.name9 = name9;
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
