package com.tuling.serialize;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.tuling.domain.*;
import com.tuling.serialize.util.*;
import org.msgpack.MessagePack;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {


    public static void main(String[] args) throws Exception {
//            SubClass[] array = new SubClass[]{};
//            System.out.println(SubClass.VERSION);
//            String name2 = new String("xiaowang");
//            String name = name2.intern();
//            boolean isEqual = name2 == "xiaowang";
//            String[] a = new String[]{"aa"};
//            String[] b = new String[]{"aa"};
//            System.out.println(a.equals(b));
////            Class t =  Class.forName("java.util.Collections$UnmodifiableMap");
////
////            testMap();
            testUnSerialWithKyro();
            testUnserialWithSerial();
//            getProcessID();
//            Thread.sleep(100000);

//              System.out.println(System.nanoTime());


//        InetAddress ip;
//        ip = InetAddress.getLocalHost();
//        System.out.println("Current IP address : " + ip.getHostAddress());
//
//        NetworkInterface network = NetworkInterface.getByInetAddress(ip);
//
//        byte[] mac = network.getHardwareAddress();
//
//        System.out.print("Current MAC address : ");
//
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < mac.length; i++) {
//            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
//        }
//        System.out.println(sb.toString());


    }

    public static final int getProcessID() {
        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        System.out.println(runtimeMXBean.getName());
        return Integer.valueOf(runtimeMXBean.getName().split("@")[0])
                .intValue();
    }

    private static void testMap() throws Exception{
//        Map<Integer,String> map = new HashMap();
//        map.put(1,"ss");
//        map.put(2,"bb");
//        List<String> list = new ArrayList();
//        list.add("aa");
//        list.add("bb");
////        ReflectUtil.add(Role.class,2);
//        ByteBuf buf = new ByteBuf();
////        buf.writeByte(Constant.CLASSNAME_SAME_WITH_FIELD);
//        buf.writeScalableInt(3);
//        buf.writeString("com.tuling.domain.Role",true);
//        String content = "";
//        Class result = null;
//        Class arrayType = null;
//        Class objectClass = null;
//        Context context = new Context();
//        context.setCurrentField(Employee.class.getDeclaredField("role"));
//        String className = "com.tuling.domain.Role";
//        int index = 0;
//        boolean flag = true;
//        long startTime = new Date().getTime();
//        for (int i = 0; i < 6000000; i++) {
//            byte preLength = buf.readByte();
//            if(preLength > 0) {
//                buf.decreaseReaderIndex(1);
//                //根据类标识获取与之对应的类；如果存在对应的类，就跳过类名数据读取；否则，读取类名数据，然后读取的类与标识绑定
//                //读取类标识
//                int classId = buf.readScalableInt();
//                result = ReflectUtil.getClassById(classId);
//                if(result != null){
//                    //跳过类名数据   对于基本类型，要跳过的字节长度不一样
//                    buf.skipNextString();
//                }
//                else{
//                    //2. 读入类名
//                    String fullClassName = buf.readString(true);
//                    if(fullClassName.endsWith("[]")){
//                         arrayType  = ReflectUtil.get(fullClassName.substring(0,fullClassName.length() - 2));
//                        result = Array.newInstance(arrayType, 0).getClass();
//                    }else{
//                        result = ReflectUtil.getComplexClass(fullClassName);
//                    }
//                    ReflectUtil.add(result,classId);
//                }
//            }else if(preLength == Constant.CLASSNAME_SAME_WITH_FIELD){
//                //字段值的类型和字段类型相同
//                result = context.getCurrentField().getType();
//            }
//            buf.readerIndex(0);
//
//        }
//        long endTime = new Date().getTime();
//        System.out.println(" cost " + (endTime - startTime) + "ms" + className + index);

    }


    private static void testIsBasicType() throws Exception {
        boolean flag = Object.class.isAssignableFrom(int.class);
        Class temp = int.class.getSuperclass();

        int hashCode = 0;
        int a = 4;
        String[] content = new String[]{};
        Object[] array = DataProvider.getList().toArray();
        array = content;
        Object t = 5;
        long result = 0L;
        User user = new User();
        Method method = User.class.getDeclaredMethod("getAge",null);
        long startTime = new Date().getTime();

        for(int j = 0;j < 80000000; j++){
//            user.getAge();
//            flag = t instanceof String;
//            ByteBuf buf = new ByteBuf(256);
//            for(int i = 0; i < 10000; i++){
////                buf.writeLong(800,2);
//                buf.writeByte(127);
//            }
            method.invoke(user,null);
        }

        long endTime = new Date().getTime();

        System.out.println("isBasicType invoke  cost " + (endTime - startTime) + "ms" + result);
    }

    /**
     * 判断该字符串是否可以采用ascii编码
     *
     * @param target
     * @return
     */
    public static boolean isAscii(String target) {
        boolean flag = true;
        for (char item : target.toCharArray()) {
            if (item > 127) {
                flag = false;
                break;
            }
        }
        return flag;
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

    private static void testSerialWithKyro() throws Exception {
        User user = DataProvider.getUser();
        List<User> users = DataProvider.getUsers();
        Object obj = DataProvider.getList();
        Set set = DataProvider.getSet();
        Object[] array = DataProvider.getArray();
        Map map = DataProvider.getMap();
        Role role = new Role("项目经理");
        long startTime = new Date().getTime();
        Kryo kryo = new Kryo();
        kryo.register(User.class);
//        kryo.register(Map.class);
        for (int i = 0; i < 1; i++) {

//            kryo.register(User.class);
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Output output = new Output(outputStream);
            kryo.writeObject(output, users);
            output.close();
//            System.out.println("");

        }
        long endTime = new Date().getTime();

        System.out.println("kyro serialization cost " + (endTime - startTime) + "ms");

    }

    private static void testUnSerialWithKyro() throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Kryo kryo2 = new Kryo();
        Output output = new Output(outputStream);
        kryo2.writeClassAndObject(output, DataProvider.getUser());
        output.close();


        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        long startTime = new Date().getTime();
        Kryo kryo = new Kryo();
        for (int i = 0; i < 600000; i++) {
            Object t = kryo.readClassAndObject(new Input(inputStream));
            inputStream.reset();
        }
        long endTime = new Date().getTime();
        System.out.println("kyro unserialization cost " + (endTime - startTime) + "ms");
    }

    private static void testUnserialWithSerial() throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new DefaultObjectOutputStream();
        out.write(DataProvider.getUser(), output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());


        ByteArrayOutputStream output2 = new ByteArrayOutputStream();
        ObjectOutputStream out2 = new DefaultObjectOutputStream();
        out2.write(DataProvider.getUser(), output2);
        output2.close();

        ByteArrayInputStream input2 = new ByteArrayInputStream(output2.toByteArray());
        ByteArrayInputStream[] inputArray = new ByteArrayInputStream[]{input,input2};
        long startTime = new Date().getTime();
        ObjectInputStream in = new DefaultObjectInputStream();
        boolean flag = true;
        for (int i = 0; i < 600000; i++) {
            Object obj =  in.readObject(input2);
            input2.reset();
        }
        long endTime = new Date().getTime();
        System.out.println("serial unserialization cost " + (endTime - startTime) + "ms" + flag);
    }



    private static void testSerialWithSerial() throws Exception {
        User user = DataProvider.getUser();
        List<User> users = DataProvider.getUsers();
        Object obj = DataProvider.getList();
        Set set = DataProvider.getSet();
        Object[] array = DataProvider.getArray();
        Map map = DataProvider.getMap();

        long startTime = new Date().getTime();
        DefaultObjectOutputStream objectOutputStream = new DefaultObjectOutputStream();
        for (int i = 0; i < 1; i++) {

            OutputStream outputStream = new ByteArrayOutputStream();
//            Serial.write(DataProvider.getUser(),outputStream,false);
            objectOutputStream.write(users, false, outputStream);

            outputStream.close();
        }
        long endTime = new Date().getTime();

        System.out.println("serial serialization cost " + (endTime - startTime) + "ms");
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

        return Arrays.asList(firstUser, secondUser, thirdUser, thirdUser, thirdUser, thirdUser);
    }


}
