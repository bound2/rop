package com.snyberichapp.tests;

import com.snyberichapp.common.JunitConfiguration;
import com.snyberichapp.common.Rop;
import org.junit.BeforeClass;
import org.junit.Test;

public class RopJunitTest extends RopTest {

    @BeforeClass
    public static void beforeClass() {
        Rop.setConfiguration(new JunitConfiguration());
    }

    @Test
    @Override
    public void regularObjectTest() throws Exception {
        super.regularObjectTest();
    }

    @Test
    @Override
    public void arrayObjectTest() throws Exception {
        super.arrayObjectTest();
    }

    @Test
    @Override
    public void nestedObjectTest() throws Exception {
        super.nestedObjectTest();
    }

    @Test
    @Override
    public void nestedArrayObjectTest() throws Exception {
        super.nestedArrayObjectTest();
    }

    @Test
    @Override
    public void startsWithTest() throws Exception {
        super.startsWithTest();
    }

    @Test
    @Override
    public void containsTest() throws Exception {
        super.containsTest();
    }

    @Test
    @Override
    public void emptyTest() throws Exception {
        super.emptyTest();
    }

    @Test
    @Override
    public void nullTest() throws Exception {
        super.nullTest();
    }

    @Test
    @Override
    public void notNullTest() throws Exception {
        super.notNullTest();
    }
}
