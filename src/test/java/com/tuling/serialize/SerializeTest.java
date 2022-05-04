package com.tuling.serialize;

import com.tuling.domain.*;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.Constant;
import org.junit.Assert;
import org.junit.Test;
import java.io.*;
import java.util.*;

/**
 * @author lujintao
 * @date 2020-06-09
 */
public class SerializeTest extends BaseTest{
    @Override
    protected void test(Object originalValue) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Serial.write(originalValue,output,256);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Object obj = null;
        try {
            obj = Serial.read(input);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            input.close();
        }
        Assert.assertEquals(TestUtil.isEqual(originalValue,obj),true);

    }


    @Test
    public void testWriteReferenceIndex(){
        testWriteReferenceIndexBase(0);
        testWriteReferenceIndexBase(3);
        testWriteReferenceIndexBase(30);
        testWriteReferenceIndexBase(31);
        testWriteReferenceIndexBase(32);
        testWriteReferenceIndexBase(1025);
        testWriteReferenceIndexBase(3588);
        testWriteReferenceIndexBase(7000);
        testWriteReferenceIndexBase(256 * 32 - 1);
    }

    @Test
    public void testWriteClassSameWithField(){

        ByteBuf buf = new ByteBuf();
        buf.writeByte(Constant.CLASSNAME_SAME_WITH_FIELD);
        byte firstByte = buf.readByte();

        Assert.assertTrue(firstByte == Constant.CLASSNAME_SAME_WITH_FIELD);

    }

    private void testWriteReferenceIndexBase(int index){
        ByteBuf buf = new ByteBuf();
        AbstractOutputStream.writeReferenceIndex(index,buf);
        byte firstByte = buf.readByte();
        int readIndex = AbstractObjectInputStream.readReferenceIndex(buf,firstByte);
        Assert.assertTrue(index == readIndex);
    }

    @Test
    public void testLengthOfInt() throws Exception{
        test(1);
        test(128);
        test(128);
        test(128);
        test(128);

    }

    @Test
    public void testArray() throws Exception{
        Object[] array = new Object[]{DataProvider.getUser(),null,DataProvider.getUser(),Role.getInstance(),null,Role.getInstance("manager")};

        test(array);
        test(new Integer[]{1,2,3,4,5,null,6});
        test(new User[]{DataProvider.getUser(),null,DataProvider.getUser(),new User("wanghong",20,170.0f,72.0f)});
        test(new int[]{1,2,3,4,6});
        test(new short[]{1,2,3,7,8});
        test(new long[]{1,2,3,8,2});
        test(new byte[]{1,2,3,5,5});
        test(new char[]{'a','b','c','e'});
        test(new boolean[]{true,false,false,true,false});
        test(new float[]{1.23f,2.42f,3.22f,0.00f});
        test(new double[]{1.25d,3.22d,4.22d,0.00f});
        List[] list_array = new List[]{DataProvider.getUsers(),DataProvider.getList(2),null,DataProvider.getList(2)};
        test( list_array );
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
        test(Arrays.asList(1,2));
        test(DataProvider.getList(2));
    }
}
