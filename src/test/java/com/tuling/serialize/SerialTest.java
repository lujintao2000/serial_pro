package com.tuling.serialize;

import com.tuling.domain.User;
import org.junit.Assert;
import org.junit.Test;
import java.io.*;

/**
 * Created by ljt on 2020-06-24.
 */
public class SerialTest {

    private void test(Object originalObj, boolean isWriteClassName){
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Object obj = null;
        InputStream inputStream = null;
        try{
            Serial.write(originalObj,outputStream,isWriteClassName);
            outputStream.close();
            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            if(!isWriteClassName){
                obj = Serial.read(inputStream,(originalObj != null) ? originalObj.getClass() : null );
            }else{
                obj = Serial.read(inputStream);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        Assert.assertEquals(TestUtil.isEqual(originalObj,obj),true);
    }

    private void test(Object originalObj, boolean isWriteClassName,boolean isCompatible){
        FileOutputStream outputStream = null;
        Object obj = null;
        InputStream inputStream = null;
        try{
            outputStream = new FileOutputStream("e:\\list.obj");
            Serial.write(originalObj,outputStream,isWriteClassName,isCompatible);
            outputStream.close();
//            inputStream = new ByteArrayInputStream(outputStream.toByteArray());
            inputStream = new FileInputStream("e:\\list.obj");
            if(!isWriteClassName){
                obj = Serial.read(inputStream,isCompatible,(originalObj != null) ? originalObj.getClass() : null );
            }else{
                obj = Serial.read(inputStream,isCompatible);
            }

        }catch (Exception ex){
            ex.printStackTrace();
        }finally {
            if(inputStream != null){
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


        }
        Assert.assertEquals(TestUtil.isEqual(originalObj,obj),true);
    }

    @Test
    public void testSerialWithDefault() throws Exception{
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//        OutputStream outputStream = new FileOutputStream("e:\\list.obj");

        Object originalObj = DataProvider.getUser();
        Object obj = null;
        try{
            Serial.write(originalObj,outputStream);
            outputStream.close();
            obj = Serial.read(new ByteArrayInputStream(outputStream.toByteArray()));

        }catch (Exception ex){
            ex.printStackTrace();
        }
        Assert.assertEquals(TestUtil.isEqual(originalObj,obj),true);
    }

    @Test
    public void testFull(){
        User user = DataProvider.getUser();
        test(user,true,true);
        test(user,true,false);
        test(user,false,true);
        test(user,false,false);
    }

    @Test
    public void testSerialWithWriteClassName(){
        User user = DataProvider.getUser();
        test(user,true);
        test(user,false);
    }


}
