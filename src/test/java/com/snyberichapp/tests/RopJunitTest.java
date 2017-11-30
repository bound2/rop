package com.snyberichapp.tests;

import com.snyberichapp.common.JunitConfiguration;
import com.snyberichapp.common.Rop;
import org.testng.annotations.BeforeClass;

public class RopJunitTest extends RopTest {

    @BeforeClass
    public void beforeClass() {
        Rop.setConfiguration(new JunitConfiguration());
    }

}
