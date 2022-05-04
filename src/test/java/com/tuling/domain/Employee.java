package com.tuling.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ljt
 * @date 2020-11-16
 */
public class Employee{



    private Object role;

    private List<String> labels = new ArrayList<>();

    private Company company;

    private Object name;
    private Object age;

    private Nation nation;

    private Profession profession = null;
    private Department department = null;

    private Float height;
    private float weight;
    private boolean sex;

    private Float another2;
    private float another3;
    private boolean another4;

    private Float another5;
    private float another6;
    private boolean another7;

    public Company getCompany() {
        return company;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    public void setSex(boolean sex) {
        this.sex = sex;
    }

    public void setAnother2(Float another2) {
        this.another2 = another2;
    }

    public void setAnother3(float another3) {
        this.another3 = another3;
    }

    public void setAnother4(boolean another4) {
        this.another4 = another4;
    }

    public void setAnother5(Float another5) {
        this.another5 = another5;
    }

    public void setAnother6(float another6) {
        this.another6 = another6;
    }

    public void setAnother7(boolean another7) {
        this.another7 = another7;
    }

    public void setRole(Role role) {
        this.role = role;
    }


    public void setName(String name) {
        this.name = name;
    }

    public void addLabel(String label){
        labels.add(label);
    }

    public void setName(Object name) {
        this.name = name;
    }

    public void setRole(Object role) {
        this.role = role;
    }

    public void setLabels(List<String> labels) {
        this.labels = labels;
    }

    public void setAge(Object age) {
        this.age = age;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  Employee){
//            if((this.role == null && ((Employee)obj).role == null) || (this.role.equals(((Employee)obj).role))){
//                return true;
//            }
//              if((this.name == null && ((Employee)obj).name == null) || (this.name != null && this.name.equals(((Employee)obj).name))){
//                  return  true;
//              }
        }

        return false;
    }

    @Override
    public int hashCode(){
        int result = 1;
//        if(this.role != null){
//            result *= this.role.hashCode();
//        }
        return result;
    }
}
