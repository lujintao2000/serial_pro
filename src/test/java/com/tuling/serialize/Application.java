package com.tuling.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tuling.domain.*;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.ContextMap;
import com.tuling.serialize.util.ReflectUtil;

import javax.xml.crypto.Data;
import java.io.*;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Ref;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {


    public static void main(String[] args) throws Exception {


        testSerialWithSerial(1,DataProvider.getRole());



//
//        testSerialWithSerial(1,DataProvider.getArray(20000));
//        testSerialWithKyro(1,DataProvider.getArray(20000));




//        testUnSerialWithKyro(1,DataProvider.getArray(20000));
//        testUnserialWithSerial(1,DataProvider.getArray(20000));
//        testSerialWithKyro(200000,DataProvider.getRole());

//        testSerialWithSerial(1,DataProvider.getRole());




//        testSerialWithSerial(1,DataProvider.getList(20000));
//        testSerialWithKyro(1,DataProvider.getList(20000));
//        testSerialWithSerial(400000, DataProvider.getRole());

//        testUnserialWithSerial(400000, DataProvider.getRole());
//        Object[] array = DataProvider.getArray(1000);



        testSerial(1,DataProvider.getUser());
        testKyro(1, DataProvider.getUser());
//        testSerialWithSerial(10000,DataProvider.getRole());
//        testSerialWithKyro(10000,DataProvider.getRole());
//        Class t = Role.class;
//        boolean flag = false;
//
//
//        long startTime = new Date().getTime();
//        for(int i = 0; i < 6000000; i++){
//              t = "".getClass();
//        }
//
//        long endTime = new Date().getTime();
//        System.out.println((endTime - startTime) + "ms");

    }


    private static Object[] baseArray = new Object[]{Byte.class, Character.class, Boolean.class, Short.class, Integer.class, Long.class, Float.class, Double.class, String.class};

    public static boolean isBasicType(Class type) {
        boolean flag = false;
        for (int i = 0; i < 9; i++) {
            if (type == baseArray[i]) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    private static void testCharsetCost() throws Exception {
        int length = 0;
        long startTime = new Date().getTime();
        for (int i = 0; i < 10000000; i++) {
            length = "hello".getBytes("ascii").length;
        }
        long endTime = new Date().getTime();

        System.out.println("serial serialization cost " + (endTime - startTime) + "ms" + length);
    }

    private static void testSerialWithKyro(int count,Object obj) throws Exception {
        Kryo kryo = new Kryo();
        kryo.register(obj.getClass());
        long startTime = System.nanoTime();


        int a = 5;

        for (int i = 0; i < count; i++) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream);
            kryo.writeObject(output, obj);
            output.close();
            a = i;
        }
        long endTime = System.nanoTime();
        System.out.println("kyro serialization cost " + (endTime - startTime)/10000 + "ns" );
    }

    private static void testUnSerialWithKyro(int count,Object target) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Kryo kryo2 = new Kryo();
        Output output = new Output(outputStream);
        kryo2.writeClassAndObject(output, target);
//        kryo2.writeObject(output,target);
        output.close();


        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
        Kryo kryo = new Kryo();
        kryo.register(target.getClass());
        long startTime = System.nanoTime();

        for (int i = 0; i < count; i++) {
            Object t = kryo.readClassAndObject(new Input(inputStream));
            inputStream.reset();
        }
        long endTime = System.nanoTime();
        System.out.println("kyro unserialization cost " + (endTime - startTime)/1000000 + "ms");
    }

    private static void testUnserialWithSerial(int count,Object target) throws Exception {

        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        ObjectOutputStream out2 = new DefaultObjectOutputStream();
        out2.write(target, output2);
//        out2.write(target,false,output2);
        output2.close();

        ByteArrayInputStream input2 = new ByteArrayInputStream(output2.toByteArray());
        ReflectUtil.register(target.getClass());
        ObjectInputStream in = new DefaultObjectInputStream();
        long startTime = System.nanoTime();

        boolean flag = true;
        for (int i = 0; i < count; i++) {
            Object obj =  in.readObject(input2);
            input2.reset();
        }
        long endTime = System.nanoTime();
        System.out.println("serial unserialization cost " + (endTime - startTime)/1000000 + "ms" + flag);
    }

    private static void testKyro(int count,Object value) throws Exception{

        Kryo kyro = new Kryo();
        long startTime = new Date().getTime();
        for (int i = 0; i < count; i++) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream);
            kyro.writeClassAndObject(output, value);
            output.close();
            ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            Object t = kyro.readClassAndObject(new Input(inputStream));
            inputStream.close();
        }
        long endTime = new Date().getTime();
        System.out.println("The total cost of kyro serialization and unserialization is  " + (endTime - startTime) + "ms");

    }

    private static void testSerial(int count,Object value) throws Exception{
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        ObjectOutputStream out = new DefaultObjectOutputStream();
//        out.write(value, output);
//        output.close();

        ObjectInputStream in = new DefaultObjectInputStream();
        boolean flag = true;
        ReflectUtil.register(value.getClass());
        long startTime = new Date().getTime();
        for (int i = 0; i < count; i++) {
            ByteArrayOutputStream output2 = new ByteArrayOutputStream();
            ObjectOutputStream out2 = new DefaultObjectOutputStream();
            out2.write(value, output2);
            output2.close();

            ByteArrayInputStream input2 = new ByteArrayInputStream(output2.toByteArray());

            Object obj =  in.readObject(input2);
            input2.close();
        }
        long endTime = new Date().getTime();
        System.out.println("The total cost of serial unserialization and serialization is " + (endTime - startTime) + "ms" + flag);
    }

    private static void testSerialWithSerial(int count,Object value) throws Exception {
        ReflectUtil.register(value.getClass());
        DefaultObjectOutputStream objectOutputStream = new DefaultObjectOutputStream();
        long startTime = System.nanoTime();
        for (int i = 0; i < count; i++) {
            OutputStream outputStream = new ByteArrayOutputStream();
            objectOutputStream.write(value, false, outputStream);
            outputStream.close();
        }
        long endTime = System.nanoTime();
        System.out.println("seri serialization cost " + (endTime - startTime)/10000 + "ns");
    }




    private static void testSerialWithJava() throws Exception {
        User user = DataProvider.getUser();
        long startTime = new Date().getTime();
        for (int i = 0; i < 600000; i++) {
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
        for (int i = 0; i < 600000; i++) {
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(input);
            in.readObject();
            input.reset();
        }
        long endTime = new Date().getTime();
        System.out.println("java unserialazation cost " + (endTime - startTime) + "ms");


    }


    private static List<User> getUsers() {
        List<User> users = new ArrayList<>();
//        User firstUser = new User("wangfei", 20, 180.f, 76.0f);
//        firstUser.setCompany(new Company("优识云创"));
//        User secondUser = new User("zhiguo", null, null, 72.03f);
//        secondUser.setCompany(new Company("奇米科技"));
//        User thirdUser = new User("huabing", 30, 172.f, 74.0f);
//        thirdUser.setCompany(new Company("微尘大业"));
//        Role role = new Role("项目经理");
//        Department department = new AboardDepartment("技术部", new Country("中国"));
//        Profession profession = new Profession("java工程师");
//        firstUser.setRole(role);
//        firstUser.setDepartment(department);
//        firstUser.setProfession(profession);
//        secondUser.setRole(role);
//        secondUser.setDepartment(department);
//        secondUser.setProfession(profession);
//        thirdUser.setRole(role);
//        thirdUser.setDepartment(department);
//        thirdUser.setProfession(profession);
//
//
//        users.add(firstUser);
//        users.add(secondUser);
//        users.add(thirdUser);
//        users.add(thirdUser);
//        users.add(thirdUser);
//        users.add(thirdUser);
//
//        return Arrays.asList(firstUser, secondUser, thirdUser, thirdUser, thirdUser, thirdUser);
        return users;
    }


}
