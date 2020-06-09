package com.tuling.serialize;

import com.tuling.domain.Company;
import com.tuling.domain.User;
import com.tuling.serialize.exception.InvalidDataFormatException;

import java.io.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author lujintao
 * @date 2020-06-09
 */
public class SerializeTest {


    public static void main(String[] args){

        try {
            Hashtable hashtable = new Hashtable();
            hashtable.put(null,"aa");


           Class type = Class.forName("java.lang.String");
            if(type.isArray()){
                System.out.print("a");
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            OutputStream output2 = new FileOutputStream("E:\\List.obj");
            ObjectOutputStream out = new ObjectOutputStream(output);
            try {
//				User user = new User("xiaowang");
//				user.setAge(20);
//				user.setCompanys(null);
				Map<String,Object> map = new HashMap();
				map.put(null,null);
				map.put("age",null);

//				out.write(map);
                LinkedList list = new LinkedList();
                list.add("red");
				list.add("yellow");
				list.add("blue");
                //Integer[]  list = new Integer[]{2,3,44};
//				List<User> list = new ArrayList<>();
////				User[] list = new User[]{new User("wangfei",20),new User("hong",30),null};
//				list.add(new User("wangfei",20,180.0f, 76.0f));
//				list.add(new User("feige",30, 172.0f, 68.3f));
                User user = new User("wangfei",20,180.0f, 76.0f);
                user.setCompany(new Company("优识云创"));
//				list.add(null);
//				Map map = new HashMap();
//				map.put("name","fei");
//				map.put("age",20);

                out.write(map);
                out.close();

            } catch (IOException e) {
                e.printStackTrace();
            }


            ObjectInputStream in = new DefaultObjectInputStream(new ByteArrayInputStream(output.toByteArray()
            ));
            try {
                Object obj;
                try {
                    obj = in.readObject();
                    System.out.println(obj);
                    //System.out.println(((User)obj).getPersonName());
                } catch (InvalidDataFormatException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

}
