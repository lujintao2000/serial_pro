package com.tuling.serialize.util;

import com.tuling.serialize.ObjectOutputStream;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.nio.charset.UnsupportedCharsetException;

/**
 * @author lujintao
 * @date 2020-06-26
 */
public class ByteBuf {
    private static final Logger LOGGER = Logger.getLogger(ByteBuf.class);

    private byte[] array;
    //记录最后一次读的位置
    private int readerIndex;
    //记录最后一次写位置
    private int writerIndex;
    //当数组扩容的时候，每次增加的大小
    private int increaseSize;

    public ByteBuf() {
        this(Constant.DEFAULT_BUFFER_SIZE);
    }

    public ByteBuf(int initialSize) {
        array = new byte[initialSize];
        increaseSize = initialSize;
    }

    public ByteBuf(int initialSize,int increaseSize) {
        array = new byte[initialSize];
        this.increaseSize = increaseSize;
    }

    public ByteBuf(byte[] array) {
        if (array == null) {
            throw new IllegalArgumentException("array can't be null");
        }
        this.array = array;
        increaseSize = array.length;
        this.writerIndex = array.length;
    }

    public void readerIndex(int index) {
        this.readerIndex = index;
    }

    public int readerIndex() {
        return this.readerIndex;
    }

    public void writerIndex(int index) {
        this.writerIndex = index;
    }

    public int writerIndex() {
        return this.writerIndex;
    }

    /**
     * 写入byte数据
     *
     * @param content
     */
    public void writeByte(byte value) {
        ensureCapacity(1);
        array[writerIndex] = value;
        writerIndex += 1;
    }

    /**
     * 写入byte数据
     *
     * @param content
     */
    public void writeByte(int value) {
        ensureCapacity(1);
        array[writerIndex] = (byte) value;
        writerIndex += 1;
    }

    /**
     * 写入布尔类型数值
     *
     * @param c
     */
    public void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    /**
     * 写入字符数据
     *
     * @param a
     */
    public void writeChar(char value) {
        writeShort(value);
    }

    /**
     * 写入字符数据
     *
     * @param a
     */
    public void writeChar(int value) {
        writeShort(value);
    }


    public void writeShort(int value) {
        ensureCapacity(2);

        array[writerIndex + 0] = (byte) (value >> 8);
        array[writerIndex + 1] = (byte) value;
        writerIndex += 2;
    }

    public void writeInt(int value) {
        ensureCapacity(4);
        array[writerIndex + 0] = (byte) (value >> 24);
        array[writerIndex + 1] = (byte) (value >> 16);
        array[writerIndex + 2] = (byte) (value >> 8);
        array[writerIndex + 3] = (byte) value;
        writerIndex += 4;
    }

    public void writeLong(long value) {
        ensureCapacity(8);
        array[writerIndex] = (byte) (value >>> 56);
        array[writerIndex + 1] = (byte) (value >>> 48);
        array[writerIndex + 2] = (byte) (value >>> 40);
        array[writerIndex + 3] = (byte) (value >>> 32);
        array[writerIndex + 4] = (byte) (value >>> 24);
        array[writerIndex + 5] = (byte) (value >>> 16);
        array[writerIndex + 6] = (byte) (value >>> 8);
        array[writerIndex + 7] = (byte) value;
        writerIndex += 8;
    }

    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    /**
     * 写入字符串,写入的字节是字符串对应的unicode编码
     *
     * @param value
     */
    public void writeString(String value) {
        char[] contentArray = value.toCharArray();
        ensureCapacity(contentArray.length * 2 + 4);
        writeLength(contentArray.length * 2);
        for (int i = 0; i < contentArray.length; i++) {
            array[writerIndex + i * 2] = (byte) (contentArray[i] >> 8);
            array[writerIndex + i * 2 + 1] = (byte) contentArray[i];
        }
        writerIndex += contentArray.length * 2;
    }

