package com.tuling.serialize;

import org.junit.Assert;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

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
                        if(Array.get(originalValue,i) != null){
                            if(Array.get(originalValue,i).getClass().isArray()){
                                if(!isEqual(Array.get(originalValue,i), Array.get(obj,i))){
                                    flag = false;
                                    break;
                                }
                            }else{
                                if(!Array.get(originalValue,i).equals(Array.get(obj,i))){
                                    flag = false;
                                    break;
                                }
                            }
                        }else if(Array.get(obj,i) != null){
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
            }else if(originalValue instanceof AtomicInteger || originalValue instanceof AtomicLong || originalValue instanceof AtomicBoolean){
                if(originalValue instanceof  AtomicInteger){
                    if(obj != null){
                        flag = ((AtomicInteger)originalValue).get() == ((AtomicInteger)obj).get();
                    }
                }else if(originalValue instanceof AtomicLong){
                    if(obj != null){
                        flag = ((AtomicLong)originalValue).get() == ((AtomicLong)obj).get();
                    }
                }else{
                    if(obj != null){
                        flag = ((AtomicBoolean)originalValue).get() == ((AtomicBoolean)obj).get();
                    }
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
