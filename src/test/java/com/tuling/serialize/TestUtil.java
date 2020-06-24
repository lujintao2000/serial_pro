package com.tuling.serialize;

import org.junit.Assert;

import java.lang.reflect.Array;
import java.util.Collection;

/**
 * Created by Administrator on 2020-06-24.
 */
public class TestUtil {

    /**
     * 判断两个引用变量是否相等
     * @param originalValue
     * @param obj
     * @return
     */
    public static boolean isEqual(Object originalValue,Object obj){
        boolean flag = false;
        if(originalValue != null){
            if(originalValue.getClass().isArray()){
                 flag = true;
                int length = Array.getLength(originalValue);
                if(obj != null && obj.getClass().isArray() && length == Array.getLength(obj)){
                    for(int i = 0; i < length; i++){
                        if(!Array.get(originalValue,i).equals(Array.get(obj,i))){
                            flag = false;
                            break;
                        }
                    }
                }else{
                    flag = false;
                }


            }else if(originalValue instanceof Collection){
                flag = false;
                if(obj != null && obj instanceof  Collection && ((Collection)originalValue).size() == ((Collection)obj).size()){
                    flag = ((Collection) obj).stream().noneMatch(x -> !((Collection)originalValue).contains(x));
                }
            }else{
                flag = originalValue.equals(obj);
            }
        }else{
            flag = obj == null;
        }
        return flag;
    }

}
