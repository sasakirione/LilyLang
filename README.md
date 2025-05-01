# LilyLang

LilyLang is a simple programming language with a clean syntax, designed for educational purposes. It compiles to JVM bytecode.

## Features

- Variable declarations and assignments
- Basic data types (integers, booleans, strings)
- Arithmetic and logical operations
- Control structures (if/else, while, for)
- Function declarations and calls
- User-defined data structures/classes

## Syntax Examples

### Variables

```
var x = 10;
var name = "John";
var isActive = true;
```

### Arithmetic Operations

```
var a = 5 + 3;   // Addition
var b = 10 - 4;  // Subtraction
var c = 3 * 7;   // Multiplication
var d = 20 / 5;  // Division
var e = 10 % 3;  // Modulo
```

### Logical Operations

```
var a = true and false;  // Logical AND
var b = true or false;   // Logical OR
var c = not true;        // Logical NOT
```

### Control Structures

#### If/Else

```
if x > 5 {
    print "x is greater than 5";
} else {
    print "x is not greater than 5";
}
```

#### While Loop

```
var i = 0;
while i < 5 {
    print i;
    i = i + 1;
}
```

#### For Loop

```
for var i = 0; i < 5; i = i + 1 {
    print i;
}
```

### Functions

```
fun add(a, b) {
    var result = a + b;
    result;  // Implicit return
}

var sum = add(5, 3);
print sum;  // Outputs: 8
```

### Classes

```
class Person {
    var name = "Unknown";
    var age = 0;

    fun setName(newName) {
        name = newName;
    }

    fun setAge(newAge) {
        age = newAge;
    }

    fun introduce() {
        print "Hello, my name is " + name + " and I am " + age + " years old.";
    }
}

// Create a Person instance
var person = new Person();

// Set properties
person.setName("John");
person.setAge(30);

// Call a method
person.introduce();  // Outputs: Hello, my name is John and I am 30 years old.
```

## Requirements

* Kotlin
* ASM (Java bytecode manipulation library)

## Running LilyLang Programs

To run a LilyLang program, use the following command:

```
java -jar lilylang.jar your_program.lily
```

## License

"LilyLang" is under [MIT license](https://en.wikipedia.org/wiki/MIT_License).