    /**
     * 写入字符串或对象的长度。写入内容根据length 的大小占用的字节会不同，从1字节到4字节，首字节的前2位表示该长度共用几字节表示，
     * 00表示1字节，01表示2字节，10表示3字节，11表示4字节
     * @param length 要写入的长度值
     */
    public void writeLength(int length){
        if(length <= 0x3f){  //第一字节首位 00
            array[writerIndex] = (byte)length;
            writerIndex += 1;
        }else if(length <= 0x3fff){ //第一字节首位 0100 0000  0011 1111
            array[writerIndex] = (byte)((length >> 8) | 0x40);
            array[writerIndex + 1] = (byte)length;
            writerIndex += 2;
        }else if(length <= 0x3fffff){ //第一字节首位 1000 0000  1011 1111
            array[writerIndex] = (byte)((length >> 16) | 0x80);
            array[writerIndex + 1] = (byte)(length >> 8);
            array[writerIndex + 2] = (byte)length;
            writerIndex += 3;
        }else if(length <= 0x3fffffff){ //第一字节首位 1100 0000  0011 1111
            array[writerIndex] = (byte)((length >> 24) | 0xc0);
            array[writerIndex + 1] = (byte)(length >> 16);
            array[writerIndex + 2] = (byte)(length >> 8);
            array[writerIndex + 3] = (byte)length;
            writerIndex += 4;
        }else{
            throw new IllegalArgumentException("Length is too long.The max length which is allowed is " + 0x3fffffff);
        }

    }

    /**
     * 读取字符串或对象的长度
     * @return
     */
    public int readLength() {
        int result = 0;
        byte first = readByte();
        int flag = (first & 0xc0) >> 6;
        if (flag == 0) {  // 首位 00
           result = first;
        } else if (flag == 1){  // 首位 01
            byte second = readByte();
            result = ((first & 0xbf) << 8) | (second & 0xff);
        }else if(flag == 2){   // 首位 10 0111 1111
            byte second = readByte();
            byte third = readByte();
            result = ((first & 0x7f) << 16) |  ((second & 0xff) << 8) | (third & 0xff);
        }else{  // 首位 11  0011 1111
            byte second = readByte();
            byte third = readByte();
            byte forth = readByte();
            result = ((first & 0x3f) << 24) |  ((second & 0xff) << 16) | ((third & 0xff) << 8) | (forth & 0xff);
        }



        return result;
    }

    /**
     * 写入字符串;如果参数isAscii为true,则写入时用ascii对字符串进行编码;否则，用unicode编码
     * @param isAsciiEncoding  是否采用ascii编码
     * @param value
     */
    public void writeString(String value,boolean isAsciiEncoding) {
       if(isAsciiEncoding){

           try {
               byte[] content = value.getBytes("ascii");
               ensureCapacity(content.length + 4);
               this.writeLength(content.length);
               this.writeBytes(content);
           }catch (UnsupportedEncodingException ex){
               LOGGER.error(ex.getMessage(),ex);
               throw new RuntimeException("Write field name with value " + value + " failed.The value contains some character which ascii not support.");
           }
       }else{
           writeString(value);
       }

    }


    /**
     * 写入字节数组
     *
     * @param value
     */
    public void writeBytes(byte[] value) {
        ensureCapacity(value.length);
        System.arraycopy(value, 0, array, writerIndex, value.length);
        writerIndex += value.length;
    }

    /**
     * 将另一个缓冲的数据写到这个缓冲中
     *
     * @param value
     */
    public void writeBytes(ByteBuf value) {
        ensureCapacity(value.readableBytes());
        System.arraycopy(value.array, value.readerIndex, array, writerIndex, value.readableBytes());
        writerIndex += value.readableBytes();
    }

    /**
     * 读取数据的时候略过指定长度的字节
     *
     * @param length
     */
    public void skip(int length) {
        readerIndex += length;
    }

    /**
     * 返回缓冲中所有未读的数据
     *
     * @return
     */
    public byte[] array() {
        byte[] result = new byte[writerIndex - readerIndex];
        System.arraycopy(array, readerIndex, result, 0, writerIndex - readerIndex);
        return result;
    }

    /**
     * 返回缓冲中所有数据
     *
     * @return
     */
    public byte[] fullArray() {
        byte[] result = new byte[writerIndex];
        System.arraycopy(array, 0, result, 0, writerIndex);
        return result;
    }

    /**
     * 读取一个byte型整数
     *
     * @return
     */
    public byte readByte() {
        if (this.readableBytes() < 1) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
        return array[readerIndex++];
    }

