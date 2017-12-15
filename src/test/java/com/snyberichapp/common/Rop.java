package com.snyberichapp.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// TODO assertAll
// TODO add printAssertion config
public final class Rop {

    private static final DateFormat DF = initDateFormat();
    private static final ObjectMapper OM = initObjectMapper();
    private static final Pattern ARRAY_ELEMENT_PATTERN = Pattern.compile("\\[\\d+\\]");

    private static TestConfiguration testConfiguration;

    private Object values;

    private Rop(Object object) throws IOException {
        if (testConfiguration == null) {
            throw new IllegalStateException("Test configuration is not set!");
        }
        String json = OM.writeValueAsString(object);
        this.values = OM.readValue(json, Object.class);
    }

    public Rop newLine() {
        return this;
    }

    public Rop assertArraySize(int expectedSize) {
        if (values instanceof Collection) {
            String actualSize = String.valueOf(((Collection) values).size());
            ResultComparison resultComparison = new ResultComparison(actualSize, String.valueOf(expectedSize));
            testConfiguration.equalsConsumer().accept(resultComparison);
            return this;
        } else {
            throw new IllegalStateException("Expected array comparison for dataset: " + values);
        }
    }

    public Rop assertArraySize(String arrayKey, int expectedSize) {
        Object result = findValue(arrayKey);
        if (result instanceof Collection) {
            String actualSize = String.valueOf(((Collection) result).size());
            ResultComparison resultComparison = new ResultComparison(actualSize, String.valueOf(expectedSize));
            testConfiguration.equalsConsumer().accept(resultComparison);
        } else {
            throw new IllegalStateException("Expected array comparison for dataset: " + result);
        }
        return this;
    }

    public Rop printAssertions() {
        StringBuilder sb = printAssertions(new StringBuilder(), new StringBuilder(), values);
        System.out.println(sb);
        return this;
    }

    private StringBuilder printAssertions(StringBuilder assertionBuilder, StringBuilder jsonKeyBuilder, Object element) {
        if (element instanceof Map) {
            //noinspection unchecked
            ((Map) element).forEach((k, v) -> {
                if (v instanceof Collection) {
                    StringBuilder keyBuilderCopy = new StringBuilder(jsonKeyBuilder);
                    if (keyBuilderCopy.length() > 0) {
                        keyBuilderCopy.append(".");
                    }
                    keyBuilderCopy.append(k).append(".");
                    assertionBuilder.append(printAssertions(new StringBuilder(), keyBuilderCopy, v));
                }
                if (v instanceof Map) {
                    StringBuilder keyBuilderCopy = new StringBuilder(jsonKeyBuilder);
                    assertionBuilder.append(printAssertions(new StringBuilder(), keyBuilderCopy, v));
                } else {
                    StringBuilder prefix = new StringBuilder(jsonKeyBuilder);
                    final String key = prefix.length() == 0 ? k.toString() : prefix.append(".").append(k.toString()).toString();
                    assertionBuilder.append(determineAssertion(key, v)).append(System.lineSeparator());
                }
            });
        }
        if (element instanceof Collection) {
            int i = 0;
            Collection elements = (Collection) element;
            for (Object object : elements) {
                StringBuilder arrayKeyBuilder = new StringBuilder(jsonKeyBuilder).append("[").append(i).append("]");
                assertionBuilder.append(printAssertions(new StringBuilder(), arrayKeyBuilder, object));
                i++;
            }
        }
        return assertionBuilder;
    }

    private String determineAssertion(String key, Object value) {
        if (value == null) {
            return String.format(".assertNull(\"%s\")", key);
        } else if (value instanceof Collection) {
            Collection elements = (Collection) value;
            return String.format(".assertArraySize(\"%s\", \"%s\")", key, elements.size());
        } else {
            if ("".equals(value.toString())) {
                return String.format(".assertEmpty(\"%s\")", key);
            } else {
                return String.format(".assertEquals(\"%s\", \"%s\")", key, value);
            }
        }
    }

    public Rop assertEquals(String key, String expectedValue) {
        String actualValue = findValueAsString(key);
        ResultComparison resultComparison = new ResultComparison(actualValue, expectedValue);
        testConfiguration.equalsConsumer().accept(resultComparison);
        return this;
    }

    public Rop assertStartsWith(String key, String startsWith) {
        String actualValue = findValueAsString(key);
        ResultComparison resultComparison = new ResultComparison(actualValue, startsWith);
        testConfiguration.startsWithConsumer().accept(resultComparison);
        return this;
    }

    public Rop assertContains(String key, String content) {
        String actualValue = findValueAsString(key);
        ResultComparison resultComparison = new ResultComparison(actualValue, content);
        testConfiguration.containsConsumer().accept(resultComparison);
        return this;
    }

    public Rop assertEmpty(String key) {
        String actualValue = findValueAsString(key);
        testConfiguration.emptyConsumer().accept(actualValue);
        return this;
    }

    public Rop assertNull(String key) {
        String actualValue = findValueAsString(key);
        testConfiguration.nullConsumer().accept(actualValue);
        return this;
    }

    public Rop assertNotNull(String key) {
        String actualValue = findValueAsString(key);
        testConfiguration.notNullConsumer().accept(actualValue);
        return this;
    }

    @SuppressWarnings("unchecked")
    private Object findValue(String key) {
        LinkedList<String> tokens = new LinkedList<>(Arrays.asList(key.split("\\.")));

        String firstToken = tokens.removeFirst();
        Object element = findNextElement(values, firstToken);

        for (String token : tokens) {
            element = findNextElement(element, token);
        }

        return element;
    }

    private String findValueAsString(String key) {
        Object element = findValue(key);
        return element != null ? element.toString() : null;
    }

    @SuppressWarnings("unchecked")
    private Object findNextElement(Object element, String token) {
        Matcher matcher = ARRAY_ELEMENT_PATTERN.matcher(token);
        StringBuilder sb = new StringBuilder();

        while (matcher.find()) {
            String arrayPosition = matcher.group();
            sb.append(arrayPosition);

            if (token.startsWith(sb.toString())) {
                element = ((List<Map<String, Object>>) element).get(getArrayElement(arrayPosition));
            } else {
                String tokenWithoutArrayPosition = token.substring(0, token.length() - arrayPosition.length());
                List<Map<String, Object>> array = (List<Map<String, Object>>) ((Map<String, Object>) element).get(tokenWithoutArrayPosition);
                element = array.get(getArrayElement(arrayPosition));
            }
        }

        if (sb.length() == 0) {
            element = ((Map<String, Object>) element).get(token);
        }

        return element;
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
