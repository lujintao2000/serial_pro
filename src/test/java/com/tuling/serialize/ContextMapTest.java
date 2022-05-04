package com.tuling.serialize;

import com.tuling.domain.Role;
import com.tuling.serialize.util.ContextMap;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2021-02-15.
 */
public class ContextMapTest {

    @Test
    public void testContains(){
        ContextMap map = new ContextMap();
        Role role = Role.getInstance();
        role.setAge(20);
        map.add(role);
        Role role2 = Role.getInstance();
        role2.setAge(20);
//        map.add(role2);
        Assert.assertTrue(!map.contains(role2));


    }

    @Test
    public void testAdd(){
        ContextMap contextMap = new ContextMap();
        for(int i = 0; i < 5000;i++){
            Role role = Role.getInstance();
            role.setAge(i);
            contextMap.add(role);
        }
        Assert.assertTrue(true);
    }

}
