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

    public Rop assertStartsWith(String key, String startsWith) {
        String actualValue = findValue(key);
        ResultComparison resultComparison = new ResultComparison(actualValue, startsWith);
        testConfiguration.startsWithConsumer().accept(resultComparison);
        return this;
    }

    public Rop assertContains(String key, String content) {
        String actualValue = findValue(key);
        ResultComparison resultComparison = new ResultComparison(actualValue, content);
        testConfiguration.containsConsumer().accept(resultComparison);
        return this;
    }

    public Rop assertEmpty(String key) {
        String actualValue = findValue(key);
        testConfiguration.emptyConsumer().accept(actualValue);
        return this;
    }

    public Rop assertNull(String key) {
        String actualValue = findValue(key);
        testConfiguration.nullConsumer().accept(actualValue);
        return this;
    }

    public Rop assertNotNull(String key) {
        String actualValue = findValue(key);
        testConfiguration.notNullConsumer().accept(actualValue);
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
                String arrayPosition = firstMatcher.group();
                if (firstToken.startsWith(arrayPosition)) {
                    element = arrayValues.get(getArrayElement(arrayPosition));
                } else {
                    // token must end with array position if it wasn't present at start
                    String tokenWithoutArrayPosition = firstToken.substring(0, firstToken.length() - arrayPosition.length());
                    List<Map<String, Object>> array = (List<Map<String, Object>>) values.get(tokenWithoutArrayPosition);
                    element = array.get(getArrayElement(arrayPosition));
                }
            } else {
                element = (Map<String, Object>) values.get(firstToken);
            }

            String lastToken = tokens.removeLast();
            for (String token : tokens) {
                Matcher matcher = ARRAY_ELEMENT_PATTERN.matcher(token);
                if (matcher.find()) {
                    String arrayPosition = matcher.group();
                    String tokenWithoutArrayPosition = token.substring(0, token.length() - arrayPosition.length());
                    List<Map<String, Object>> array = (List<Map<String, Object>>) element.get(tokenWithoutArrayPosition);
                    element = array.get(getArrayElement(arrayPosition));
                } else {
                    element = (Map<String, Object>) element.get(token);
                }
            }
            // Final value should come from the last token
            value = element.get(lastToken).toString();
        }
        return value;
    }

    private Integer getArrayElement(String arrayPosition) {
        return Integer.valueOf(arrayPosition
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
