package com.snyberichapp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

public final class Rop {

    private static final ObjectMapper OM = init();
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private static TestConfiguration testConfiguration;

    private final Map<String, Object> values;

    private Rop(Object object) throws IOException {
        String json = OM.writeValueAsString(object);
        this.values = OM.readValue(json, TYPE_REFERENCE);
    }

    public Rop assertEquals(String key, String expectedValue) {
        // TODO handle map etc from actual value
        Object actualValue = values.get(key);
        ResultComparison resultComparison = new ResultComparison((String) actualValue, expectedValue);
        testConfiguration.equalsConsumer().accept(resultComparison);
        return this;
    }

    // ========================= HELPER METHODS ========================= //
    public static Rop of(Object object) throws IOException {
        return new Rop(object);
    }

    public static void setConfiguration(TestConfiguration testConfiguration) {
        Rop.testConfiguration = testConfiguration;
    }

    private static ObjectMapper init() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        return objectMapper;
    }

}