    /**
     * 读取一个布尔型数据
     *
     * @return
     */
    public boolean readBoolean() {
        if (this.readableBytes() < 1) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }

        return array[readerIndex++] == (byte) 1 ? true : false;
    }

    /**
     * 从缓冲中读取一个字符
     *
     * @return
     */
    public char readChar() {
        return (char) readShort();
    }

    /**
     * 从缓冲中读取一个短整形整数
     *
     * @return
     */
    public short readShort() {
        short result = 0;
        if (this.readableBytes() < 2) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
        result = (short) ((array[readerIndex] << 8) | (array[readerIndex + 1] & 0xff));
        readerIndex += 2;
        return result;
    }

    /**
     * 从缓冲中读取一个整数
     *
     * @return
     */
    public int readInt() {
        if (this.readableBytes() < 4) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }

        return array[readerIndex++]  << 24 |
                (array[readerIndex++] & 0xff) << 16 |
                (array[readerIndex++] & 0xff) << 8 |
                (array[readerIndex++] & 0xff);
    }

    /**
     * 从缓冲中读取一个长整形整数
     *
     * @return
     */
    public long readLong() {
        if (this.readableBytes() < 8) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }

        long result = 0L;
        result = ((long)array[readerIndex] << 56) |
                ((long)(array[readerIndex + 1] & 0xff) << 48) |
                ((long)(array[readerIndex + 2] & 0xff) << 40) |
                ((long)(array[readerIndex + 3] & 0xff) << 32) |
                ((long)(array[readerIndex + 4] & 0xff) << 24) |
                ((long)(array[readerIndex + 5] & 0xff) << 16) |
                ((long)(array[readerIndex + 6] & 0xff) << 8) |
                (array[readerIndex + 7] & 0xff);
        readerIndex += 8;
        return result;
    }

    /**
     * 读取字符串，字符串对应的字节的长度为length
     *
     * @param length
     * @return
     */
    public String readString() {
        int length = this.readLength();
        if (this.readableBytes() < length) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
        byte[] content = new byte[length];
        System.arraycopy(array, readerIndex, content, 0, length);
        String result = null;
        try {
            result = new String(content, "unicode");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("读取字符串出错|" + ex.getMessage(), ex);
        }
        readerIndex += length;
        return result;
    }

    /**
     * 读取字符串，字符串对应的字节的长度为length
     *
     * @param length
     * @param isAsciiDecoding 是否用ascii将字节数组解码成字符串
     * @return
     */
    public String readString(boolean isAsciiDecoding) {
        if(!isAsciiDecoding){
            return readString();
        }
        int length = this.readLength();
        if (this.readableBytes() < length) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
        byte[] content = new byte[length];
        System.arraycopy(array, readerIndex, content, 0, length);
        String result = null;
        try {
            result = new String(content, "ascii");
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error("读取字符串出错|" + ex.getMessage(), ex);
            throw new RuntimeException(ex);
        }
        readerIndex += length;
        return result;
    }

    /**
     * 读取浮点数
     *
     * @return
     */
    public float readFloat() {
        int m = readInt();
        return Float.intBitsToFloat(m);
    }

    /**
     * 读取双精度浮点数
     *
     * @return
     */
    public double readDouble() {
        return Double.longBitsToDouble(readLong());
    }

    /**
     * @return
     */
    public int readableBytes() {
        return writerIndex - readerIndex;
    }


    /**
     * 重置缓冲
     */
    public void reset() {
        this.readerIndex = 0;
        this.writerIndex = 0;
    }

    /**
     * 释放空间
     */
    public void release() {
        this.reset();
        this.array = null;
    }

    /**
     * 保证数组的大小不小于capacity
     *
     * @param capacity
     */
    private void ensureCapacity(int increaseCapacity) {
        if (increaseCapacity + writerIndex > array.length) {
            grow(increaseCapacity);
        }
    }

    /**
     * 当array长度不够时，对数组array进行扩容
     */
    private void grow(int increaseCapacity) {
        byte[] newArray = new byte[getNewCapacity(increaseCapacity)];
        System.arraycopy(array, 0, newArray, 0, writerIndex);
        array = newArray;
    }

    private int getNewCapacity(int increaseCapacity) {
        return array.length + increaseCapacity + increaseSize ;
    }
}
