package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.IdGenerator;

import java.util.*;

/**
 * Created by Administrator on 2020-06-24.
 */
public class DataProvider {

    public static List getList(){
        List<Object> list = new ArrayList<>();
        for(int i = 0; i < 1;i++){
            for(int j = 0;j < 10000;j++){
                list.add((long)j);
            }

        }
        return list;
    }

    public static Set getSet(){
        Set<Integer> set = new TreeSet<>();
        for(int i = 0; i < 10000;i++){
            set.add(i);
        }
        return set;
    }

    public static Object[] getArray(){
        Object[] result = new Object[10000];
        for(int i = 0; i < 10000;i++){
            result[i] = i;
        }
        return result;
    }

    public static Map getMap(){
        Map map = new TreeMap();
        for(int i = 0; i < 10000;i++){
           map.put("hello" + i,i);
        }
        return map;
    }

    public static User getUser(){
        User user = new User("wangfei", 20, 170.0f, 76.0f);
        user.setCompany(new Company("优识云创"));
        user.setRole(new Role("项目经理"));
        user.setDepartment(new AboardDepartment("技术部",new Country("china")));
        user.setProfession(new Profession("java工程师"));
        user.setNation(Nation.HANZU);
//        user.setAnother(null);
        user.addLabel("死不了");
        user.addLabel("游戏主播");
//        user.setId(IdGenerator.getId());
        return user;
    }

    public static List<User> getUsers(){
        List<User> users = new ArrayList<>();
        User firstUser = new User("wangfei", 20, 180.f, 76.0f);
        firstUser.setCompany(new Company("优识云创"));
        User secondUser = new User("zhiguo", null, null, 72.03f);
        secondUser.setCompany(new Company("奇米科技"));
        User thirdUser = new User("huabing", 30, 172.f, 74.0f);
        thirdUser.setCompany(new Company("微尘大业"));
        Role role = new Role("项目经理");
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
