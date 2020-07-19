package com.tuling.domain;

import org.msgpack.annotation.Message;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Message
public class User extends Person implements Comparable<User> {
    private Company company = null;
    private Role role = null;
    private Profession profession = null;
    private Department department = null;
    private Object name;
    private Object age;
    private Nation nation;
//    private Integer id;
//    private Object another;
   private final List<String> labels = new ArrayList<>();

    public User(){

    }

    private User(String name,boolean sex,Float height, float weight){
        super(sex,height,weight);
        this.name = name;
    }


    public User(String name){
        super(null,0.0f);
//        if(name != null && name.length() < 20){
//            throw new IllegalArgumentException();
//        }
        this.name =  name;
    }

    public User(String name,Integer age){
        this(name,age,170.0f, 72.0f);
    }

    public User(String name,Integer age,Float height,float weight){
        super(height,weight);
        this.name = name;
        this.age = age;
    }

    public String getName() {
        return (String)name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return (Integer) age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public void setCompany(Company company){
        this.company = company;
        this.company.setUser(this);
    }

    public Company getCompany(){
        return this.company;
    }

    @Override
    public boolean equals(Object obj){
        if(obj != null && obj instanceof  User){
            User another = (User)obj;
            if(super.equals(obj)){
                if((this.name == another.getName() || (this.name != null && this.name.equals(another.getName())))
                        && (this.age == another.getAge() || (this.age != null && this.age.equals(another.age)))
                        && (this.company == another.getCompany() || (this.company != null && another.getCompany() != null && this.company.getName().equals(another.getCompany().getName())) )
                        && (this.role == another.getRole() || (this.role != null && this.role.equals(another.role)) )
                        && (this.nation == another.nation || (this.nation != null && this.nation.equals(another.nation)))
                        && (this.labels == another.labels || (this.labels != null && this.labels.equals(another.labels)) )
                        && (this.department == another.getDepartment() || (this.department != null && another.getDepartment() != null && this.department.getName().equals(another.getDepartment().getName())) )
                        && (this.profession == another.getProfession() || (this.profession != null && another.getProfession() != null && this.profession.getName().equals(another.getProfession().getName())) )){
                   return  true;
                }
            }
        }
        return false;
    }

    @Override
    public int hashCode(){
        int result = super.hashCode();
        if(company != null){
            result  += company.hashCode();
        }
        if(role != null){
            result += role.hashCode();
        }
        if(department != null){
            result += department.hashCode();
        }
        if(profession != null){
            result += profession.hashCode();
        }
//        if(id != null){
//            result += id.intValue();
//        }
        return result;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Profession getProfession() {
        return profession;
    }

    public void setProfession(Profession profession) {
        this.profession = profession;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    public Nation getNation() {
        return nation;
    }

    public void setNation(Nation nation) {
        this.nation = nation;
    }

    //    public Object getAnother() {
//        return another;
//    }
//
    public void setAnother(Object another) {
//        this.another = another;
    }

    public void addLabel(String label){
        labels.add(label);
    }

    public void setId(Integer id) {
//        this.id = id;
    }

    @Override
    public int compareTo(User another) {
        if(another == null){
            return 1;
        }else{
            return this.hashCode() - another.hashCode();
        }
    }
}
