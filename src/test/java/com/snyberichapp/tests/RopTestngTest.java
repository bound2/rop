package com.snyberichapp.tests;

import com.snyberichapp.common.Rop;
import com.snyberichapp.common.TestngConfiguration;
import org.testng.annotations.BeforeClass;

public class RopTestngTest extends RopTest {

    @BeforeClass
    public void beforeClass() {
        Rop.setConfiguration(new TestngConfiguration());
    }

}
