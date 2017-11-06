package com.snyberichapp.tests.pojo;

import java.time.Instant;
import java.util.Calendar;

public class RegularTestObject {

    private final String name;
    private final Integer kidCount;
    private final Boolean married;
    private final Calendar born;
    private final Instant died;

    public RegularTestObject(String name, Integer kidCount, Boolean married, Calendar born, Instant died) {
        this.name = name;
        this.kidCount = kidCount;
        this.married = married;
        this.born = born;
        this.died = died;
    }

    public String getName() {
        return name;
    }

    public Integer getKidCount() {
        return kidCount;
    }

    public Boolean getMarried() {
        return married;
    }

    public Calendar getBorn() {
        return born;
    }

    public Instant getDied() {
        return died;
    }
}
