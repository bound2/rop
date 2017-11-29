package com.snyberichapp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.exc.MismatchedInputException;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Rop {

    private static final DateFormat DF = initDateFormat();
    private static final ObjectMapper OM = initObjectMapper();
    private static final TypeReference<Map<String, Object>> TYPE_REFERENCE = new TypeReference<Map<String, Object>>() {
    };
    private static final TypeReference<List<Map<String, Object>>> LIST_TYPE_REFERENCE = new TypeReference<List<Map<String, Object>>>() {
    };
    private static final Pattern ARRAY_ELEMENT_PATTERN = Pattern.compile("\\[\\d+\\]");
    private static TestConfiguration testConfiguration;

    private Map<String, Object> values;
    private List<Map<String, Object>> arrayValues;

    private Rop(Object object) throws IOException {
        String json = OM.writeValueAsString(object);
        try {
            this.values = OM.readValue(json, TYPE_REFERENCE);
        } catch (MismatchedInputException e) {
            this.arrayValues = OM.readValue(json, LIST_TYPE_REFERENCE);
        }
    }

    public Rop newLine() {
        return this;
    }

    public Rop assertArraySize(int expectedSize) {
        ResultComparison resultComparison = new ResultComparison(String.valueOf(arrayValues.size()), String.valueOf(expectedSize));
        testConfiguration.equalsConsumer().accept(resultComparison);
        return this;
    }

    public Rop assertEquals(String key, String expectedValue) {
        String actualValue = findValue(key);
        ResultComparison resultComparison = new ResultComparison(actualValue, expectedValue);
        testConfiguration.equalsConsumer().accept(resultComparison);
        return this;
    }

    private String findValue(String key) {
        LinkedList<String> tokens = new LinkedList<>(Arrays.asList(key.split("\\.")));
        final String value;
        if (tokens.size() <= 1) {
            value = values.get(key).toString();
        } else {
            // something.different.arrays[0].value
            // [0].something.different.arrays[0].value
            String firstToken = tokens.removeFirst();
            Matcher firstMatcher = ARRAY_ELEMENT_PATTERN.matcher(firstToken);
            Map<String, Object> element;
            if (firstMatcher.find()) {
                element = arrayValues.get(getArrayElement(firstMatcher));
            } else {
                element = (Map<String, Object>) values.get(firstToken);
            }

            String lastToken = tokens.removeLast();
            for (String token : tokens) {
                Matcher matcher = ARRAY_ELEMENT_PATTERN.matcher(token);
                if (matcher.find()) {
                    List<Map<String, Object>> array = (List<Map<String, Object>>) element.get(token);
                    element = array.get(getArrayElement(matcher));
                } else {
                    element = (Map<String, Object>) element.get(token);
                }
            }
            value = element.get(lastToken).toString();
        }
        return value;
    }

    private Integer getArrayElement(Matcher matcher) {
        return Integer.valueOf(matcher.group()
                .replace("[", "")
                .replace("]", ""));
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
