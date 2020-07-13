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
public class SerializeTest2 extends  BaseTest{

    @Override
    protected void test(Object originalValue) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
//        OutputStream output = new FileOutputStream("e:\\list.obj");

        ObjectOutputStream out = new DefaultObjectOutputStream( );
        out.write(originalValue,output);
        output.close();

        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

//        InputStream input = new FileInputStream("e:\\list.obj");
        ObjectInputStream in = new DefaultObjectInputStream();
        Object obj = null;
        try {
            obj = in.readObject(input);
            System.out.println(obj);
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            input.close();
        }
        Assert.assertEquals(TestUtil.isEqual(originalValue,obj),true);
    }


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

}
