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
public class SerializeTest4 extends BaseTest{
    @Override
    protected void test(Object originalValue) throws Exception {
//        ByteArrayOutputStream output = new ByteArrayOutputStream();
        OutputStream output = new FileOutputStream("e:\\list.obj");

        ObjectOutputStream out = new CompatibleObjectOutputStream();
        out.write(originalValue, output);
        output.close();


//        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());

        InputStream input = new FileInputStream("e:\\list.obj");
        ObjectInputStream in = new CompatibleObjectInputStream();
        Object obj = null;
        try {
            obj = in.readObject(input);
            System.out.println(obj);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            input.close();
        }
        Assert.assertEquals(TestUtil.isEqual(originalValue, obj), true);
    }

    @Test
    public void testDomain() throws Exception{
        test(DataProvider.getUser());
    }

}
