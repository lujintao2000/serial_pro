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
        out.write(originalValue,false,output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        ObjectInputStream in = new DefaultObjectInputStream();
        Object obj = null;
        try {
            if(originalValue == null){
                obj = in.readObject(input);
            }else{
                obj = in.readObject(originalValue.getClass(),input);
            }
            System.out.println(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            input.close();
        }
        Assert.assertEquals(TestUtil.isEqual(originalValue,obj),true);

    }

    @Test
    public void testList()  throws Exception{
        List<BaseTypeEnum> list = new ArrayList<>();
        list.add(BaseTypeEnum.BOOLEAN);
        list.add(BaseTypeEnum.BYTE);
        list.add(BaseTypeEnum.BOOLEAN);
        list.add(BaseTypeEnum.BOOLEAN);
        list.add(BaseTypeEnum.BOOLEAN);
        test(list);

        List<Integer> list2 = new LinkedList<>();
        list2.add(1);
        list2.add(3);
        list2.add(5);
        list2.add(null);
        list2.add(4);
        test(list2);
//        test(Arrays.asList(1,2,3,null,5));
    }

    @Test
    public void testEnum() throws Exception{
        test(BaseTypeEnum.CHARACTER);
    }

    @Test
    public void testDomain() throws Exception{
        test(DataProvider.getUser());
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
}
