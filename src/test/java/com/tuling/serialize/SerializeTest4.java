package com.tuling.serialize;

import org.junit.Assert;
import org.junit.Test;
import java.io.*;

/**
 * @author lujintao
 * @date 2020-06-09
 */
public class SerializeTest4 extends BaseTest{
    @Override
    protected void test(Object originalValue) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ObjectOutputStream out = new CompatibleObjectOutputStream();
        Serial.write(originalValue, output,true,true);
        output.close();
        ByteArrayInputStream input = new ByteArrayInputStream(output.toByteArray());
        Object obj = null;
        try {
            obj = Serial.read(input,true);
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
