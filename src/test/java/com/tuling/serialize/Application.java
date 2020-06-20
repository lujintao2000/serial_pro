package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.ReflectUtil;
import org.msgpack.MessagePack;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;

/**
 * Created by Administrator on 2020-06-10.
 */
public class Application {

    public static void main(String[] args) throws Exception {
        List<Class> list = ReflectUtil.getSelfAndSuperClass(Object.class);
        list =  ReflectUtil.getSelfAndSuperClass(Object.class);

        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        com.tuling.serialize.ObjectOutputStream out = new DefaultObjectOutputStream(output, false,true);
//        Map<Class,List<Class>> map = new HashMap<>();
//        List<Class> parentClassList = new ArrayList<>();
//        parentClassList.add(Department.class);
//        map.put(AboardDepartment.class,parentClassList);
//
//
//        Department department = new Department("开发部");
//        Field field = Department.class.getDeclaredField("name");
        java.io.ObjectOutputStream  out = new java.io.ObjectOutputStream (output);
        int i = 0;
        long startTime = new Date().getTime();



//        MessagePack messagePack = new MessagePack();
////        messagePack.register(User.class);
//        byte[] content = messagePack.write(new Department("开发部"));
//
//        Object obj = messagePack.read(content);

        for(i = 0;i < 100000; i++){
//            field.setAccessible(true);
//            Object value = field.get(department);
//            out.write(getUser());
//            Class t = Department.class.getSuperclass();
//            Object obj = parentClassList.get(0);
            out.writeObject(getUsers());
//            Class a = map.get("java.util.ArrayList");
//            Class.forName("java.util.ArrayList");

        }
        String.class.getDeclaredField()


        long endTime = new Date().getTime();
        System.out.println("Total cost " + (endTime - startTime) + "ms");
        out.close();



//        Field[] fields = ReflectUtil.getAllInstanceField(User.class, true, true
//        );
//        BigInteger a = new BigInteger("10241024102410241024");
//        BigDecimal b = new BigDecimal("");
//        System.out.println(a.toString(10));
//        System.out.println(String.class.getResource("/").getPath());
////        String[] a =  new String[]{};
////        String[] b = new String[]{"a"};
////        System.out.println(a.getClass() == b.getClass());
////        System.out.println(String.format("类%s",String.class));
//        Object obj = Array.newInstance(String.class,2);
//        Object obj2 = Array.newInstance(String.class,3);
//        System.out.println(obj.getClass() == obj2.getClass());
//
//        Map map = new HashMap();
//        map.put("xiaowang",20);
//        System.out.println(new String[0].getClass().getComponentType());
////        MessagePacker messagePack = MessagePack.newDefaultPacker(new ByteArrayOutputStream());
//        ObjectOutputStream outputStream = null;
//        try {
//             outputStream = new ObjectOutputStream(new FileOutputStream("e:\\list.obj"));
//            outputStream.writeObject(getUsers());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            if(outputStream != null){
//                try{
//                    outputStream.close();
//                }catch (Exception ex){
//                    ex.printStackTrace();
//                }
//
//            }
//        }

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


//        users.add(firstUser);
//        users.add(secondUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
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
