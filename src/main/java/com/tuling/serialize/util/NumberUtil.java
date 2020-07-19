package com.tuling.serialize.util;

public class NumberUtil {

	
	   // long转换为byte[8]数组
    public static byte[] getByteArray(long l) {
        byte b[] = new byte[8];
        b[0] = (byte)  (0xff & (l >> 56));
        b[1] = (byte)  (0xff & (l >> 48));
        b[2] = (byte)  (0xff & (l >> 40));
        b[3] = (byte)  (0xff & (l >> 32));
        b[4] = (byte)  (0xff & (l >> 24));
        b[5] = (byte)  (0xff & (l >> 16));
        b[6] = (byte)  (0xff & (l >> 8));
        b[7] = (byte)  (0xff & l);
        return b;
    }



	   // int转换为byte[8]数组
	 public static byte[] getByteArray(int a) {
	     byte b[] = new byte[4];
	     b[0] = (byte)  (a >> 24);
	     b[1] = (byte)  (a >> 16);
	     b[2] = (byte)  (a >> 8);
	     b[3] = (byte)  a;
	     return b;
	 }

     public static long getLong(byte[] array){
    	if(array == null || array.length != 8){
    		throw new RuntimeException();
    	}
    	int factor = (array[0] < 0) ? -1 : 1;
    	//标识该数是否是负数
    	boolean negative = (factor == -1);
    	long result = factor * (
    	          NumberUtil.getInteger(array[0], negative,false) * (long)Math.pow(256, 7)
    			+ NumberUtil.getInteger(array[1], negative,false) * (long)Math.pow(256, 6)
    			+ NumberUtil.getInteger(array[2], negative,false) * (long)Math.pow(256, 5)
    			+ NumberUtil.getInteger(array[3], negative,false) * (long)Math.pow(256, 4)
    			+ NumberUtil.getInteger(array[4], negative,false) * (long)Math.pow(256, 3)
    			+ NumberUtil.getInteger(array[5], negative,false) * (long)Math.pow(256, 2)
    			+ NumberUtil.getInteger(array[6], negative,false) * (long)Math.pow(256, 1)
    			+ NumberUtil.getInteger(array[7], negative,true)
    			);

    	return result;
     }

     public static int getInteger(byte[] array){
    	if(array == null || array.length != 4){
    		throw new RuntimeException();
    	}
    	int factor = (array[0] < 0) ? -1 : 1;
    	//标识该数是否是负数
    	boolean negative = (factor == -1);
    	int result = factor * ((NumberUtil.getInteger(array[0], negative,false)) * (int)Math.pow(256, 3)
    			+ NumberUtil.getInteger(array[1], negative,false) * (int)Math.pow(256, 2)
    			+ NumberUtil.getInteger(array[2], negative,false) * (int)Math.pow(256, 1)
    			+ NumberUtil.getInteger(array[3], negative,true));

    	return result;
     }

     public static int getShort(byte[] array){
    	if(array == null || array.length != 2){
    		throw new RuntimeException();
    	}
    	int factor = (array[0] < 0) ? -1 : 1;
    	//标识该数是否是负数
    	boolean negative = (factor == -1);
    	int result = factor * ((NumberUtil.getInteger(array[0], negative,false)) * (int)Math.pow(256, 1)
    			+ NumberUtil.getInteger(array[1], negative,true));

    	return result;
     }

     /**
      *
      * @param a
      * @param isNegative  该字节是否是来源于负数的字节
      * @isLast 标识是否是负数的最后一个字节
      * @return
      */
     private static int getInteger(byte a,boolean negative,boolean isLast){

    	 if(negative){
    		  byte b = (isLast) ? (byte)((a ^ 0xff) + 1) : (byte)(a ^ 0xff) ;

    		  return converByteToInt(b);

    	 }else{
    		 return  converByteToInt(a);
    	 }
     }

     /**
      * 将一个字节转变成范围在（0-255）范围内的整数
      * @param a
      * @return
      */
     public static int converByteToInt(byte a){
    	 return (a >= 0) ? a : (256 + a);
     }

	   // short 转换为byte[8]数组
	 public static byte[] getByteArray(short a) {
	     byte b[] = new byte[2];
	     b[0] = (byte)  (a >> 8);
	     b[1] = (byte)  a;
	     return b;
	 }

