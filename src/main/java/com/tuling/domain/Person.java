package com.tuling.domain;

public  class Person extends  Animal{
    private boolean sex;

    protected Person(Float height, float weight) {
        this(true,height,weight);
    }

    protected Person(boolean sex,Float height, float weight) {
        super(height,weight);
        this.sex = sex;
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


    public boolean isSex() {
        return sex;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof Person){
            Person another = (Person)obj;
            if(super.equals(obj)){
                if(this.sex == another.sex){
                    return true;
                }
            }
        }

        return  false;
    }

    @Override
    public int hashCode(){
        return super.hashCode();
    }
}
