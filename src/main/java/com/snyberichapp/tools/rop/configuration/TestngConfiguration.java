package com.snyberichapp.tools.rop.configuration;

import com.snyberichapp.tools.rop.ResultComparison;
import org.testng.Assert;

import java.util.function.Consumer;

public class TestngConfiguration implements TestConfiguration {

    @Override
    public Consumer<ResultComparison> equalsConsumer() {
        return result -> Assert.assertEquals(result.getActual(), result.getExpected());
    }

    @Override
    public Consumer<ResultComparison> startsWithConsumer() {
        return result -> {
            String errorMessage = String.format("Expected %s to start with %s", result.getExpected(), result.getActual());
            Assert.assertTrue(result.getActual().startsWith(result.getExpected()), errorMessage);
        };
    }

    @Override
    public Consumer<ResultComparison> containsConsumer() {
        return result -> {
            String errorMessage = String.format("%s didn't contain %s", result.getActual(), result.getExpected());
            Assert.assertTrue(result.getActual().contains(result.getExpected()), errorMessage);
        };
    }

    @Override
    public Consumer<Object> nullConsumer() {
        return Assert::assertNull;
    }

    @Override
    public Consumer<Object> notNullConsumer() {
        return Assert::assertNotNull;
    }

    @Override
    public Consumer<String> emptyConsumer() {
        return item -> Assert.assertEquals(item, "", "Expected element to be empty, but was: " + item);
    }

}
