package com.tuling.serialize;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import com.tuling.serialize.util.ByteBuf;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

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
        base(TestType.SHORT,-1);
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
            Assert.assertEquals(content,buf.readString());
        }
    }

    @Test
    public void testString(){
        ByteBuf buf = new ByteBuf();
        String src = "你在哪里啊";
        buf.writeString(src);
        Assert.assertEquals("",src,buf.readString());
        Assert.assertTrue(buf.readableBytes() == 0);
    }

    @Test
    public void testWriteString(){
        ByteBuf buf = new ByteBuf();
        for(int i = 0;i < 10000; i++){
            buf.writeString("good morning");
        }
        for(int i = 0;i < 10000; i++){
//            int length = buf.readLengthOfString();
            Assert.assertTrue("good morning".equals(buf.readString()));
        }

    }

    @Test
    public void testWriteStringWithAscii(){
        ByteBuf buf = new ByteBuf();
        for(int i = 0;i < 10000; i++){
            buf.writeString("good morning",true);
        }
        for(int i = 0;i < 10000; i++){
//            int length = buf.readLengthOfString();
            Assert.assertTrue("good morning".equals(buf.readString(true)));
        }
    }

    @Test
    public void testWriteBytes(){
        ByteBuf buf = new ByteBuf(30);
        for(int i = 0;i < 200;i++){
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
        String t = buf.readString();
        String result = new String(buf.array(),"unicode");
//        Assert.assertTrue("hello".equals(result));
    }

    @Test
    public void testFullArray() throws  Exception{
        ByteBuf buf = new ByteBuf(30);
        buf.writeString("hello");
        buf.writeString("hello");
//        Assert.assertTrue("hellohello".equals(new String(buf.fullArray(),"unicode")));
    }


    public final short readFieldCount(ByteBuf in) throws IOException {
        short result = in.readByte();
        if(result < 0){
            result += 128;
        }else{
            result = (short)((result << 8) | (in.readByte() & 0xff));
        }
        return result;
    }

    protected final void writeFieldCount(int length,ByteBuf buf){
        if(length <= 127){
            buf.writeByte(length - 128);
        }else{
            buf.writeShort(length);
        }
    }


    public void compareWriteReadOfFieldCount(int length) throws  IOException{
        ByteBuf buf = new ByteBuf();
        writeFieldCount(length,buf);

        int readLength = readFieldCount(buf);
        Assert.assertTrue(length == readLength);
    }

    @Test
    public void testFieldCount() throws IOException{
        compareWriteReadOfFieldCount(0);
        compareWriteReadOfFieldCount(1);
        compareWriteReadOfFieldCount(50);
        compareWriteReadOfFieldCount(127);
        compareWriteReadOfFieldCount(128);
        compareWriteReadOfFieldCount(255);
        compareWriteReadOfFieldCount(256);
        compareWriteReadOfFieldCount(2034);
    }

    @Test
    public void testWriteReadStringWithAscii(){
        ByteBuf buf = new ByteBuf();
        buf.writeString("name",true);
        Assert.assertTrue(buf.readableBytes() == 5);
//        int length = buf.readLengthOfString();
        String value = buf.readString(true);
        Assert.assertEquals("name",value);

    }

    @Test
    public void testWriteLengthOfString(){
        writeLengthOfString(62);
        writeLengthOfString(63);
        writeLengthOfString(64);

        writeLengthOfString(0x3fff - 2);
        writeLengthOfString(0x3fff - 1);
        writeLengthOfString(0x3fff);

        writeLengthOfString(0x3fffff - 2);
        writeLengthOfString(0x3fffff - 1);
        writeLengthOfString(0x3fffff);


        writeLengthOfString(0x3fffffff - 2);
        writeLengthOfString(0x3fffffff - 1);
        writeLengthOfString(0x3fffffff);
    }

    @Test
    public void testAllWrite(){
        ByteBuf buf = new ByteBuf(128);

        for(int i = 0;i < 100000;i++){
            buf.writeChar((char)520);
            buf.writeChar(3020);
            buf.writeByte((byte) 117);
            buf.writeByte(39933);
            buf.writeBoolean(true);
            buf.writeShort(38838);
            buf.writeInt(883838);
            buf.writeLong(82828882L);
            buf.writeFloat(28822.20f);
            buf.writeDouble(8333883.8282d);
            buf.writeString("你在干什么？");
            buf.writeString("good morning",true);
            buf.writeBytes(new byte[]{20,30,35,63,78,88,99,102,33,55,87,98,60});

            buf.writeBytes(new ByteBuf(new byte[]{33,55,44,39,115,35,56,88,99}));
            buf.writeLength(393993);

        }

    }

    public ByteBuf writeLengthOfString(int length){
        ByteBuf buf  =  new ByteBuf();
        buf.writeLength(length);
        return buf;
    }

    @Test
    public  void testReadLengthOfString(){
        Assert.assertTrue(isReadLengthEqualWriteLength(62));
        Assert.assertTrue(isReadLengthEqualWriteLength(63));
        Assert.assertTrue(isReadLengthEqualWriteLength(64));
        Assert.assertTrue(isReadLengthEqualWriteLength(65));

        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fff - 2));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fff - 1));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fff));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fff + 1));

        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffff - 2));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffff - 1));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffff));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffff + 1));


        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffffff - 2));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffffff - 1));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffffff));

        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffffff - 5));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffffff - 6));
        Assert.assertTrue(isReadLengthEqualWriteLength(0x3fffffff - 200));
    }

    public boolean isReadLengthEqualWriteLength(int length){
        ByteBuf buf  =  new ByteBuf();
        buf.writeLength(length);
        int readLength = buf.readLength();
        return length == readLength;

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
