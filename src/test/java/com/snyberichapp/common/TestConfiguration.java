package com.snyberichapp.common;

import java.util.function.Consumer;

public interface TestConfiguration {

    Consumer<ResultComparison> equalsConsumer();

    Consumer<ResultComparison> startsWithConsumer();

    Consumer<ResultComparison> containsConsumer();

    Consumer<Object> nullConsumer();

    Consumer<Object> notNullConsumer();

    Consumer<String> emptyConsumer();

}
