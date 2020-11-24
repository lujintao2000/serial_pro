package com.tuling.domain;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ljt
 * @date 2020-11-16
 */
public class Employee {

//    private String name;
//
    private Object role;

//    private List<String> labels = new ArrayList<>();

//    private Company company;
//
//    public Company getCompany() {
//        return company;
//    }
//
//    public void setCompany(Company company) {
//        this.company = company;
//    }

//    public Role getRole() {
//        return role;
//    }

    public void setRole(Role role) {
        this.role = role;
    }

//    public String getName() {
//        return name;
//    }
//
    public void setName(String name) {
//        this.name = name;
    }

    public void addLabel(String label){
//        labels.add(label);
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
