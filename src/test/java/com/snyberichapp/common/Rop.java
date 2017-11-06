package com.snyberichapp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.TimeZone;

public final class Rop {

    private static final DateFormat DF = initDateFormat();
    private static final ObjectMapper OM = initObjectMapper();
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
        final String actualValue;
        final Object value = values.get(key);
        actualValue = value.toString();

        ResultComparison resultComparison = new ResultComparison(actualValue, expectedValue);
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

    private static ObjectMapper initObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(DF);
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private static DateFormat initDateFormat() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

}
