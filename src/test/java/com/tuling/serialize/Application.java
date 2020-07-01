package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.ByteBuf;

import javax.xml.crypto.Data;
import java.io.*;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {

    public static void main(String[] args) throws Exception {

         byte t = 0;
         short t2 = 0;

         System.out.println(t == t2);
//        testCharsetCost();

//          byte a = (byte) 255;


//        int length = "hello".getBytes("ascii").length;
        testSerialWithSerial();
//        testSerialWithJava();
//        testUnserialWithJava();
//        testUnserialWithSerial();
    }

    private static void testCharsetCost() throws  Exception{
        int length = 0;
        long startTime = new Date().getTime();
        for (int i = 0; i < 10000000; i++) {
            length = "hello".getBytes("ascii").length;
        }
        long endTime = new Date().getTime();

        System.out.println("serial serialization cost " + (endTime - startTime) + "ms" + length);
    }

    private static void testSerialWithSerial() throws Exception{
        User user = DataProvider.getUser();
        long startTime = new Date().getTime();
        for (int i = 0; i < 300000; i++) {
            OutputStream outputStream = new ByteArrayOutputStream();
            Serial.write(user,outputStream,true);

            outputStream.close();
        }
        long endTime = new Date().getTime();

        System.out.println("serial serialization cost " + (endTime - startTime) + "ms");
    }

    private static void testSerialWithJava() throws Exception{
        User user = DataProvider.getUser();
        long startTime = new Date().getTime();
        for (int i = 0; i < 300000; i++) {
            OutputStream outputStream = new ByteArrayOutputStream();
            java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(outputStream);
            out.writeObject(user);
            outputStream.close();
        }
        long endTime = new Date().getTime();

        System.out.println("java serialazation cost " + (endTime - startTime) + "ms");
    }

    private static void testUnserialWithJava() throws IOException, ClassNotFoundException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        java.io.ObjectOutputStream out = new java.io.ObjectOutputStream(output);
        out.writeObject(DataProvider.getUser());
        out.close();
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
//        java.io.ObjectInputStream in = new java.io.ObjectInputStream(input);
        long startTime = new Date().getTime();
        for (int i = 0; i < 100000; i++) {
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(input);
            in.readObject();
            input.reset();
        }
        long endTime = new Date().getTime();
        System.out.println("java unserialazation cost " + (endTime - startTime) + "ms");


    }

    private static void testUnserialWithSerial() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new DefaultObjectOutputStream( );
        out.write(DataProvider.getUser(),output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        long startTime = new Date().getTime();
        for (int i = 0; i < 100000; i++) {
            ObjectInputStream in = new DefaultObjectInputStream();
            in.readObject(input);
            input.reset();
        }
        long endTime = new Date().getTime();
        System.out.println("serial unserialization cost " + (endTime - startTime) + "ms");
    }

    private static List<User> getUsers() {
        List<User> users = new ArrayList<>();
        User firstUser = new User("wangfei", 20, 180.f, 76.0f);
        firstUser.setCompany(new Company("优识云创"));
        User secondUser = new User("zhiguo", null, null, 72.03f);
        secondUser.setCompany(new Company("奇米科技"));
        User thirdUser = new User("huabing", 30, 172.f, 74.0f);
        thirdUser.setCompany(new Company("微尘大业"));
        Role role = new Role("项目经理");
        Department department = new AboardDepartment("技术部", new Country("中国"));
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

        return Arrays.asList(firstUser,secondUser,thirdUser,thirdUser,thirdUser,thirdUser);
    }


}
