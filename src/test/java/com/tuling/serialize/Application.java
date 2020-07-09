package com.tuling.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tuling.domain.*;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.ReflectUtil;
import org.msgpack.MessagePack;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Array;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {



    public static void main(String[] args) throws Exception {

        testIsBasicType();



//        testCharsetCost();

//          testSerialWithKyro();
//          testSerialWithSerial();
//        testSerialWithJava();

//          testUnserialWithJava();
//        testUnserialWithSerial();
    }



    private static boolean testWriteLengthOfObject(int length) throws  IOException{
//        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        AbstractOutputStream.writeLengthOfObject(length,outputStream);
//        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());
//        int readLength = AbstractObjectInputStream.readLengthOfObject(inputStream);
//        return   length == readLength;
        return true;
    }

    private static void testIsBasicType() throws  Exception{

        Class temp = String.class;
        String temp2 = "boolean";
        boolean flag = BaseTypeEnum.BOOLEAN instanceof Enum;
        int hashCode = 0;
        int a = 4;
        String[] array = new String[0];
        Class parentClass = array.getClass().getSuperclass();
        long startTime = new Date().getTime();


        for (int i = 0; i < 90000000; i++) {
//            flag = temp == List.class;
//            hashCode = temp2.hashCode();
//            flag = temp.isArray();
            flag = 3 == a;
        }
        long endTime = new Date().getTime();

        System.out.println("isBasicType invoke  cost " + (endTime - startTime) + "ms" + flag);
    }

    /**
     * 判断该字符串是否可以采用ascii编码
     * @param target
     * @return
     */
    public static boolean isAscii(String target){
        boolean flag = true;
        for(char item : target.toCharArray()){
            if(item > 127){
                flag = false;
                break;
            }
        }
        return flag;
    }

    private static Object[] baseArray = new Object[]{Byte.class,Character.class,Boolean.class,Short.class,Integer.class,Long.class,Float.class,Double.class,String.class};

    public static boolean isBasicType(Class type){
        boolean flag = false;
        for(int i = 0;i < 9;i++){
            if(type == baseArray[i]){
                flag = true;
                break;
            }
        }
        return flag;
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

    private static void testSerialWithKyro() throws Exception{
        User user = DataProvider.getUser();
        List<User> users = DataProvider.getUsers();
        Role role = new Role("项目经理");
        long startTime = new Date().getTime();
        Kryo kryo = new Kryo();
//        kryo.register(User.class);
        for (int i = 0; i < 100000; i++) {

//            kryo.register(User.class);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream);
            kryo.writeObject(output, users);
            output.close();
            System.out.println("");
//            Object m =   kryo.readObject(new Input(new ByteArrayInputStream(outputStream.toByteArray())),User.class);
//            System.out.println(m);
        }
        long endTime = new Date().getTime();

        System.out.println("kyro serialization cost " + (endTime - startTime) + "ms");

    }

    private static void testSerialWithSerial() throws Exception{
        User user = DataProvider.getUser();
        List<User> users = DataProvider.getUsers();
        long startTime = new Date().getTime();
        DefaultObjectOutputStream objectOutputStream = new DefaultObjectOutputStream();
        for (int i = 0; i < 600000; i++) {
            OutputStream outputStream = new ByteArrayOutputStream();
//            Serial.write(DataProvider.getUser(),outputStream,false);
            objectOutputStream.write(users,false,outputStream);

            outputStream.close();
        }
        long endTime = new Date().getTime();

        System.out.println("serial serialization cost " + (endTime - startTime) + "ms");
    }

    private static void testSerialWithJava() throws Exception{
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
        for (int i = 0; i < 300000; i++) {
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
