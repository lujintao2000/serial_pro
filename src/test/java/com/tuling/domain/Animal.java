package com.tuling.domain;

/**
 * Created by Administrator on 2020-06-08.
 */
public abstract  class Animal {

    private float height;
    private float weight;

    public Animal(float height, float weight){
        this.height = height;
        this.weight = weight;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }



}
