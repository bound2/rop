package com.snyberichapp.common;

import org.junit.Assert;

import java.util.function.Consumer;

public class JunitConfiguration implements TestConfiguration {

    @Override
    public Consumer<ResultComparison> equalsConsumer() {
        return result -> Assert.assertEquals(result.getExpected(), result.getActual());
    }

    @Override
    public Consumer<ResultComparison> startsWithConsumer() {
        return result -> {
            String errorMessage = String.format("Expected %s to start with %s", result.getExpected(), result.getActual());
            Assert.assertTrue(errorMessage, result.getExpected().startsWith(result.getActual()));
        };
    }

    @Override
    public Consumer<ResultComparison> containsConsumer() {
        return result -> {
            String errorMessage = String.format("%s didn't contain %s", result.getActual(), result.getExpected());
            Assert.assertTrue(errorMessage, result.getExpected().contains(result.getActual()));
        };
    }

    @Override
    public Consumer<Object> notNullConsumer() {
        return Assert::assertNotNull;
    }

}
