package com.tuling.domain;

import org.msgpack.annotation.Message;

@Message
public class User extends Person {
    private Company company = null;
    private Role role = null;
    private Profession profession = null;
    private Department department = null;
    private String name;
    private Integer age;

//    public User(){
//        super(null, 0.0f);
//    }

    public User(String name){
        super(null,0.0f);
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
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
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
                if((this.name == another.getName() || (this.name != null && this.name.equals(another.getName()))) && this.age == another.getAge()
                        && (this.company == another.getCompany() || (this.company != null && another.getCompany() != null && this.company.getName().equals(another.getCompany().getName())) )
                        && (this.role == another.getRole() || (this.role != null && another.getRole() != null && this.role.getName().equals(another.getRole().getName())) )
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
}