	   // float 转换为byte[8]数组
	 public static byte[] getByteArray(float a) {
	     int num = Float.floatToIntBits(a);
	     return NumberUtil.getByteArray(num);
	 }

	   // double 转换为byte[8]数组
	 public static byte[] getByteArray(double a) {
	     long num = Double.doubleToLongBits(a);
	     return NumberUtil.getByteArray(num);
	 }

	   // int转换为byte[8]数组
	 public static byte[] getByteArray(char a) {
	     byte b[] = new byte[2];
	     b[0] = (byte)  (a >> 8);
	     b[1] = (byte)  a;
	     return b;
	 }

	// 将字符数组转换为byte[8]数组
	public static byte[] getByteArray(char[] a) {
		byte b[] = new byte[2 * a.length];
		for(int i = 0;i < a.length;i++){
			int index = i * 2;
			b[index] = (byte)  (0xff & (a[i] >> 8));
			b[index + 1] = (byte)  (0xff & a[i]);
		}

		return b;
	}

	// 转换为byte[8]数组

	/****
	 * 返回一个同时包含整形数和字符数组内容的字节数组
	 * @param b
	 * @param a
	 * @return
	 */
	public static byte[] getByteArray(int b,char[] a) {
		byte[] result = new byte[2 * a.length + 4];

		result[0] = (byte)  (0xff & (b >> 24));
		result[1] = (byte)  (0xff & (b >> 16));
		result[2] = (byte)  (0xff & (b >> 8));
		result[3] = (byte)  (0xff & b);

		for(int i = 0;i < a.length;i++){
			int index = i * 2 + 4;
			result[index] = (byte)  (0xff & (a[i] >> 8));
			result[index + 1] = (byte)  (0xff & a[i]);
		}

		return result;
	}

	/**
	  * 将一个0-255范围内的整数转变为对应的byte数
	  * @param num
	  * @return
	  */
	 public static byte convertIntToByte(int num){
		 return (byte)((num > 127) ? (num - 256) : num);
	 }

	/**
	 * 获得指定的char最少可以用几字节表示
	 * @param value
	 * @return
	 */
	public static int getLength(char value){
		short shortValue = (short)value;
		return getLength(shortValue);
	}

	/**
	 * 获得指定的short型整数最少可以用几字节表示
	 * @param value
	 * @return
	 */
	public static int getLength(short value){
		return ((value >= 0 && value <= Byte.MAX_VALUE) || (value < 0  && value >= Byte.MIN_VALUE)) ? 1 : 2;
	}

	/**
	 * 获得指定的long型整数最少可以用几字节表示
	 * @param value
	 * @return
	 */
	public static int getLength(long value){
		int length = 8;
		long longValue = (Long)value;
		if(longValue >= 0){
			if(longValue >> 7 == 0){
				length = 1;
			}else if(longValue >> 15 == 0){
				length = 2;
			}else if(longValue >> 23 == 0){
				length = 3;
			}else if(longValue >> 31 == 0){
				length = 4;
			}else if(longValue >> 39 == 0){
				length = 5;
			}else if(longValue >> 47 == 0){
				length = 6;
			}else if(longValue >> 55 == 0){
				length = 7;
			}
		}else{
			if(longValue >> 7 == -1){
				length = 1;
			}else if(longValue >> 15 == -1){
				length = 2;
			}else if(longValue >> 23 == -1){
				length = 3;
			}else if(longValue >> 31 == -1){
				length = 4;
			}else if(longValue >> 39 == -1){
				length = 5;
			}else if(longValue >> 47 == -1){
				length = 6;
			}else if(longValue >> 55 == -1){
				length = 7;
			}
		}
		return length;
	}

	/**
	 * 获得指定的整数最少可以用几字节表示
	 * @param value
	 * @return
	 */
	public static int getLength(int value){
		int length = 4;
		int intValue = (Integer)value;
		if(intValue >= 0){
			if(intValue >> 7 == 0){
				length = 1;
			}else if(intValue >> 15 == 0){
				length = 2;
			}else if(intValue >> 23 == 0){
				length = 3;
			}
		}else{
			if(intValue >> 7 == -1){
				length = 1;
			}else if(intValue >> 15 == -1){
				length = 2;
			}else if(intValue >> 23 == -1){
				length = 3;
			}
		}
		return  length;
	}
}
