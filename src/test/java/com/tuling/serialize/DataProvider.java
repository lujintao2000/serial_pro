package com.tuling.serialize;

import com.tuling.domain.*;

/**
 * Created by Administrator on 2020-06-24.
 */
public class DataProvider {

    public static User getUser() {
        User user = new User("wangfei", 20, 170.0f, 76.0f);
        user.setCompany(new Company("优识云创"));
        user.setRole(new Role("项目经理"));
        user.setDepartment(new Department("技术部"));
        user.setProfession(new Profession("java工程师"));
        return user;
    }
}
