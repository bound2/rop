package com.snyberichapp.tools.rop;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.snyberichapp.tools.rop.configuration.TestConfiguration;
import com.snyberichapp.tools.rop.exception.AssertAllException;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class Rop {

    private static final ObjectMapper OM = initObjectMapper();
    private static final Pattern ARRAY_ELEMENT_PATTERN = Pattern.compile("\\[\\d+\\]");
    private static final String EMPTY_JSON = "{}";
    private static final Set<Rop> PENDING_ASSERTIONS = Collections.synchronizedSet(new HashSet<>());

    private static TestConfiguration testConfiguration;
    private static Consumer<String> assertionPrinter;

    private final String identifier;
    private Object values;
    private boolean assertAll;
    private List<Throwable> failedAssertions = Collections.synchronizedList(new LinkedList<>());

    private Rop(Object object) throws IOException {
        Objects.requireNonNull(object, "Can't form Rop from null element!");
        Objects.requireNonNull(testConfiguration, "Rop test configuration is not set!");
        Objects.requireNonNull(assertionPrinter, "Rop assertion printer is not set!");

        final String json;
        if (object instanceof String && ((String) object).trim().length() > 0) {
            json = object.toString();
        } else {
            json = OM.writeValueAsString(object);
        }
        this.values = OM.readValue(json, Object.class);
        this.identifier = UUID.randomUUID().toString();
    }

    public Rop enableAssertAll() {
        this.assertAll = true;
        PENDING_ASSERTIONS.add(this);
        return this;
    }

    public void assertAll() {
        try {
            for (Throwable t : failedAssertions) {
                t.printStackTrace();
            }
            if (failedAssertions.size() > 0) {
                throw new AssertAllException();
            }
        } finally {
            PENDING_ASSERTIONS.remove(this);
        }
    }

    public Rop separator() {
        return this;
    }

    public Rop printAssertions() {
        if (values instanceof String && ((String) values).trim().length() == 0) {
            assertionPrinter.accept(".assertEmpty()");
        } else if (EMPTY_JSON.equals(values.toString())) {
            assertionPrinter.accept(".assertEmptyJson()");
        } else {
            StringBuilder sb = buildAssertions(new StringBuilder(), new StringBuilder(), values);
            assertionPrinter.accept(sb.toString());
        }
        return this;
    }

    private StringBuilder buildAssertions(StringBuilder assertionBuilder, StringBuilder jsonKeyBuilder, Object element) {
        if (element instanceof Map) {
            //noinspection unchecked
            ((Map) element).forEach((k, v) -> {
                if (v instanceof Collection) {
                    StringBuilder keyBuilderCopy = new StringBuilder(jsonKeyBuilder);
                    if (keyBuilderCopy.length() > 0) {
                        keyBuilderCopy.append(".");
                    }
                    keyBuilderCopy.append(k);
                    assertionBuilder.append(buildAssertions(new StringBuilder(), keyBuilderCopy, v));
                }
                if (v instanceof Map) {
                    StringBuilder keyBuilderCopy = new StringBuilder(jsonKeyBuilder);
                    if (keyBuilderCopy.length() > 0) {
                        keyBuilderCopy.append(".");
                    }
                    keyBuilderCopy.append(k);
                    assertionBuilder.append(buildAssertions(new StringBuilder(), keyBuilderCopy, v));
                } else {
                    StringBuilder prefix = new StringBuilder(jsonKeyBuilder);
                    final String key = prefix.length() == 0 ? k.toString() : prefix.append(".").append(k.toString()).toString();
                    assertionBuilder.append(determineAssertion(key, v)).append(System.lineSeparator());
                }
            });
        } else if (element instanceof Collection) {
            int i = 0;
            Collection elements = (Collection) element;
            for (Object object : elements) {
                StringBuilder arrayKeyBuilder = new StringBuilder(jsonKeyBuilder).append("[").append(i).append("]");
                assertionBuilder.append(buildAssertions(new StringBuilder(), arrayKeyBuilder, object));
                i++;
            }
        } else {
            String key = jsonKeyBuilder.toString();
            assertionBuilder.append(determineAssertion(key, findValue(key))).append(System.lineSeparator());
        }
        return assertionBuilder;
    }

    private String determineAssertion(String key, Object value) {
        if (value == null) {
            return String.format(".assertNull(\"%s\")", key);
        } else if (value instanceof Collection) {
            Collection elements = (Collection) value;
            return String.format(".assertArraySize(\"%s\", %s)", key, elements.size());
        } else {
            if ("".equals(value.toString())) {
                return String.format(".assertEmpty(\"%s\")", key);
            } else {
                return String.format(".assertEquals(\"%s\", \"%s\")", key, value);
            }
        }
    }

    public Rop assertArraySize(int expectedSize) {
        return assertArraySize(values, expectedSize);
    }

    public Rop assertArraySize(String arrayKey, int expectedSize) {
        Object arrayObject = findValue(arrayKey);
        return assertArraySize(arrayObject, expectedSize);
    }

    private Rop assertArraySize(Object arrayObject, int expectedSize) {
        try {
            if (arrayObject instanceof Collection) {
                String actualSize = String.valueOf(((Collection) arrayObject).size());
                ResultComparison resultComparison = new ResultComparison(actualSize, String.valueOf(expectedSize));
                testConfiguration.equalsConsumer().accept(resultComparison);
            } else {
                throw new IllegalStateException("Expected array comparison for dataset: " + arrayObject);
            }
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertEquals(String key, String expectedValue) {
        try {
            String actualValue = findValueAsString(key);
            ResultComparison resultComparison = new ResultComparison(actualValue, expectedValue);
            testConfiguration.equalsConsumer().accept(resultComparison);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertStartsWith(String key, String startsWith) {
        try {
            String actualValue = findValueAsString(key);
            ResultComparison resultComparison = new ResultComparison(actualValue, startsWith);
            testConfiguration.startsWithConsumer().accept(resultComparison);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertContains(String key, String content) {
        try {
            String actualValue = findValueAsString(key);
            ResultComparison resultComparison = new ResultComparison(actualValue, content);
            testConfiguration.containsConsumer().accept(resultComparison);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertEmptyJson() {
        try {
            ResultComparison resultComparison = new ResultComparison(values.toString(), EMPTY_JSON);
            testConfiguration.equalsConsumer().accept(resultComparison);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertEmpty() {
        try {
            testConfiguration.emptyConsumer().accept(values.toString());
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertEmpty(String key) {
        try {
            String actualValue = findValueAsString(key);
            testConfiguration.emptyConsumer().accept(actualValue);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertNull(String key) {
        try {
            String actualValue = findValueAsString(key);
            testConfiguration.nullConsumer().accept(actualValue);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    public Rop assertNotNull(String key) {
        try {
            String actualValue = findValueAsString(key);
            testConfiguration.notNullConsumer().accept(actualValue);
        } catch (Throwable t) {
            if (assertAll) {
                failedAssertions.add(t);
            } else {
                throw t;
            }
        }
        return this;
    }

    @SuppressWarnings({"unchecked", "WeakerAccess"})
    public Object findValue(String key) {
        LinkedList<String> tokens = new LinkedList<>(Arrays.asList(key.split("\\.")));

        String firstToken = tokens.removeFirst();
        Object element = findNextElement(values, firstToken);

        for (String token : tokens) {
            element = findNextElement(element, token);
        }

        return element;
    }

    @SuppressWarnings("WeakerAccess")
    public String findValueAsString(String key) {
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

    @Override
    public int hashCode() {
        return Objects.hashCode(this.identifier);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Rop that = (Rop) o;
        return Objects.equals(this.identifier, that.identifier);
    }

    // ========================= HELPER METHODS ========================= //
    public static Rop of(Object object) throws IOException {
        return new Rop(object);
    }

    public static void setConfiguration(TestConfiguration testConfiguration, Consumer<String> assertionPrinter) {
        Rop.testConfiguration = testConfiguration;
        Rop.assertionPrinter = assertionPrinter;
    }

    public static void validateConsistency() {
        if (PENDING_ASSERTIONS.size() > 0) {
            throw new RuntimeException("Found pending assertions!");
        }
    }

    private static ObjectMapper initObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        objectMapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        objectMapper.setDateFormat(getUTCDateFormat());
        objectMapper.registerModule(new Jdk8Module());
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    private static DateFormat getUTCDateFormat() {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(TimeZone.getTimeZone("UTC"));
        return df;
    }

}