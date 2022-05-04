package com.tuling.serialize;

import org.junit.Assert;
import org.junit.Test;
import java.io.*;

/**
 * @author lujintao
 * @date 2020-06-09
 */
public class SerializeTest2 extends  BaseTest{

    @Override
    protected void test(Object originalValue) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Serial.write(originalValue,output);
        output.close();
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        ObjectInputStream in = new DefaultObjectInputStream();
        Object obj = null;
        try {
            obj = Serial.read(input);
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
