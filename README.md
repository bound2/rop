# rop - response object proxy

Have you ever had a problem, where you refactor java objects, and
accidentally end up breaking your API because it refactored all the tests?
Rop will eliminate this possibility by wrapping testable objects. Forcing the developer to use object field comparison with String literal.

## Usage

### Setup TestNg
```
// Second parameter defines consumer for printAssertions
Rop.setConfiguration(new TestngConfiguration(), System.out::println);
```
### Setup junit
```
// Second parameter defines consumer for printAssertions
Rop.setConfiguration(new JunitConfiguration(), System.out::println);
```

### Basic

```
Car car = new Car();
car.setMake("Audi");
car.setModel("A7");
car.setYear(2017);
car.setNotes(Arrays.asList("Good condition", "Low mileage");
Rop.of(car)
    .assertEquals("make", "Audi")
    .assertEquals("model", "A7")
    .assertEquals("year", "2017")
    .assertEquals("notes[0]", "Good condition")
    .assertEquals("notes[1]", "Low mileage")
    .assertArraySize("notes", 2);
```

### Print all data
```
Rop.of(car).printAssertions();
```

### Separate elements logically
```
List<String> elements = Arrays.asList(
    "int",
    "double",
    "short",
    "String",
    "Object"
);
Rop.of(elements)
    .assertEquals("[0]", "int")
    .assertEquals("[1]", "double")
    .assertEquals("[2]", "short")
    .separator()
    .assertEquals("[3]", "String")
    .assertEquals("[4]", "Object")
    .separator()
    .assertArraySize(5);
```
