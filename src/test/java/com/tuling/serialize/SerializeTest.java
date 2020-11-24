package com.tuling.serialize;

import com.sun.javafx.image.ByteToBytePixelConverter;
import com.tuling.domain.*;
import com.tuling.serialize.exception.InvalidDataFormatException;
import com.tuling.serialize.util.BuilderUtil;
import com.tuling.serialize.util.ByteBuf;
import com.tuling.serialize.util.Constant;
import org.junit.Assert;
import org.junit.Test;
import sun.reflect.generics.tree.BaseType;

import javax.xml.crypto.Data;
import java.io.*;
import java.lang.reflect.Array;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author lujintao
 * @date 2020-06-09
 */
public class SerializeTest extends BaseTest{
    @Override
    protected void test(Object originalValue) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new DefaultObjectOutputStream( );
        out.write(originalValue,true,output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        ObjectInputStream in = new DefaultObjectInputStream();
        Object obj = null;
        try {
//            if(originalValue == null){
                obj = in.readObject(input);
//            }else{
//                obj = in.readObject(originalValue.getClass(),input);
//            }
            System.out.println(obj);
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
        Object[] array = new Object[]{"aa",1,2L,true};
        test(array);
    }

    @Test
    public void testList()  throws Exception{
//        List<Integer> list = new ArrayList<>();
//        list.add(1);
//        list.add(3);
//        list.add(5);
//        list.add(null);
//        list.add(4);
//        test(list);

//        list = new LinkedList<>();
//        list.add(1);
//        list.add(3);
//        list.add(5);
//        list.add(null);
//        list.add(4);
//        test(list);
//        test(Arrays.asList(1,2,3,null,5));
          List<String> list = new ArrayList<>();
          list.add("women");
          list.add("man");
          test(list);
    }
}
