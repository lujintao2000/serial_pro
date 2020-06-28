package com.tuling.serialize;

import com.tuling.serialize.util.ByteBuf;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2020-06-28.
 */
public class ByteBufTest {

    public void base(TestType type,Number originalValue){
        base(type,originalValue,null);
    }

    public void base(TestType type,Number originalValue,Object originalObject){
        ByteBuf buf = new ByteBuf();
        switch (type){
            case SHORT:
                buf.writeShort(originalValue.shortValue());
                Assert.assertEquals(buf.readShort(),originalValue.shortValue());
                break;
            case INT:
                buf.writeInt(originalValue.intValue());
                Assert.assertEquals(buf.readInt(),originalValue.intValue());
                break;
            case LONG:
                buf.writeLong(originalValue.longValue());
                Assert.assertEquals(buf.readLong(),originalValue.longValue());
                break;
            case FLOAT:
                buf.writeFloat(originalValue.floatValue());
                float t = buf.readFloat();
                Assert.assertTrue(t == originalValue.floatValue());
                break;
            case DOUBLE:
                buf.writeDouble(originalValue.doubleValue());
                Assert.assertTrue(buf.readDouble() == originalValue.doubleValue());
                break;
            case BYTE:
                buf.writeByte((byte)originalObject);
                Assert.assertEquals(buf.readByte(),(byte)originalObject);
                break;
            case CHAR:
                buf.writeChar((char)originalObject);
                Assert.assertEquals(buf.readChar(),(char)originalObject);
                break;
            case BOOLEAN:
                buf.writeBoolean((boolean)originalObject);
                Assert.assertTrue(buf.readBoolean() == (boolean)originalObject);
                break;
            default:

        }

    }

    @Test
    public void testShort(){
        base(TestType.SHORT,20);
        base(TestType.SHORT,384);
        base(TestType.SHORT,385);
        base(TestType.SHORT,23000);
        base(TestType.SHORT,-188);
        base(TestType.SHORT,-539);
        base(TestType.SHORT,0);
        base(TestType.SHORT,Short.MAX_VALUE);
        base(TestType.SHORT,Short.MIN_VALUE);
    }

    @Test
    public void testInt(){
        base(TestType.INT,20);
        base(TestType.INT,384);
        base(TestType.INT,385);
        base(TestType.INT,23000);

        base(TestType.INT,65532);

        base(TestType.INT,65538);

        base(TestType.INT,23000);

        base(TestType.INT,-188);
        base(TestType.INT,-539);
        base(TestType.INT,0);
        base(TestType.INT,Long.MAX_VALUE);
        base(TestType.INT,Long.MIN_VALUE);
    }

    @Test
    public void testLong(){
        base(TestType.LONG,20);
        base(TestType.LONG,384);
        base(TestType.LONG,385);
        base(TestType.LONG,23000);

        base(TestType.LONG,65532);

        base(TestType.LONG,65538);

        base(TestType.LONG,23000);

        base(TestType.LONG,-188);
        base(TestType.LONG,-539);
        base(TestType.LONG,0);
        base(TestType.LONG,Long.MAX_VALUE);
        base(TestType.LONG,Long.MIN_VALUE);
    }


    @Test
    public void testFloat(){
        base(TestType.FLOAT,20.325f);
        base(TestType.FLOAT,342.122f);
        base(TestType.FLOAT,3850.222f);
        base(TestType.FLOAT,23000.00f);

        base(TestType.FLOAT,65532.322f);

        base(TestType.FLOAT,-65538.534f);

        base(TestType.FLOAT,-23000.24f);

        base(TestType.FLOAT,-188.456f);
        base(TestType.FLOAT,-539.123f);
        base(TestType.FLOAT,0);
        base(TestType.FLOAT,Float.MAX_VALUE);
        base(TestType.FLOAT,Float.MIN_VALUE);
    }

    @Test
    public void testDouble(){
        base(TestType.DOUBLE,20.325d);
        base(TestType.DOUBLE,342.122d);
        base(TestType.DOUBLE,3850.222d);
        base(TestType.DOUBLE,23000.00d);

        base(TestType.DOUBLE,65532.322d);

        base(TestType.DOUBLE,-65538.534d);

        base(TestType.DOUBLE,-23000.24d);

        base(TestType.DOUBLE,-188.456d);
        base(TestType.DOUBLE,-539.123d);
        base(TestType.DOUBLE,0);
        base(TestType.DOUBLE,Long.MAX_VALUE);
        base(TestType.DOUBLE,Long.MIN_VALUE);
    }

    @Test
    public void testByte(){
        base(TestType.BYTE,null,(byte)3);
        base(TestType.BYTE,null,(byte)0);
        base(TestType.BYTE,null,(byte)-1);
        base(TestType.BYTE,null,(byte)-128);
        base(TestType.BYTE,null,(byte)127);
    }

    @Test
    public void testBoolean(){
        base(TestType.BOOLEAN,null,true);
        base(TestType.BOOLEAN,null,false);
    }

    @Test
    public void testChar(){
        base(TestType.CHAR,null,(char)127);
        base(TestType.CHAR,null,(char)256);
        base(TestType.CHAR,null,(char)3333);
    }

    @Test
    public void testGrow(){
        ByteBuf buf = new ByteBuf(10);
        String content = "你要去哪里";
        for(int i = 0;i < 10;i++){
            buf.writeString(content);
        }
        for(int i = 0;i < 10;i++){
//            System.out.println(buf.readString(10));
            Assert.assertEquals(content,buf.readString(10));
        }
    }

    @Test
    public void testString(){
        ByteBuf buf = new ByteBuf();
        String src = "你在哪里啊";
        buf.writeString(src);
        Assert.assertEquals("",src,buf.readString(src.length() * 2));
        Assert.assertTrue(buf.readableBytes() == 0);
    }

    @Test
    public void testWriteBytes(){
        ByteBuf buf = new ByteBuf(30);
        for(int i = 0;i < 20;i++){
            buf.writeBytes("hello".getBytes());
        }
    }

    @Test
    public void testWriteBytesWithBuf(){
        ByteBuf buf = new ByteBuf(128);
        String content = "hellohello";
        for(int i = 0;i < 30000;i++){
            buf.writeBytes(new ByteBuf(content.getBytes()));
        }
//        Assert.assertTrue(buf.readableBytes() == 20 * content.getBytes().length);
    }

    @Test
    public void testArray() throws Exception{
        ByteBuf buf = new ByteBuf(30);
        buf.writeString("hello");
        buf.writeString("hello");
        String t = buf.readString(10);
        String result = new String(buf.array(),"unicode");
        Assert.assertTrue("hello".equals(result));
    }

    @Test
    public void testFullArray() throws  Exception{
        ByteBuf buf = new ByteBuf(30);
        buf.writeString("hello");
        buf.writeString("hello");
        Assert.assertTrue("hellohello".equals(new String(buf.fullArray(),"unicode")));
    }




    enum  TestType{
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        BYTE,
        BOOLEAN,
        CHAR;
    }
}
