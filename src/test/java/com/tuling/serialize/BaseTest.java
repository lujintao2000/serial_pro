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
//        test(new BigInteger("200000"));
//        test(new AtomicInteger(20));
    }

    @Test
    public void testDomain() throws Exception{
        test(DataProvider.getUser());
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
        test(DataProvider.getUsers().toArray(new User[0]));
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
        test(Arrays.asList(1,2,3,null,5));
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
        Map<String, Object> map = new HashMap();
        map.put(null, null);
        map.put("company",new Company(""));
        map.put("user", new User("wangfei", null, null, 76.0f));
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
