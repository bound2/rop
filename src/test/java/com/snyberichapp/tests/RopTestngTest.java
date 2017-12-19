package com.snyberichapp.tests;

import com.snyberichapp.common.Rop;
import com.snyberichapp.common.TestngConfiguration;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class RopTestngTest extends RopTest {

    @BeforeClass
    public void beforeClass() {
        Rop.setConfiguration(new TestngConfiguration(), System.out::println);
    }

    @Test
    @Override
    public void regularObjectTest() throws Exception {
        super.regularObjectTest();
    }

    @Test
    @Override
    public void jsonStringObjectTest() throws Exception {
        super.jsonStringObjectTest();
    }

    @Test
    @Override
    public void emptyJsonStringObjectTest() throws Exception {
        super.emptyJsonStringObjectTest();
    }

    @Test
    @Override
    public void stringEmptyObjectTest() throws Exception {
        super.stringEmptyObjectTest();
    }

    @Test(expectedExceptions = NullPointerException.class)
    @Override
    public void nullObjectTest() throws Exception {
        super.nullObjectTest();
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
    public void twoDimensionalArrayTest() throws Exception {
        super.twoDimensionalArrayTest();
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

    @Test
    @Override
    public void printAssertionsTest() throws Exception {
        super.printAssertionsTest();
    }
}
