package com.snyberichapp.tests.pojo;

import java.time.Instant;
import java.util.List;

public class NestedTestObject {

    private String firstName;
    private String lastName;
    private License license;
    private List<Car> cars;

    public static class Car {
        private String make;
        private String model;
        private List<String> notes;

        public String getMake() {
            return make;
        }

        public void setMake(String make) {
            this.make = make;
        }

        public String getModel() {
            return model;
        }

        public void setModel(String model) {
            this.model = model;
        }

        public List<String> getNotes() {
            return notes;
        }

        public void setNotes(List<String> notes) {
            this.notes = notes;
        }
    }

    public static class License {
        private String category;
        private Instant expires;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public Instant getExpires() {
            return expires;
        }

        public void setExpires(Instant expires) {
            this.expires = expires;
        }
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public List<Car> getCars() {
        return cars;
    }

    public void setCars(List<Car> cars) {
        this.cars = cars;
    }
}
