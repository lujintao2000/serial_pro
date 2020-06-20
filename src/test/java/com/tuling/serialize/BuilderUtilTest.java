package com.tuling.serialize;

import com.tuling.serialize.util.BuilderUtil;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by Administrator on 2020-06-17.
 */
public class BuilderUtilTest {

    @Test
    public void testGetDefaultPackageName(){
        String packageName = BuilderUtil.getDefaultPackageName();
        Assert.assertEquals(packageName, "com.tuling.builder");


    }
}
