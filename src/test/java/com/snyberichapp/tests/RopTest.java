package com.snyberichapp.tests;

import com.snyberichapp.common.Rop;
import com.snyberichapp.tests.pojo.NestedTestObject;
import com.snyberichapp.tests.pojo.RegularTestObject;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static java.util.Collections.singletonMap;

public abstract class RopTest {

    public void regularObjectTest() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Instant instant = Instant.now();
        RegularTestObject testObject = new RegularTestObject("sven", 7, Boolean.TRUE, calendar, instant);
        Rop.of(testObject)
                .assertEquals("name", "sven")
                .assertEquals("kidCount", "7")
                .assertEquals("married", "true")
                .assertEquals("born", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("died", DateTimeFormatter.ISO_INSTANT.format(instant));
    }

    public void arrayObjectTest() throws Exception {
        Calendar calendar = Calendar.getInstance();
        Instant instant = Instant.now();

        List<RegularTestObject> objects = new ArrayList<>();
        {
            RegularTestObject testObject = new RegularTestObject("sven_array", 14, Boolean.TRUE, calendar, instant);
            objects.add(testObject);
        }
        {
            RegularTestObject testObject = new RegularTestObject("frodo_array", 2, Boolean.FALSE, calendar, instant);
            objects.add(testObject);
        }
        {
            RegularTestObject testObject = new RegularTestObject("jansen_array", 8, Boolean.FALSE, calendar, instant);
            objects.add(testObject);
        }

        Rop.of(objects).assertArraySize(3)
                .assertEquals("[0].name", "sven_array")
                .assertEquals("[0].kidCount", "14")
                .assertEquals("[0].married", "true")
                .assertEquals("[0].born", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("[0].died", DateTimeFormatter.ISO_INSTANT.format(instant))
                .newLine()
                .assertEquals("[1].name", "frodo_array")
                .assertEquals("[1].kidCount", "2")
                .assertEquals("[1].married", "false")
                .assertEquals("[1].born", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("[1].died", DateTimeFormatter.ISO_INSTANT.format(instant))
                .newLine()
                .assertEquals("[2].name", "jansen_array")
                .assertEquals("[2].kidCount", "8")
                .assertEquals("[2].married", "false")
                .assertEquals("[2].born", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("[2].died", DateTimeFormatter.ISO_INSTANT.format(instant));
    }

    public void nestedObjectTest() throws Exception {
        Instant instant = Instant.now();

        NestedTestObject nestedTestObject = new NestedTestObject();
        nestedTestObject.setFirstName("Johan");
        nestedTestObject.setLastName("Blem");

        NestedTestObject.License license = new NestedTestObject.License();
        license.setCategory("A");
        license.setExpires(instant);
        nestedTestObject.setLicense(license);

        List<NestedTestObject.Car> cars = new ArrayList<>();
        {
            NestedTestObject.Car car = new NestedTestObject.Car();
            car.setMake("AUDI");
            car.setModel("A6");
            cars.add(car);
        }
        {
            NestedTestObject.Car car = new NestedTestObject.Car();
            car.setMake("BMW");
            car.setModel("535i");
            cars.add(car);
        }
        nestedTestObject.setCars(cars);

        Rop.of(nestedTestObject)
                .assertEquals("firstName", "Johan")
                .assertEquals("lastName", "Blem")
                .assertEquals("license.category", "A")
                .assertEquals("license.expires", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("cars[0].make", "AUDI")
                .assertEquals("cars[0].model", "A6")
                .assertEquals("cars[1].make", "BMW")
                .assertEquals("cars[1].model", "535i");
    }

    public void nestedArrayObjectTest() throws Exception {
        Instant instant = Instant.now();

        List<NestedTestObject> objects = new ArrayList<>();
        {
            NestedTestObject nestedTestObject = new NestedTestObject();
            nestedTestObject.setFirstName("Johannes");
            nestedTestObject.setLastName("Strauss");

            NestedTestObject.License license = new NestedTestObject.License();
            license.setCategory("B");
            license.setExpires(instant);
            nestedTestObject.setLicense(license);

            List<NestedTestObject.Car> cars = new ArrayList<>();
            {
                NestedTestObject.Car car = new NestedTestObject.Car();
                car.setMake("TOYOTA");
                car.setModel("PRIUS");
                cars.add(car);
            }
            {
                NestedTestObject.Car car = new NestedTestObject.Car();
                car.setMake("SUBARU");
                car.setModel("BRZ");
                cars.add(car);
            }
            nestedTestObject.setCars(cars);
            objects.add(nestedTestObject);
        }

        {
            NestedTestObject nestedTestObject = new NestedTestObject();
            nestedTestObject.setFirstName("Young");
            nestedTestObject.setLastName("Boi");

            NestedTestObject.License license = new NestedTestObject.License();
            license.setCategory("C");
            license.setExpires(instant);
            nestedTestObject.setLicense(license);

            List<NestedTestObject.Car> cars = new ArrayList<>();
            {
                NestedTestObject.Car car = new NestedTestObject.Car();
                car.setMake("MITSUBISHI");
                car.setModel("LANCER");
                cars.add(car);
            }
            nestedTestObject.setCars(cars);
            objects.add(nestedTestObject);
        }

        Rop.of(objects)
                .assertEquals("[0].firstName", "Johannes")
                .assertEquals("[0].lastName", "Strauss")
                .assertEquals("[0].license.category", "B")
                .assertEquals("[0].license.expires", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("[0].cars[0].make", "TOYOTA")
                .assertEquals("[0].cars[0].model", "PRIUS")
                .assertEquals("[0].cars[1].make", "SUBARU")
                .assertEquals("[0].cars[1].model", "BRZ")
                .newLine()
                .assertEquals("[1].firstName", "Young")
                .assertEquals("[1].lastName", "Boi")
                .assertEquals("[1].license.category", "C")
                .assertEquals("[1].license.expires", DateTimeFormatter.ISO_INSTANT.format(instant))
                .assertEquals("[1].cars[0].make", "MITSUBISHI")
                .assertEquals("[1].cars[0].model", "LANCER");
    }

    public void twoDimensionalArrayTest() throws Exception {
        {
            List<String[]> items = new ArrayList<>();
            items.add(new String[]{"Veni", "Vidi", "Vici"});
            items.add(new String[]{"Trio", "Leo"});
            items.add(new String[]{"Neo"});

            Rop.of(items)
                    .assertEquals("[0][0]", "Veni")
                    .assertEquals("[0][1]", "Vidi")
                    .assertEquals("[0][2]", "Vici")
                    .newLine()
                    .assertEquals("[1][0]", "Trio")
                    .assertEquals("[1][1]", "Leo")
                    .newLine()
                    .assertEquals("[2][0]", "Neo");
        }

        {
            List<Map[]> items = new ArrayList<>();
            items.add(new Map[] {
                    singletonMap("key", "Value"),
                    singletonMap("hey", 1),
                    singletonMap("tramp", Boolean.FALSE)
            });
            items.add(new Map[] {
                    singletonMap("top", "Kek"),
                    singletonMap("yolo", Boolean.TRUE)
            });
            items.add(new Map[] {
                    singletonMap("something", "Bad")
            });

            Rop.of(items)
                    .assertEquals("[0][0].key", "Value")
                    .assertEquals("[0][1].hey", "1")
                    .assertEquals("[0][2].tramp", "false")
                    .newLine()
                    .assertEquals("[1][0].top", "Kek")
                    .assertEquals("[1][1].yolo", "true")
                    .newLine()
                    .assertEquals("[2][0].something", "Bad");
        }
    }

    public void startsWithTest() throws Exception {
        RegularTestObject testObject = new RegularTestObject("Jim", 17, Boolean.TRUE, null, null);

        Rop.of(testObject)
                .assertStartsWith("name", "Ji")
                .assertStartsWith("kidCount", "1")
                .assertStartsWith("married", "tru");
    }

    public void containsTest() throws Exception {
        RegularTestObject testObject = new RegularTestObject("Alexander", 575, Boolean.FALSE, null, null);

        Rop.of(testObject)
                .assertContains("name", "xan")
                .assertContains("kidCount", "7")
                .assertContains("married", "ls");
    }

    public void emptyTest() throws Exception {
        RegularTestObject testObject = new RegularTestObject("", null, null, null, null);
        Rop.of(testObject)
                .assertEmpty("name");
    }

    public void nullTest() throws Exception {
        RegularTestObject testObject = new RegularTestObject(null, null, null, null, null);

        Rop.of(testObject)
                .assertNull("name")
                .assertNull("kidCount")
                .assertNull("married")
                .assertNull("born")
                .assertNull("died");

        NestedTestObject nestedTestObject = new NestedTestObject();

        NestedTestObject.Car car = new NestedTestObject.Car();
        nestedTestObject.setCars(Collections.singletonList(car));

        Rop.of(nestedTestObject)
                .assertNull("cars[0].make")
                .assertNull("cars[0].model");
    }

    public void notNullTest() throws Exception {
        RegularTestObject testObject = new RegularTestObject("Thor", 2, Boolean.FALSE, Calendar.getInstance(), Instant.now());

        Rop.of(testObject)
                .assertNotNull("name")
                .assertNotNull("kidCount")
                .assertNotNull("married")
                .assertNotNull("born")
                .assertNotNull("died");
    }
}
