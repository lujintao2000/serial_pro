package com.tuling.serialize.util;

/**
 * Created by Administrator on 2020-06-12.
 */
public class Constant {

    //代表写对象开始
    public static final int BEGIN_FLAG = 127;
    //代表写对象结束
    public static final int END_FLAG = 126;
    //代表写入了NULL
    public static final byte NULL_FLAG = -1;  //0x1111 1111

    //代表写入了非空
    public static final int NOT_NULL_FLAG = 0; // 0x0111 1111

    //代表写入了类名
    public static final byte WRITE_CLASS_NAME_FLAG = 63;  //0x0011 1111

    //char,short,int,long,boolean
    //在writeValue的时候，数据表示为 0x01 000 00 0,其中第1位0表示非空，紧接着的4位表示数据类型，再后3节表示数据长度

    //表示当前序列化操作是应用的第一次序列化
    public static final byte FIRST_FLAG = -128;  // 0x0 0001 000

    //在writeValue写入的第一字节中，标识boolean类型数据的字节表示(后2位为0)
    public static final byte BOOLEAN_FLAG = 0x08;  // 0x0 0001 000
    //在writeValue写入的第一字节中，标识char类型数据的字节表示(后2位为0)
    public static final byte CHAR_FLAG = 0x10;    //  0x0 0010 000
    //在writeValue写入的第一字节中，标识short类型数据的字节表示(后2位为0)
    public static final byte SHORT_FLAG = 0x18;   //  0x0 0011 000
    //在writeValue写入的第一字节中，标识int类型数据的字节表示(后2位为0)
    public static final byte INT_FLAG = 0x20;     //  0x0 0100 000
    //在writeValue写入的第一字节中，标识long类型数据的字节表示(后2位为0)
    public static final byte LONG_FLAG = 0x28;    //  0x0 0101 000

    //在writeValue写入的第一字节中，标识long类型数据的字节表示(后2位为0)
    public static final byte BYTE_FLAG = 0x30;    //  0x0 0110 000

    //在writeValue写入的第一字节中，标识long类型数据的字节表示(后2位为0)
    public static final byte FLOAT_FLAG = 0x38;    //  0x0 0111 000

    //在writeValue写入的第一字节中，标识long类型数据的字节表示(后2位为0)
    public static final byte DOUBLE_FLAG = 0x40;    // 0x0 1000 000

    //在writeValue写入的第一字节中，标识String类型数据的字节表示(后2位为0)
    public static final byte STRING_FLAG = 0x48;    // 0x0 1001 000

    //在writeValue写入的第一字节中，标识Enum类型数据的字节表示(后2位为0)
    public static final byte ENUM_FLAG = 0x50;      //  0x0 1010 000

    //在writeValue写入的第一字节中，标识非boolean,char,short,int,long类型数据的字节表示(后2位为0)
    public static final byte OTHER_FLAG = 0x58;     // 0x0 1011 000
    //代表写入了CONTINUE,循环还要进行
    public static final int CONTINUE_FLAG = 123;
    //代表当前要写入的值是之前已经写入流中的对象的引用
    public static final int REFERENCE_FLAG = 122;
    //代表当前要写入的值是完整信息，并非之前已经写入流中的对象的引用,且需要将值对应类型写入流中
    public static final int NORMAL_CONTAIN_CLASSNAME_FLAG = 121;

    //代表当前要写入的值是完整信息，并非之前已经写入流中的对象的引用，且无需将值类型写入流中，这种情况对应值类型和值所属属性的类型相同的情形
    public static final int NORMAL_WITHOUT_CLASSNAME_FLAG = 120;
    //当前序列化格式的版本
    public static final short CURRENT_VERSION = 1;
    //当前序列化实现支持的序列化格式的最小版本
    public static final short MIN_VERSION = 1;
    //当前序列化实现支持的序列化格式的最大版本
    public static final short MAX_VERSION = 1;
    //ByteBuf对象创建默认的大小
    public static final int DEFAULT_BUFFER_SIZE = 256;
    //存储字段内容的临时ByteBuf的默认大小
    public static final int DEFAULT_BUFFER_SIZE_OF_FIELD = 32;
    //标识类名引用了先前已经缓存的类名 1000 0000 //1011 1111
    public static final int CLASSNAME_REFERENCE =  0x80;
    //在序列化的时候，当类引用序号超过31时，写入的序号需要与之作与计算的数
    public static final int CLASSNAME_REFERENCE_OVER_FLOW = CLASSNAME_REFERENCE & 0x2f;
    //标识类名和字段的类型名一致 1100 0000
    public static final byte CLASSNAME_SAME_WITH_FIELD = (byte)0xc0;

    //在序列化码流中用该字节表示布尔型  1100 0001
    public static final byte BOOLEAN = (byte) 0xc1;
    //在序列化码流中用该字节表示字节类型
    public static final byte BYTE = (byte) 0xc2;
    //在序列化码流中用该字节表示字符类型
    public static final byte CHARACTER = (byte) 0xc3;
    //在序列化码流中用该字节表示短整型
    public static final byte SHORT = (byte) 0xc4;
    //在序列化码流中用该字节表示整型
    public static final byte INTEGER = (byte)0xc5;
    //在序列化码流中用该字节表示长整型
    public static final byte LONG = (byte) 0xc6;
    //在序列化码流中用该字节表示浮点型
    public static final byte FLOAT = (byte) 0xc7;
    //在序列化码流中用该字节表示双精度型
    public static final byte DOUBLE = (byte) 0xc8;
    //在序列化码流中用该字节表示字符串类型 //0x1100 0000
    public static final byte STRING = (byte) 0xc9;
}
