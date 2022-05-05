package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.IdGenerator;

import javax.xml.crypto.Data;
import java.util.*;

/**
 * Created by Administrator on 2020-06-24.
 */
public class DataProvider {

    public static List getList(int size){
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < size;i++){
            Role role = DataProvider.getRole();
            role.setName("xiaowang" + i);
            list.add(role);
        }
        return list;
    }

    public static Set getSet(int size){
        Set<User> set = new TreeSet<>();
        for(int i = 0; i < size;i++){
            User user = DataProvider.getUser();
            user.setAge(i);
            set.add(user);
        }
        return set;
    }

    public static Object[] getArray(int size){
        Object[] result = new Object[size];
        for(int i = 0; i < size;i++){
              Role role = Role.getInstance();
              role.setAge(i);
              role.setName("总监级别的人物，你可了解" + i);
              role.setSex(true);
              result[i] = role;
        }
        return result;
    }

    public static Map getMap(int count){
        Map map = new TreeMap();
        for(int i = 0; i < count;i++){
           map.put("hello" + i,i);
        }
        return map;
    }

    public static Map getObjectMap(int count){
        Map map = new TreeMap();
        for(int i = 0; i < count;i++){
            User key = DataProvider.getUser();
            key.setAge(i);
            map.put(key,DataProvider.getUser());
        }
        return map;
    }

    public static Role getRole(){
       Role role = Role.getInstance();
       role.setName("总监级别的人物，那肯定不一样");
       role.setAge(20);
       return role;
    }

    public static Department getDepartment(){
        return new AboardDepartment("开发部",new Country("china"));
    }


    public static Employee getEmployee(){
        Employee employee = new Employee();

        employee.setCompany(new Company("优识云创信息技术有限公司"));
        employee.setRole(Role.getInstance("architecture"));


        employee.setDepartment(new AboardDepartment("技术部",new Country("china")));
        employee.setProfession(new Profession("java工程师"));

        employee.setNation(Nation.HANZU);

        employee.addLabel("dog");
        employee.addLabel("cat");

        employee.setAge(20);
        employee.setName("小花");

        employee.setWeight(70.4f);
        employee.setHeight(170.10f);
        employee.setSex(true);
        return employee;
    }

    public static User getUser(){
        User user = new User("wangfei", 20, 170.0f, 76.0f);
        user.setCompany(new Company("优识云创"));
        user.setRole(Role.getInstance("项目经理"));
        user.setDepartment(new AboardDepartment("技术部",new Country("china")));
        user.setProfession(new Profession("java工程师"));
        user.setNation(Nation.HANZU);
        user.setAnother(null);
        user.addLabel("死不了");
        user.addLabel("游戏主播");
        return user;
    }

    public static User2 getUser2(){
        User2 user2 = new User2("xiaohei",true,173.0f,72.2f);

        return user2;
    }

    public static List<Role> getRoles(){
        return Arrays.asList(Role.getInstance("xiaobai"),Role.getInstance("xiaohei"));
    }

    public static List<User> getUsers(){
        List<User> users = new ArrayList<>();
        User firstUser = new User("wangfei", 20, 180.f, 76.0f);
        firstUser.setCompany(new Company("优识云创"));
        User secondUser = new User("zhiguo", null, null, 72.03f);
        secondUser.setCompany(new Company("奇米科技"));
        User thirdUser = new User("huabing", 30, 172.f, 74.0f);
        thirdUser.setCompany(new Company("微尘大业"));
        Role role = Role.getInstance("项目经理");
        Department department = new AboardDepartment("技术部",new Country("中国"));
        Profession profession = new Profession("java工程师");
        firstUser.setRole(role);
        firstUser.setDepartment(department);
        firstUser.setProfession(profession);
        secondUser.setRole(role);
        secondUser.setDepartment(department);
        secondUser.setProfession(profession);
        thirdUser.setRole(role);
        thirdUser.setDepartment(department);
        thirdUser.setProfession(profession);
        users.add(firstUser);
        users.add(secondUser);
        users.add(firstUser);

        users.add(thirdUser);
        users.add(secondUser);
        return users;
    }
}
