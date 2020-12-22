package com.tuling.serialize;

import com.tuling.domain.Nation;
import com.tuling.domain.Role;
import com.tuling.domain.User;
import com.tuling.serialize.util.ReflectUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.print.attribute.standard.MediaSize;

/**
 * Created by Administrator on 2020-11-16.
 */
public class ReflectUtilTest {


    @Test
    public void testGetEnum(){

        Assert.assertEquals(ReflectUtil.getEnum(Nation.class,"MIAOZU"),Nation.MIAOZU);
        Assert.assertEquals(ReflectUtil.getEnum(Nation.class,"MENGGUZU"),Nation.MENGGUZU);
        Assert.assertEquals(ReflectUtil.getEnum(Nation.class,"HANZU"),Nation.HANZU);
    }

    @Test
    public void testGetIdForClass(){
        byte  a = (byte)128;
        Assert.assertTrue(new Integer(1).equals(ReflectUtil.getIdForClass(Role.class)));
        Assert.assertTrue(new Integer(1).equals(ReflectUtil.getIdForClass(Role.class)));
        Assert.assertTrue(new Integer(2).equals(ReflectUtil.getIdForClass(User.class)));
        Assert.assertTrue(new Integer(3).equals(ReflectUtil.getIdForClass(Nation.class)));
    }

}
