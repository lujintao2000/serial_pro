package com.tuling.domain;

import java.io.Serializable;

/**
 * Created by Administrator on 2020-06-08.
 */
 public class Animal implements Serializable {

    private Float height;
    private float weight;

    public Animal(){

    }

     public Animal(Float height, float weight){
        this.height = height;
        this.weight = weight;
    }

    public Animal(Float height, float weight,String name,int age){
        this.height = height;
        this.weight = weight;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof Animal){
            Animal another = (Animal)obj;
            if(((this.height == null && another.height == null) ||
                    (this.height != null && another.height != null && this.height.floatValue() == another.height.floatValue()))
                    && this.weight == another.weight){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    @Override
    public int hashCode(){
        int result = 1;
        if(this.height != null){
            result *= this.height;
        }
        result = (int)(result * this.weight);
        return result;
    }
}
