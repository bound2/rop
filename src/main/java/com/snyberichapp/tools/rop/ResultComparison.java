package com.snyberichapp.tools.rop;

public class ResultComparison {

    private final String actual;
    private final String expected;

    public ResultComparison(String actual, String expected) {
        this.actual = actual;
        this.expected = expected;
    }

    public String getActual() {
        return actual;
    }

    public String getExpected() {
        return expected;
    }

}
