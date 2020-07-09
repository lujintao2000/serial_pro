package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.IdGenerator;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Administrator on 2020-06-24.
 */
public class DataProvider {

    public static User getUser(){
        User user = new User("wangfei", 20, 170.0f, 76.0f);
        user.setCompany(new Company("优识云创"));
        user.setRole(new Role("项目经理"));
        user.setDepartment(new AboardDepartment("技术部",new Country("china")));
        user.setProfession(new Profession("java工程师"));
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
