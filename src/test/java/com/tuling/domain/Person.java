package com.tuling.domain;

public  class Person extends  Animal{

    protected Person(Float height, float weight) {
        super(height,weight);
    }

    public void run(){
        System.out.print("I am running");
    }
//    private String name = "";
//    private float height;
//    private float weight;
//
//    protected Person(){
//
//    }
//
//    protected Person(float height, float weight) {
//        this.height = height;
//        this.weight = weight;
//    }
//
//    public float getHeight() {
//        return height;
//    }
//
//    public void setHeight(float height) {
//        this.height = height;
//    }
//
//    public float getWeight() {
//        return weight;
//    }
//
//    public void setWeight(float weight) {
//        this.weight = weight;
//    }
//
//    public String getPersonName() {
//        return name;
//    }

    @Override
    public boolean equals(Object obj){
        return super.equals(obj);
    }
}
