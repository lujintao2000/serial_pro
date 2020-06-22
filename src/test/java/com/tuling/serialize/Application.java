package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.ReflectUtil;
import org.msgpack.MessagePack;
import java.io.*;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {

    public static void main(String[] args) throws Exception {
        BaseTypeEnum t = BaseTypeEnum.valueOf("VOID");
        Class departClass = Class.forName("com.tuling.serialize.BaseTypeEnum");


        Constructor[] constructors = departClass.getDeclaredConstructors();
        Constructor constructor = constructors[0];
        constructor.setAccessible(true);

        Object department = constructor.newInstance("STRING",10,String.class,"");
        Constructor[]  constructors2 = User.class.getConstructors();

        List<Class> list = ReflectUtil.getSelfAndSuperClass(Object.class);
        list =  ReflectUtil.getSelfAndSuperClass(Object.class);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        com.tuling.serialize.ObjectOutputStream out = new DefaultObjectOutputStream( false,true);
//        Map<Class,List<Class>> map = new HashMap<>();
//        List<Class> parentClassList = new ArrayList<>();
//        parentClassList.add(Department.class);
//        map.put(AboardDepartment.class,parentClassList);
//
//
//        Department department = new Department("开发部");
//        Field field = Department.class.getDeclaredField("name");
//        java.io.ObjectOutputStream  out = new java.io.ObjectOutputStream (output);
        out.write(getUser(),output);


        int i = 0;
        long startTime = new Date().getTime();



//        MessagePack messagePack = new MessagePack();
////        messagePack.register(User.class);
//        byte[] content = messagePack.write(getUser());

       // Object obj = messagePack.read(content);

        for(i = 0;i < 100000; i++){
            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            com.tuling.serialize.ObjectInputStream in = new DefaultObjectInputStream();
             in.readObject(input);

        }
//
        long endTime = new Date().getTime();
        System.out.println("serial cost " + (endTime - startTime) + "ms");
//
//        testUnserialWithJava();

    }

    private static void testUnserialWithJava() throws IOException,ClassNotFoundException{
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(output);
        out.writeObject(getUser());
        out.close();

        long startTime = new Date().getTime();
        for(int i = 0; i < 10000; i++) {
            ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(input);
            Object obj = in.readObject();
            in.close();
        }
        long endTime = new Date().getTime();
        System.out.println("java cost " + (endTime - startTime) + "ms");


    }

    private static List<User> getUsers(){
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
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);

        return users;
    }

    public static User getUser(){
        User user = new User("wangfei", 20, 170.0f, 76.0f);
        user.setCompany(new Company("优识云创"));
        user.setRole(new Role("项目经理"));
        user.setDepartment(new Department("技术部"));
        user.setProfession(new Profession("java工程师"));
        return user;
    }
}
