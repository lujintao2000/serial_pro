package com.tuling.serialize;

import com.tuling.domain.*;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by Administrator on 2020-07-07.
 */
public abstract class BaseTest {

    protected abstract void test(Object originalValue) throws Exception;

    @Test
    public void testAllBaseType() throws Exception{
        test(1);
        test(true);
        test('A');
        test((byte)10);
        test((short)10);
        test(1L);
        test(1f);
        test(1d);
        test("world");
    }

    @Test
    public void testDomain() throws Exception{
        test( DataProvider.getRole() );
        test(Calendar.getInstance(TimeZone.getDefault()));
    }

    @Test
    public void testNull() throws Exception{
        test(null);
    }

    @Test
    public void testArrayBase() throws Exception{
        int[][] array = new int[2][2];
        array[0][0] = 1;
        array[0][1] = 2;
        array[1][0] = 3;
        array[1][1] = 4;
        test(array);
        Object[] array2 = new Object[4];
        array2[0] = DataProvider.getUser();
        array2[1] = DataProvider.getUser();
        array2[2] = 5;
        array2[3] = DataProvider.getRole();
        test(array2);
        Object[] array3 = new Object[]{DataProvider.getUser(),null,DataProvider.getUser(),Role.getInstance(),null,Role.getInstance("manager")};

        test(array3);
        test(new Integer[]{1,2,3,4,5,null,6});
        test(new User[]{DataProvider.getUser(),null,DataProvider.getUser(),new User("wanghong",20,170.0f,72.0f)});
        test(new int[]{1,2,3});
        test(new short[]{1,2,3});
        test(new long[]{1,2,3});
        test(new byte[]{1,2,3});
        test(new char[]{'a','b','c'});
        test(new boolean[]{true,false,true,false});
        test(new float[]{1.23f,2.42f,3.22f});
        test(new double[]{1.25d,3.22d,4.22d});
    }



    @Test
    public void testEnum() throws Exception{
        test(BaseTypeEnum.CHARACTER);
    }

    @Test
    public void testArrayObject() throws Exception{
        test(DataProvider.getRoles().toArray(new Role[0]));
    }

    @Test
    public void testList()  throws Exception{
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(3);
        list.add(5);
        list.add(null);
        list.add(4);
        test(list);

        list = new LinkedList<>();
        list.add(1);
        list.add(3);
        list.add(5);
        list.add(null);
        list.add(4);
        test(list);
        test(Arrays.asList(1));
    }

    @Test
    public void testListObject()  throws Exception{
        List<User> users = new ArrayList<>();
        users.addAll(DataProvider.getUsers());
        users.add(null);
        test(users);
        users = new LinkedList<>();
        users.addAll(DataProvider.getUsers());
        users.add(null);
        test(users);
    }

    @Test
    public void testSet()  throws Exception{
        Set<String> set = new TreeSet<>();
        set.add("first");
        set.add("second");
        set.add("third");
        test(set);
        set = new HashSet<>();
        set.add("first");
        set.add("second");
        set.add("third");
        test(set);
    }

    @Test
    public void testSetObject()  throws Exception{
        Set<User> users = new TreeSet<>();
        users.addAll(DataProvider.getUsers());

        test(users);
        users = new HashSet<>();
        users.addAll(DataProvider.getUsers());
        users.add(null);
        test(users);
    }

    @Test
    public void testMap() throws Exception{
        Map map = new HashMap();
        map.put("name","xiaowang");

        map.put(null, null);
        map.put("company",new Company(""));
        map.put("user", new User("wangfei", null, null, 76.0f));
        map.put(new Company(""),"");
        map.put(5,6);
        map.put(new User("wangfei", null, null, 76.0f),5);
        test(Collections.unmodifiableMap(map));
        test(map);
        map = new TreeMap<>();

        map.put("name","xiaohua");
        map.put("age",20);
        map.put("sex","female");
        map.put("user", new User("wangfei", null, null, 76.0f));
        test(map);
    }



    @Test
    public void testGetBytes() throws Exception{
        User user = DataProvider.getUser();
        ObjectOutputStream outputStream = new DefaultObjectOutputStream();
        byte[]  content = outputStream.getBytes(user);
        ObjectInputStream inputStream = new DefaultObjectInputStream();
        User readUser = (User)inputStream.readObject(content);
        Assert.assertEquals(user,readUser);

    }

    @Test
    public void testGetBytesWithClass() throws Exception{
        User user = DataProvider.getUser();
        ObjectOutputStream outputStream = new DefaultObjectOutputStream();
        byte[]  content = outputStream.getBytes(user,false);
        ObjectInputStream inputStream = new DefaultObjectInputStream();
        User readUser = (User)inputStream.readObject(content,User.class);
        Assert.assertEquals(user,readUser);

        outputStream = new DefaultObjectOutputStream();
        content = outputStream.getBytes(user,true);
        inputStream = new DefaultObjectInputStream();
        readUser = (User)inputStream.readObject(content);
        Assert.assertEquals(user,readUser);
    }
}
