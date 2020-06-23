package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.BuilderUtil;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;

/**
 * @author lujintao
 * @date 2020-06-09
 */
public class SerializeTest {

    private void test(Object originalValue) throws Exception {

//        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStream output = new FileOutputStream("e:\\list2.obj");
        //ObjectOutputStream out = new DefaultObjectOutputStream( needOrder,true);
        ObjectOutputStream out = new DefaultObjectOutputStream( );
        out.write(originalValue,false,output);
        output.close();


//        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        //ObjectInputStream in = new DefaultObjectInputStream(needOrder,true);
        InputStream input = new FileInputStream("e:\\list2.obj");
        ObjectInputStream in = new DefaultObjectInputStream();
        Object obj = null;
        try {
            if(originalValue == null){
                obj = in.readObject(User.class,input);
            }else{
                obj = in.readObject(originalValue.getClass(),input);
            }

            System.out.println(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            input.close();
        }
        if(originalValue != null){
            if(originalValue.getClass().isArray()){
                boolean flag = true;
                int length = Array.getLength(originalValue);
                if(obj != null && obj.getClass().isArray() && length == Array.getLength(obj)){
                    for(int i = 0; i < length; i++){
                        if(!Array.get(originalValue,i).equals(Array.get(obj,i))){
                            flag = false;
                            break;
                        }
                    }
                }else{
                    flag = false;
                }
                Assert.assertEquals(flag, true);

            }else if(originalValue instanceof  Collection){
                boolean flag = false;
                if(obj != null && obj instanceof  Collection && ((Collection)originalValue).size() == ((Collection)obj).size()){
                    flag = ((Collection) obj).stream().noneMatch(x -> !((Collection)originalValue).contains(x));

                }
                Assert.assertEquals(flag, true);

            }else{
                Assert.assertEquals(originalValue,obj);
            }
        }else{
            Assert.assertEquals(originalValue,obj);
        }


    }

    @Test
    public void testAllBaseType() throws Exception{
        Number t;
        test(1);
        test(true);
        test('A');
        test((byte)10);
        test((short)10);
        test(1L);
        test(1f);
        test(1d);
        test("world");
        test(new BigInteger("200000"));
    }

    @Test
    public void testDomain() throws Exception{
        User user = new User("wangfei", 20, 170.0f, 76.0f);
        user.setCompany(new Company("优识云创"));
        user.setRole(new Role("项目经理"));
        user.setDepartment(new AboardDepartment("技术部"));
        user.setProfession(new Profession("java工程师"));
        test(user);
    }

    @Test
    public void testNull() throws Exception{
        test(null);
    }

    @Test
    public void testArrayBase() throws Exception{
        int[] array = new int[]{1,2,3};
        test(array);
    }

    @Test
    public void testArray() throws Exception{
        Integer[] array = new Integer[]{1,2,3};
        test(array);
    }

    @Test
    public void testEnum() throws Exception{
        test(BaseTypeEnum.CHARACTER);
    }

    @Test
    public void testArrayObject() throws Exception{
        test(getUsers().toArray(new User[0]));
    }

    @Test
    public void testList()  throws Exception{
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(5);
        list.add(null);
        list.add(4);
        test(Arrays.asList(1,2,3,null,5));

    }

    @Test
    public void testListObject()  throws Exception{
        List<User> users = new ArrayList<>();
        users.addAll(getUsers());

        test(users);
    }

    @Test
    public void testSet()  throws Exception{
        Set<String> set = new HashSet<>();
        set.add("first");
        set.add("second");
        set.add("third");
        test(set);
    }

    @Test
    public void testSetObject()  throws Exception{
        Set<User> users = new HashSet<>();
        users.addAll(getUsers());
        test(users);
    }

    @Test
    public void testMap() throws Exception{
        Map<String, Object> map = new HashMap();
        map.put(null, null);
        map.put("company",new Company(""));
        map.put("user", new User("wangfei", null, null, 76.0f));
        test(map);
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
//        users.add(thirdUser);
//        users.add(thirdUser);
//        users.add(thirdUser);
//        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);
        users.add(thirdUser);

        return users;
    }
}
