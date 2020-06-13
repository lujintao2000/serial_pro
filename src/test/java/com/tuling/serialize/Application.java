package com.tuling.serialize;

import com.tuling.domain.Company;
import com.tuling.domain.User;
import com.tuling.serialize.util.ReflectUtil;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {

    public static void main(String[] args) throws ClassNotFoundException {

        Field[] fields = ReflectUtil.getAllInstanceField(User.class, true, true
        );
        System.out.print(fields);
//        String[] a =  new String[]{};
//        String[] b = new String[]{"a"};
//        System.out.println(a.getClass() == b.getClass());
//        System.out.println(String.format("类%s",String.class));
        Object obj = Array.newInstance(String.class,2);
        Object obj2 = Array.newInstance(String.class,3);
        System.out.println(obj.getClass() == obj2.getClass());

        Map map = new HashMap();
        map.put("xiaowang",20);
        System.out.println(map.get(new String("xiaowang")));

    }

    private static List<User> getUsers(){
        List<User> users = new ArrayList<>();
        User firstUser = new User("wangfei", 20, 180.f, 76.0f);
//        firstUser.setCompany(new Company("优识云创"));
        User secondUser = new User("zhiguo", 30, 190.0f, 72.03f);
//        secondUser.setCompany(new Company("奇米科技"));
//        User thirdUser = new User("huabing", 30, 172.f, 74.0f);
//        thirdUser.setCompany(new Company("微尘大业"));
        users.add(firstUser);
        users.add(secondUser);
//        users.add(thirdUser);
        return users;
    }
}
