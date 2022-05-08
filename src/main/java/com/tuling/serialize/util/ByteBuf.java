package com.tuling.serialize.util;

import org.apache.log4j.Logger;

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
//    private int increaseSize;
    //扩容的次数对应的序号，从0开始。第一次扩容对应的序号为0
    private int growIndex = 0;

    public ByteBuf() {
        this(Constant.DEFAULT_BUFFER_SIZE);
    }

    public ByteBuf(int initialSize) {
        array = new byte[initialSize];
    }

    public ByteBuf(byte[] array) {
        if (array == null) {
            throw new IllegalArgumentException("array can't be null");
        }
        this.array = array;
        this.writerIndex = array.length;
    }

    public static ByteBuf newInstance(){
        return new ByteBuf(256);
    }

    public void readerIndex(int index) {
        this.readerIndex = index;
    }

    public void decreaseReaderIndex(int decrease){
        this.readerIndex -= decrease;
    }

    public void increaseWriterIndex(int increase){
        this.writerIndex += increase;
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
     * @param value
     */
    public void writeByte(byte value) {
        ensureCapacity(1);
        array[writerIndex] = value;
        writerIndex += 1;
    }

    /**
     * 写入byte数据
     *
     * @param value
     */
    public void writeByte(byte[] value) {
        ensureCapacity(value.length);
        for(int i = 0; i < value.length; i++){
            array[writerIndex + i] = value[i];
        }
        writerIndex += value.length;
    }

    /**
     * 写入byte数据
     *
     * @param value  需要写入的值
     */
    public void writeByte(int value) {
        ensureCapacity(1);
        array[writerIndex] = (byte) value;
        writerIndex += 1;
    }

    /**
     * 写入布尔类型数值
     */
    public void writeBoolean(boolean value) {
        writeByte(value ? 1 : 0);
    }

    /**
     * 写入布尔类型数值
     */
    public void writeBoolean(boolean[] value) {
        ensureCapacity(value.length);
        for(int i = 0; i < value.length; i++){
            array[writerIndex + i] = (byte)(value[i] ? 1 : 0);
        }
        writerIndex += value.length;
    }

    /**
     * 写入字符数据
     */
    public void writeChar(char value) {
        writeShort(value);
    }

    /**
     * 写入字符数组
     */
    public void writeChar(char[] value) {
        for(int i = 0;i < value.length; i++){
            writeShort(value[i]);
        }
    }

    /**
     * 写入字符数据
     */
    public void writeChar(int value) {
        writeShort((int) value);
    }

    /**
     * 采用变长方式写入char型数据
     * @param value   要写入的值
     * @param length  要写入的字节数
     */
    public void writeChar(int value,int length) {
        writeShort(value,length);
    }

    public void writeShort(int value) {
        ensureCapacity(2);

        array[writerIndex] = (byte) (value >> 8);
        array[writerIndex + 1] = (byte) value;
        writerIndex += 2;
    }

    public void writeShort(short[] value) {
        ensureCapacity(2 * value.length);

        for(int i = 0; i < value.length;i++){
            array[writerIndex + 2 * i] = (byte) (value[i] >> 8);
            array[writerIndex + 2 * i + 1] = (byte) value[i];
        }

        writerIndex += 2 * value.length;
    }

    /**
     * 采用变长方式写入short型数据
     * @param value   要写入的值
     * @param length  要写入的字节数
     */
    public void writeShort(int value,int length) {
        ensureCapacity(length);
        if(length == 1){
           array[writerIndex]  = (byte) value;
        }else{
            array[writerIndex] = (byte) (value >> 8);
            array[writerIndex + 1] = (byte) value;
        }
        writerIndex += length;
    }

    public void writeInt(int value) {
        ensureCapacity(4);
        array[writerIndex] = (byte) (value >> 24);
        array[writerIndex + 1] = (byte) (value >> 16);
        array[writerIndex + 2] = (byte) (value >> 8);
        array[writerIndex + 3] = (byte) value;
        writerIndex += 4;
    }

    /**
     * 从指定位置写入整型值
     * @param value
     * @param position
     */
    public void writeInt(int value,int position) {
        array[position] = (byte) (value >> 24);
        array[position + 1] = (byte) (value >> 16);
        array[position + 2] = (byte) (value >> 8);
        array[position + 3] = (byte) value;
    }

    /**
     *
     * 采用变长方式写入int型数据
     * @param value   要写入的值
     * @param length  要写入的字节数
     */
    public void writeAnotherInt(int value) {
//        ensureCapacity(length);
        int length = NumberUtil.getLength(value);
        ensureCapacity(length + 1);
        array[writerIndex] = (byte) length;
        writerIndex++;
        if(length == 1){
            array[writerIndex] = (byte) value;
        }else if(length == 2){
            array[writerIndex] = (byte) (value >> 8);
            array[writerIndex + 1] = (byte) value;
        }else if(length == 3){
            array[writerIndex] = (byte) (value >> 16);
            array[writerIndex + 1] = (byte) (value >> 8);
            array[writerIndex + 2] = (byte) value;
        }else{
            array[writerIndex] = (byte) (value >> 24);
            array[writerIndex + 1] = (byte) (value >> 16);
            array[writerIndex + 2] = (byte) (value >> 8);
            array[writerIndex + 3] = (byte) value;
        }
        writerIndex += length;

    }

    /**
     * 弹性写入整型值，前三位标识要占用的字节数,标识分别为000,001,010,011,100,第四位为符号位，表示正负数；
     * @param value
     */
    public void writeIntWithScala(int value){
        if(value >= 0){
                if(value >> 4 == 0){
                    ensureCapacity(1);
                    array[writerIndex] = (byte)value;
                    writerIndex++;
                }else if(value >> 12 == 0){
                    ensureCapacity(2);
                    array[writerIndex] = (byte)(0x20 | (value >> 8));
                    array[writerIndex + 1] = (byte)value;
                    writerIndex += 2;
                }else if(value >> 20 == 0){
                    ensureCapacity(3);
                    array[writerIndex] = (byte)(0x40 | (value >> 16));
                    array[writerIndex + 1] = (byte)(value >> 8);
                    array[writerIndex + 2] = (byte)value;
                    writerIndex += 3;
                }else if(value >> 28 == 0) {
                    ensureCapacity(4);
                    array[writerIndex] = (byte)(0x60 | (value >> 24));
                    array[writerIndex + 1] = (byte)(value >> 16);
                    array[writerIndex + 2] = (byte)(value >> 8);
                    array[writerIndex + 3] = (byte)value;
                    writerIndex += 4;
                }else{
                    ensureCapacity(5);
                    array[writerIndex] = (byte)0x80;  //  0111 1111  1111 1111 1111 1111 11111101
                    array[writerIndex + 1] = (byte)(value >> 24);
                    array[writerIndex + 2] = (byte)(value >> 16);
                    array[writerIndex + 3] = (byte)(value >> 8);
                    array[writerIndex + 4] = (byte)value;
                    writerIndex += 5;
                }
        }else{
            if(value >> 4 == -1){
                ensureCapacity(1);
                array[writerIndex] = (byte)(0x1f & value);
                writerIndex++;
            }else if(value >> 12 == -1){
                ensureCapacity(2);
                array[writerIndex] = (byte)(0x3f & (value >> 8));
                array[writerIndex + 1] = (byte)value;
                writerIndex += 2;
            }else if(value >> 20 == -1){
                ensureCapacity(3);
                array[writerIndex] = (byte)(0x5f & (value >> 16));
                array[writerIndex + 1] = (byte)(value >> 8);
                array[writerIndex + 2] = (byte)value;
                writerIndex += 3;
            }else if(value >> 28 == -1){
                ensureCapacity(4);
                array[writerIndex] = (byte)(0x7f & (value >> 24));
                array[writerIndex + 1] = (byte)(value >> 16);
                array[writerIndex + 2] = (byte)(value >> 8);
                array[writerIndex + 3] = (byte)value;
                writerIndex += 4;
            }else{
                ensureCapacity(5);
                array[writerIndex] = (byte)0x9f;
                array[writerIndex + 1] = (byte)(value >> 24);
                array[writerIndex + 2] = (byte)(value >> 16);
                array[writerIndex + 3] = (byte)(value >> 8);
                array[writerIndex + 4] = (byte)value;
                writerIndex += 5;
            }
        }
    }

    /**
     * 弹性写入整型数组，前三位标识要占用的字节数,标识分别为000,001,010,011,100,第四位为符号位，表示正负数；
     * @param value
     */
    public void writeIntWithScala(int[] values){
        for(int value : values){
            writeIntWithScala(value);
        }
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

    /**
     *
     * 采用变长方式写入long型数据
     * @param value   要写入的值
     * @param length  要写入的字节数
     */
    public void writeLong(long value,int length) {
        ensureCapacity(length);
        if(length == 1){
            array[writerIndex] = (byte) value;
        }else if(length == 2){
            array[writerIndex] = (byte) (value >> 8);
            array[writerIndex + 1] = (byte) value;
        }else if(length == 3){
            array[writerIndex] = (byte) (value >> 16);
            array[writerIndex + 1] = (byte) (value >> 8);
            array[writerIndex + 2] = (byte) value;
        }else if(length == 4){
            array[writerIndex] = (byte) (value >> 24);
            array[writerIndex + 1] = (byte) (value >> 16);
            array[writerIndex + 2] = (byte) (value >> 8);
            array[writerIndex + 3] = (byte) value;
        }else if(length == 5){
            array[writerIndex] = (byte) (value >> 32);
            array[writerIndex + 1] = (byte) (value >> 24);
            array[writerIndex + 2] = (byte) (value >> 16);
            array[writerIndex + 3] = (byte) (value >> 8);
            array[writerIndex + 4] = (byte) value;
        }else if(length == 6){
            array[writerIndex] = (byte) (value >> 40);
            array[writerIndex + 1] = (byte) (value >> 32);
            array[writerIndex + 2] = (byte) (value >> 24);
            array[writerIndex + 3] = (byte) (value >> 16);
            array[writerIndex + 4] = (byte) (value >> 8);
            array[writerIndex + 5] = (byte) value;
        }
        else if(length == 7){
            array[writerIndex] = (byte) (value >> 48);
            array[writerIndex + 1] = (byte) (value >> 40);
            array[writerIndex + 2] = (byte) (value >> 32);
            array[writerIndex + 3] = (byte) (value >> 24);
            array[writerIndex + 4] = (byte) (value >> 16);
            array[writerIndex + 5] = (byte) (value >> 8);
            array[writerIndex + 6] = (byte) value;
        }
        else{
            array[writerIndex] = (byte) (value >>> 56);
            array[writerIndex + 1] = (byte) (value >>> 48);
            array[writerIndex + 2] = (byte) (value >>> 40);
            array[writerIndex + 3] = (byte) (value >>> 32);
            array[writerIndex + 4] = (byte) (value >>> 24);
            array[writerIndex + 5] = (byte) (value >>> 16);
            array[writerIndex + 6] = (byte) (value >>> 8);
            array[writerIndex + 7] = (byte) value;
        }
        writerIndex += length;

    }

    /**
     * 弹性写入长整型数组
     */
    public void writeLongWithScala(long[] values){
        for(long value : values){
            writeLongWithScala(value);
        }
    }

    /**
     * 弹性写入长整型值。第一字节中的前四位表示值所占字节数，第五位为符号位，其中1到9字节分别由0000,0001,0010,0011,0100,0101,0110,0111,1000表示
     */
    public void writeLongWithScala(long value){

        if(value >= 0){
            if(value >> 3 == 0){
                ensureCapacity(1);
                array[writerIndex] = (byte)value;
                writerIndex++;
            }else if(value >> 11 == 0){
                ensureCapacity(2);
                array[writerIndex] = (byte)(0x10 | (value >> 8));
                array[writerIndex + 1] = (byte)value;
                writerIndex += 2;
            }else if(value >> 19 == 0){
                ensureCapacity(3);
                array[writerIndex] = (byte)(0x20 | (value >> 16));
                array[writerIndex + 1] = (byte)(value >> 8);
                array[writerIndex + 2] = (byte)value;
                writerIndex += 3;
            }else if(value >> 27 == 0) {
                ensureCapacity(4);
                array[writerIndex] = (byte)(0x30 | (value >> 24));
                array[writerIndex + 1] = (byte)(value >> 16);
                array[writerIndex + 2] = (byte)(value >> 8);
                array[writerIndex + 3] = (byte)value;
                writerIndex += 4;
            }else if(value >> 35 == 0){
                ensureCapacity(5);
                array[writerIndex] = (byte)(0x40 | (value >> 32));
                array[writerIndex + 1] = (byte)(value >> 24);
                array[writerIndex + 2] = (byte)(value >> 16);
                array[writerIndex + 3] = (byte)(value >> 8);
                array[writerIndex + 4] = (byte)value;
                writerIndex += 5;
            }else if(value >> 43 == 0){
                ensureCapacity(6);  //1010
                array[writerIndex] = (byte)(0x50 | (value >> 40));
                array[writerIndex + 1] = (byte)(value >> 32);
                array[writerIndex + 2] = (byte)(value >> 24);
                array[writerIndex + 3] = (byte)(value >> 16);
                array[writerIndex + 4] = (byte)(value >> 8);
                array[writerIndex + 5] = (byte)value;
                writerIndex += 6;
            }else if(value >> 51 == 0){
                ensureCapacity(7);  //1100
                array[writerIndex] = (byte)(0x60 | (value >> 48));
                array[writerIndex + 1] = (byte)(value >> 40);
                array[writerIndex + 2] = (byte)(value >> 32);
                array[writerIndex + 3] = (byte)(value >> 24);
                array[writerIndex + 4] = (byte)(value >> 16);
                array[writerIndex + 5] = (byte)(value >> 8);
                array[writerIndex + 6] = (byte)value;
                writerIndex += 7;
            }else if(value >> 59 == 0){
                ensureCapacity(8);
                array[writerIndex] = (byte)(0x70 | (value >> 56));
                array[writerIndex + 1] = (byte)(value >> 48);
                array[writerIndex + 2] = (byte)(value >> 40);
                array[writerIndex + 3] = (byte)(value >> 32);
                array[writerIndex + 4] = (byte)(value >> 24);
                array[writerIndex + 5] = (byte)(value >> 16);
                array[writerIndex + 6] = (byte)(value >> 8);
                array[writerIndex + 7] = (byte)value;
                writerIndex += 8;
            }else{
                ensureCapacity(9);
                array[writerIndex] = (byte)0x80;
                array[writerIndex + 1] = (byte)(value >> 56);
                array[writerIndex + 2] = (byte)(value >> 48);
                array[writerIndex + 3] = (byte)(value >> 40);
                array[writerIndex + 4] = (byte)(value >> 32);
                array[writerIndex + 5] = (byte)(value >> 24);
                array[writerIndex + 6] = (byte)(value >> 16);
                array[writerIndex + 7] = (byte)(value >> 8);
                array[writerIndex + 8] = (byte)value;
                writerIndex += 9;
            }
        }else{
            if(value >> 3 == -1){
                ensureCapacity(1);  //0000 1 111
                array[writerIndex] = (byte)(0x0f & value);
                writerIndex++;
            }else if(value >> 11 == -1){
                ensureCapacity(2); //0001 1 111
                array[writerIndex] = (byte)(0x1f & (value >> 8));
                array[writerIndex + 1] = (byte)value;
                writerIndex += 2;
            }else if(value >> 19 == -1){
                ensureCapacity(3);
                array[writerIndex] = (byte)(0x2f & (value >> 16));
                array[writerIndex + 1] = (byte)(value >> 8);
                array[writerIndex + 2] = (byte)value;
                writerIndex += 3;
            }else if(value >> 27 == -1){
                ensureCapacity(4);
                array[writerIndex] = (byte)(0x3f & (value >> 24));
                array[writerIndex + 1] = (byte)(value >> 16);
                array[writerIndex + 2] = (byte)(value >> 8);
                array[writerIndex + 3] = (byte)value;
                writerIndex += 4;
            }else if(value >> 35 == -1){
                ensureCapacity(5);
                array[writerIndex] = (byte)(0x4f & (value >> 32));
                array[writerIndex + 1] = (byte)(value >> 24);
                array[writerIndex + 2] = (byte)(value >> 16);
                array[writerIndex + 3] = (byte)(value >> 8);
                array[writerIndex + 4] = (byte)value;
                writerIndex += 5;
            }else if(value >> 43 == -1){
                ensureCapacity(6);
                array[writerIndex] = (byte)(0x5f & (value >> 40));
                array[writerIndex + 1] = (byte)(value >> 32);
                array[writerIndex + 2] = (byte)(value >> 24);
                array[writerIndex + 3] = (byte)(value >> 16);
                array[writerIndex + 4] = (byte)(value >> 8);
                array[writerIndex + 5] = (byte)value;
                writerIndex += 6;
            }else if(value >> 51 == -1){
                ensureCapacity(7);
                array[writerIndex] = (byte)(0x6f & (value >> 48));
                array[writerIndex + 1] = (byte)(value >> 40);
                array[writerIndex + 2] = (byte)(value >> 32);
                array[writerIndex + 3] = (byte)(value >> 24);
                array[writerIndex + 4] = (byte)(value >> 16);
                array[writerIndex + 5] = (byte)(value >> 8);
                array[writerIndex + 6] = (byte)value;
                writerIndex += 7;
            }else if(value >> 59 == -1){
                ensureCapacity(8);
                array[writerIndex] = (byte)(0x7f & (value >> 56));
                array[writerIndex + 1] = (byte)(value >> 48);
                array[writerIndex + 2] = (byte)(value >> 40);
                array[writerIndex + 3] = (byte)(value >> 32);
                array[writerIndex + 4] = (byte)(value >> 24);
                array[writerIndex + 5] = (byte)(value >> 16);
                array[writerIndex + 6] = (byte)(value >> 8);
                array[writerIndex + 7] = (byte)value;
                writerIndex += 8;
            }else{
                ensureCapacity(9); //1000 1111
                array[writerIndex] = (byte) 0x8f;
                array[writerIndex + 1] = (byte)(value >> 56);
                array[writerIndex + 2] = (byte)(value >> 48);
                array[writerIndex + 3] = (byte)(value >> 40);
                array[writerIndex + 4] = (byte)(value >> 32);
                array[writerIndex + 5] = (byte)(value >> 24);
                array[writerIndex + 6] = (byte)(value >> 16);
                array[writerIndex + 7] = (byte)(value >> 8);
                array[writerIndex + 8] = (byte)value;
                writerIndex += 9;
            }
        }

    }



    /**
     * 该方法提供int型部分字节的写入赋值
     * @param value  要写入的值
     * @param size  写入的字节数
     */
    private void writeIntBase(int value,int size){
        for(int i = 1;i <= size - 1;i++){
            array[writerIndex + i] = (byte)(value >> (size - 1 - i) * 8);
        }
        writerIndex += size;
    }

    /**
     * 该方法提供long型部分字节的写入赋值
     * @param value  要写入的值
     * @param size  写入的字节数
     *
     */
    private void writeLongBase(long value,int size){
        for(int i = 1;i <= size - 1;i++){
            array[writerIndex + i] = (byte)(value >> (size - 1 - i) * 8);
        }
        writerIndex += size;






    }

    /**
     * 写入浮点数
     * @param value
     */
    public void writeFloat(float value) {
        writeInt(Float.floatToIntBits(value));
    }

    /**
     * 写入浮点数组
     * @param array
     */
    public void writeFloat(float[] array) {
        for(float value : array){
            writeInt(Float.floatToIntBits(value));
        }

    }

    public void writeDouble(double value) {
        writeLong(Double.doubleToLongBits(value));
    }

    public void writeDouble(double[] values) {
        for(double value : values){
            writeLong(Double.doubleToLongBits(value));
        }
    }

    /**
     * 写入字符串,写入的字节是字符串对应的unicode编码
     *
     * @param value
     */
    public void writeString(String value) {
        char[] contentArray = value.toCharArray();
        ensureCapacity(contentArray.length * 2 + 4);
        writeScalableInt(contentArray.length * 2);
        for (int i = 0; i < contentArray.length; i++) {
            array[writerIndex + i * 2] = (byte) (contentArray[i] >> 8);
            array[writerIndex + i * 2 + 1] = (byte) contentArray[i];
        }
        writerIndex += contentArray.length * 2;
    }

    /**
     * 写入字符串或对象的长度。写入内容根据value 的大小占用的字节会不同，从1字节到4字节，首字节的前2位表示该长度共用几字节表示，
     * 00表示1字节，01表示2字节，10表示3字节，11表示4字节
     * @param value 要写入的值
     */
    public void writeScalableInt(int length){
        ensureCapacity(4);
        if(length <= 0x3f){  //第一字节首位 0011 1111   0001 1111
            array[writerIndex] = (byte)length;
            writerIndex += 1;
        }else if(length <= 0x3fff){ //第一字节首位 0100 0000  0011 1111   0011 1111 1111 1111
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
            throw new IllegalArgumentException("value is too long.The max length which is allowed is " + 0x3fffffff);
        }

    }

    /**
     * 读取可伸缩的int型整数
     * @return
     */
    public int readScalableInt() {
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
           char[] contentArray = value.toCharArray();
           int length = contentArray.length;
           ensureCapacity(length  + 4);
           writeScalableInt(length);
           for (int i = 0; i < length; i++) {
               array[writerIndex + i ] = (byte) contentArray[i];
           }
           writerIndex += length;
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
     * 从缓冲中读取一个字符
     * @param length 需要读取的字节长度
     * @return
     */
    public char readChar(int length) {
        return (char)readShort(length);
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
     * 从缓冲中读取一个短整形整数
     * @param  length  整数占用的字节数
     * @return
     */
    public short readShort(int length) {
        readCheck(length);
        short result = 0;
        if(length == 1){
            result = array[readerIndex];
        }else{
            result = (short) ((array[readerIndex] << 8) | (array[readerIndex + 1] & 0xff));
        }
        readerIndex += length;
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
     * 从缓冲中读取一个整数
     * @param length 要读取的字节数
     * @return
     */
    public int readInt(int length) {
        int result = 0;
        readCheck(length);
        if(length == 1){
            result = array[readerIndex];
        }else if(length == 2){
            result = array[readerIndex] << 8 | (array[readerIndex + 1] & 0xff);
        }else if(length == 3){
            result = array[readerIndex] << 16 | (array[readerIndex + 1] & 0xff) << 8 | (array[readerIndex + 2] & 0xff);
        }else{
            result = array[readerIndex] << 24 | (array[readerIndex + 1] & 0xff) << 16 | (array[readerIndex + 2] & 0xff) << 8 | (array[readerIndex + 3] & 0xff);
        }

        readerIndex += length;
        return result;
    }

    /**
     * 弹性读取整型值。前三位标识要占用的字节数,标识分别为000,001,010,011,100,第四位为符号位，表示正负数；
     * @return
     */
    public int readIntWithScala(){
        int result = 0;
        byte first = readByte();
        int flag = first >>> 5;   // 000 0 1101    000 1 0000  //1000 0000
        if((first & 0x10) > 0){
            if(flag == 0){
                // 000 1 1101   111 1 1001
                result = (first | 0xf0) | 0xffffff00;  //00000000 00000000 00000000 1111 1001  => 00000000 00000000 1111 1001 00000000
            }else if(flag == 1){ // 001 1 1001 1100 1101  => //1111  1001 1100     00000000 00000000 0000 0000 00101001
                result = ((first | 0xf0) << 8 | ((array[readerIndex] & 0xff))) | 0xffff0000;
                readerIndex++;
            }else if(flag == 2){
                result = (first | 0xf0) << 16 | (array[readerIndex] & 0xff) << 8 | (array[readerIndex + 1] & 0xff) | 0xff000000;
                readerIndex += 2;
            }else if(flag == 3){
                result = (first | 0xf0) << 24 | (array[readerIndex] & 0xff) << 16 | (array[readerIndex + 1] & 0xff) << 8 | (array[readerIndex + 2] & 0xff);
                readerIndex += 3;
            }else{
                result = (array[readerIndex] & 0xff) << 24 | (array[readerIndex + 1] & 0xff) << 16 | (array[readerIndex + 2] & 0xff) << 8 | (array[readerIndex + 3] & 0xff);
                readerIndex += 4;
            }
        }else{
            if(flag == 0){
                // 000 0 1101   000 1 1001
                result = first;
            }else if(flag == 1){
                //001 0 0011  =>  000 0 0011
                result = ((first & 0x0f) << 8) | (array[readerIndex] & 0xff);
                readerIndex++;
            }else if(flag == 2){
                //010 0 0011  => 000 0 0011
                result =((first & 0x0f) << 16) | (array[readerIndex] & 0xff) << 8 | (array[readerIndex + 1] & 0xff);
                readerIndex += 2;
            }else if(flag == 3){
                result =((first & 0x0f) << 24) | (array[readerIndex] & 0xff) << 16 | (array[readerIndex + 1] & 0xff) << 8 | (array[readerIndex + 2] & 0xff);
                readerIndex += 3;
            }else{
                result = (array[readerIndex] & 0xff) << 24 | (array[readerIndex + 1] & 0xff) << 16 | (array[readerIndex + 2] & 0xff) << 8 | (array[readerIndex + 3] & 0xff);
                readerIndex += 4;
            }
        }
        return result;
    }

    /**
     * 弹性读取长整型值。第一字节中高四位标识要占用的字节数,标识分别为0000,0001,0010,0011,0100,0101,0110,0111,1000,高位中第五位为符号位，表示正负；
     * @return
     */
    public long readLongWithScala(){

        long result = 0;
        byte first = readByte();  //1000 1000
        int flag = first >> 4;   // 0001 0 100       0001 1 111   0000 1000
        //当值为负数时
        if((first & 0x08) > 0){
            if(flag == 0){
                // 000 1 1101   111 1 1001
                result = (first | 0xf8L) | 0xffffffffffffff00L;  //00000000 00000000 00000000 1111 1001  => 00000000 00000000 1111 1001 00000000
            }else if(flag == 1){ // 001 1 1001 1100 1101  => //1111  1001 1100     00000000 00000000 0000 0000 00101001
                result = ((first | 0xf8L) << 8 | ((array[readerIndex] & 0xffL))) | 0xffffffffffff0000L;
                readerIndex++;
            }else if(flag == 2){
                result = (first | 0xf8L) << 16 | (array[readerIndex] & 0xffL) << 8 | (array[readerIndex + 1] & 0xffL) | 0xffffffffff000000L;
                readerIndex += 2;
            }else if(flag == 3){
                result = (first | 0xf8L) << 24 | (array[readerIndex] & 0xffL) << 16 | (array[readerIndex + 1] & 0xffL) << 8 | (array[readerIndex + 2] & 0xffL) | 0xffffffff00000000L;
                readerIndex += 3;
            }else if(flag == 4){
                result = (first | 0xf8L) << 32 | (array[readerIndex] & 0xffL) << 24 | (array[readerIndex + 1] & 0xffL) << 16 | (array[readerIndex + 2] & 0xffL) << 8 | (array[readerIndex + 3] & 0xffL) | 0xffffff0000000000L;
                readerIndex += 4;
            }else if(flag == 5){
                result = (first | 0xf8L) << 40 | (array[readerIndex] & 0xffL) << 32 | (array[readerIndex + 1] & 0xffL) << 24 |
                (array[readerIndex + 2] & 0xffL) << 16 | (array[readerIndex + 3] & 0xffL) << 8 | (array[readerIndex + 4] & 0xffL) | 0xffff000000000000L;
                readerIndex += 5;
            }else if(flag == 6){
                result = (first | 0xf8L) << 48 |  (array[readerIndex] & 0xffL) << 40 | (array[readerIndex + 1] & 0xffL) << 32 |
                        (array[readerIndex + 2] & 0xffL) << 24 | (array[readerIndex + 3] & 0xffL) << 16 | (array[readerIndex + 4] & 0xffL) << 8 |
                        (array[readerIndex + 5] & 0xffL) | 0xff00000000000000L;
                readerIndex += 6;
            }else if(flag == 7){
                result = (first | 0xf8L) << 56 | (array[readerIndex] & 0xffL) << 48 | (array[readerIndex + 1] & 0xffL) << 40 | (array[readerIndex + 2] & 0xffL) << 32 |
                        (array[readerIndex + 3] & 0xffL) << 24 | (array[readerIndex + 4] & 0xffL) << 16 | (array[readerIndex + 5] & 0xffL) << 8 |
                        (array[readerIndex + 6] & 0xffL);
                readerIndex += 7;
            }else{
                result = (array[readerIndex] & 0xffL) << 56 | (array[readerIndex + 1] & 0xffL) << 48 | (array[readerIndex + 2] & 0xffL) << 40 | (array[readerIndex + 3] & 0xffL) << 32 |
                        (array[readerIndex + 4] & 0xffL) << 24 | (array[readerIndex + 5] & 0xffL) << 16 | (array[readerIndex + 6] & 0xffL) << 8 | (array[readerIndex + 7] & 0xffL);
                readerIndex += 8;
            }
        }else{
            if(flag == 0){
                // 000 0 1101   000 1 1001
                result = first;
            }else if(flag == 1){
                //0001 0011  =>  0000 0011
                result = ((first & 0x0fL) << 8) | (array[readerIndex] & 0xffL);
                readerIndex++;
            }else if(flag == 2){
                //010 0 0011  => 000 0 0011
                result =((first & 0x0fL) << 16) | (array[readerIndex] & 0xffL) << 8 | (array[readerIndex + 1] & 0xffL);
                readerIndex += 2;
            }else if(flag == 3){
                result =((first & 0x0fL) << 24) | (array[readerIndex] & 0xffL) << 16 | (array[readerIndex + 1] & 0xffL) << 8 | (array[readerIndex + 2] & 0xffL);
                readerIndex += 3;
            }else if(flag == 4){
                result =((first & 0x0fL) << 32) | (array[readerIndex] & 0xffL) << 24 | (array[readerIndex + 1] & 0xffL) << 16 | (array[readerIndex + 2] & 0xffL) << 8 | (array[readerIndex + 3] & 0xffL);
                readerIndex += 4;
            }else if(flag == 5){
                result =((first & 0x0fL) << 40) | (array[readerIndex] & 0xffL) << 32 | (array[readerIndex + 1] & 0xffL) << 24 | (array[readerIndex + 2] & 0xffL) << 16 | (array[readerIndex + 3] & 0xffL) << 8 | (array[readerIndex + 4] & 0xffL);
                readerIndex += 5;
            }else if(flag == 6){
                result =((first & 0x0fL) << 48) | (array[readerIndex] & 0xffL) << 40 | (array[readerIndex + 1] & 0xffL) << 32 | (array[readerIndex + 2] & 0xffL) << 24 | (array[readerIndex + 3] & 0xffL) << 16 | (array[readerIndex + 4] & 0xffL) << 8 | (array[readerIndex + 5] & 0xffL);
                readerIndex += 6;
            }else if(flag == 7){
                result =((first & 0x0fL) << 56) | (array[readerIndex] & 0xffL) << 48 | (array[readerIndex + 1] & 0xffL) << 40 | (array[readerIndex + 2] & 0xffL) << 32 | (array[readerIndex + 3] & 0xffL) << 24 | (array[readerIndex + 4] & 0xffL) << 16 | (array[readerIndex + 5] & 0xffL) << 8 | (array[readerIndex + 6] & 0xffL);
                readerIndex += 7;
            }else{
                result = (array[readerIndex] & 0xffL) << 56 | (array[readerIndex + 1] & 0xffL) << 48 | (array[readerIndex + 2] & 0xffL) << 40 | (array[readerIndex + 3] & 0xffL) << 32 | (array[readerIndex + 4] & 0xffL) << 24 | (array[readerIndex + 5] & 0xffL) << 16 | (array[readerIndex + 6] & 0xffL) << 8 | (array[readerIndex + 7] & 0xffL);
                readerIndex += 8;  //01111111  11111111
            }
        }
        return result;

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
     * 从缓冲中读取一个长整形整数
     * @param length 需要读取的字节数
     * @return
     */
    public long readLong(int length) {
        readCheck(length);
        long result = 0;
        if(length == 1){
            result = array[readerIndex];
        }else if(length == 2){
            result = array[readerIndex] << 8 | (array[readerIndex + 1] & 0xff);
        }else if(length == 3){
            result = array[readerIndex] << 16 | (array[readerIndex + 1] & 0xff) << 8 | (array[readerIndex + 2] & 0xff);
        }else if(length == 4){
            result = array[readerIndex] << 24 | (array[readerIndex + 1] & 0xff) << 16 | (array[readerIndex + 2] & 0xff) << 8 | (array[readerIndex + 3] & 0xff);
        }else if(length == 5){
            result = ((long)array[readerIndex] << 32) |
                    ((long)(array[readerIndex + 1] & 0xff) << 24) |
                    ((long)(array[readerIndex + 2] & 0xff) << 16) |
                    ((long)(array[readerIndex + 3] & 0xff) << 8) |
                    (long)(array[readerIndex + 4] & 0xff);
        }else if(length == 6){
            result = ((long)array[readerIndex] << 40) |
                    ((long)(array[readerIndex + 1] & 0xff) << 32) |
                    ((long)(array[readerIndex + 2] & 0xff) << 24) |
                    ((long)(array[readerIndex + 3] & 0xff) << 16) |
                    ((long)(array[readerIndex + 4] & 0xff) << 8) |
                    (long)(array[readerIndex + 5] & 0xff);
        }else if(length == 7){
            result = ((long)array[readerIndex] << 48) |
                    ((long)(array[readerIndex + 1] & 0xff) << 40) |
                    ((long)(array[readerIndex + 2] & 0xff) << 32) |
                    ((long)(array[readerIndex + 3] & 0xff) << 24) |
                    ((long)(array[readerIndex + 4] & 0xff) << 16) |
                    ((long)(array[readerIndex + 5] & 0xff) << 8) |
                    (long)(array[readerIndex + 6] & 0xff);
        }else{
            result = ((long)array[readerIndex] << 56) |
                    ((long)(array[readerIndex + 1] & 0xff) << 48) |
                    ((long)(array[readerIndex + 2] & 0xff) << 40) |
                    ((long)(array[readerIndex + 3] & 0xff) << 32) |
                    ((long)(array[readerIndex + 4] & 0xff) << 24) |
                    ((long)(array[readerIndex + 5] & 0xff) << 16) |
                    ((long)(array[readerIndex + 6] & 0xff) << 8) |
                    (array[readerIndex + 7] & 0xff);
        }


        this.readerIndex += length;
        return result;
    }

    /**
     * 读取字符串，字符串对应的字节的长度为length
     * @return
     */
    public String readString() {
        int length = this.readScalableInt();
        if (this.readableBytes() < length) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
        char[] content = new char[length/2];
        String result = null;
        for(int i = 0;i < content.length; i++){
            content[i] = (char) ((array[readerIndex + 2 * i] << 8) | (array[readerIndex + 2 * i + 1] & 0xff));
        }
        result = new String(content);
        readerIndex += length;
        return result;
    }

    /**
     * 读取字符串，字符串对应的字节的长度为length
     * @param isAsciiDecoding 是否用ascii将字节数组解码成字符串
     * @return
     */
    public String readString(boolean isAsciiDecoding) {
        if(!isAsciiDecoding){
            return readString();
        }
        int length = this.readScalableInt();
        if (this.readableBytes() < length) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
        char[] content = new char[length];
        for(int i = 0;i < content.length; i++){
            content[i] = (char) array[readerIndex + i];
        }

        readerIndex += length;
        return new String(content);
    }

    /**
     * 从当前位置处读取指定长度的数据，存放到一个新的ByteBuf中
     * @param length  需要读取长度
     * @return
     */
    public ByteBuf readBuf(int length){
        ByteBuf result = new ByteBuf(this.array);
        result.readerIndex(this.readerIndex());
        result.writerIndex(this.readerIndex() + length);
        readerIndex += length;
        return result;
    }

    /**
     * 跳过要读取的一个字符串,让下次读取从该字符串后面第一字节开始
     */
    public void skipNextString(){
        int length = this.readScalableInt();
        readerIndex += length;
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
     * 保证数组的大小能够满足存储要求
     *
     * @param increaseCapacity  要增加的容量
     */
    private void ensureCapacity(int increaseCapacity) {
        if (increaseCapacity + writerIndex > array.length) {
            grow(array.length > increaseCapacity ? array.length : increaseCapacity);
        }
    }

    /**
     * 读取数据的时候进行检查，检查是否有足够的数据可读
     * @param length 需要读取的数据长度
     */
    private void readCheck(int length){
        if (this.readableBytes() < length) {
            throw new IllegalArgumentException("There are not enough data to be read.");
        }
    }

    /**
     * 当array长度不够时，对数组array进行扩容
     */
    public void grow(int increaseCapacity) {
        byte[] newArray = new byte[array.length + increaseCapacity + getExtraIncreaseCapacity(growIndex++)];
        System.arraycopy(array, 0, newArray, 0, writerIndex);
        array = newArray;
    }

    /**
     * 获取需要增加的容量
     * @param growIndex 当前扩容的次数序号，从0开始
     * @return
     */
    private  int getExtraIncreaseCapacity(int growIndex){
        int result = growIndex + 1;
        for(int i = 0;i <= growIndex; i++ ){
            result *= 2;
        }

        return result;
    }

    private int getNewCapacity(int increaseCapacity) {
        return array.length + increaseCapacity;
    }
}
