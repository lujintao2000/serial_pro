package com.tuling.serialize;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 该枚举定义了8种基本数据类型及其代表字符
 * @author lujintao
 * @date 2020-06-10
 */
public enum BaseTypeEnum {
    BYTE(Byte.class,"!"),
    CHARACTER(Character.class,"@"),
    BOOLEAN(Boolean.class,"#"),
    SHORT(Short.class,"$"),
    INT(Integer.class,"&"),
    LONG(Long.class,"*"),
    FLOAT(Float.class,"("),
    DOUBLE(Double.class,")"),
    STRING(String.class,"?"),
    VOID(Void.class,"-");


    private static Map<Class,BaseTypeEnum> map = new HashMap<>();
    private Class type;
    private String value;

    static{
        Arrays.stream(BaseTypeEnum.values()).forEach(x -> map.put(x.getType(), x));
    }

    private BaseTypeEnum(Class type,String value) {
        this.type = type;
        this.value = value;
    }

    /**
     * 获得指定类型对应的BaseTypeEnum
     * @param type
     * @return
     */
    public static BaseTypeEnum get(Class type){
        return map.get(type);
    }

    public String getValue() {
        return value;
    }

    public Class getType() {
        return type;
    }
}
