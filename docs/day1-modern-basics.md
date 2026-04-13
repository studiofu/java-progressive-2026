# Day 1: Modern Basics — var, Text Blocks, and Switch Expressions

Let's dive right in. Today is about shedding the verbosity of Java 8.

---

## 1. Local Variable Type Inference (`var`) — Java 10+

The compiler can now infer the type of a local variable from its initializer. No more typing out long generic signatures twice.

### Java 8 (verbose)

```java
Map<String, Integer> namesToAge = Map.of("Alice", 25, "Bob", 30);
```

### Modern Java (clean)

```java
var namesToAge = Map.of("Alice", 25, "Bob", 30);
```

The type is still **statically typed** — `var` is not `dynamic` or `Object`. The compiler infers `Map<String, Integer>` and that's what you get. IDEs and compilers know the exact type.

### When to use `var`

- Right-hand side makes the type obvious (constructor call, factory method)
- Long generic types that add noise (`Map<String, List<Predicate<String>>>`)
- Loop variables in enhanced for-each

### When NOT to use `var`

- The initializer doesn't make the type clear (`var result = process(data)`)
- You specifically want to upcast to a broader type (`List` vs `ArrayList`)
- Method parameters or return types — `var` is only for **local variables**

---

## 2. Text Blocks (`"""`) — Java 15+

Multi-line strings without escape-character soup. Perfect for JSON, SQL, HTML, or any embedded text.

### Java 8 (concatenation nightmare)

```java
String json = "{\n" +
              "  \"name\": \"John\",\n" +
              "  \"age\": 30\n" +
              "}";
```

### Modern Java (readable)

```java
String json = """
        {
          "name": "John",
          "age": 30
        }
        """;
```

### Key rules

- The opening `"""` must be followed by a newline (content starts on the next line).
- The closing `"""` controls indentation — Java strips common leading whitespace automatically. Place it at the leftmost column of your desired indentation level.
- No need to escape double quotes inside the block (single `"` just works).
- Still need to escape backslashes and triple quotes.

### Bonus: `stripIndent()` and `formatted()`

```java
String query = """
        SELECT * FROM users
        WHERE age > %d
        """.formatted(21);
```

---

## 3. Switch Expressions — Java 14+

Switch can now **return a value**, uses lambda-like arrows (`->`), and eliminates fall-through bugs entirely.

### Java 8 (error-prone)

```java
String typeOfDay;
switch (day) {
    case "MONDAY":
    case "TUESDAY":
    case "WEDNESDAY":
    case "THURSDAY":
    case "FRIDAY":
        typeOfDay = "Weekday";
        break;
    case "SATURDAY":
    case "SUNDAY":
        typeOfDay = "Weekend";
        break;
    default:
        throw new IllegalArgumentException("Invalid day: " + day);
}
```

Pitfalls: missing `break` causes silent fall-through, no compile-time check for completeness, multiple lines of boilerplate just to assign a variable.

### Modern Java (expression form)

```java
var typeOfDay = switch (day) {
    case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
    case "SATURDAY", "SUNDAY" -> "Weekend";
    default -> throw new IllegalArgumentException("Invalid day: " + day);
};
```

### What changed

| Feature | Old switch | Switch expressions |
|---|---|---|
| Returns a value | No | Yes |
| Syntax | `case X:` with `break` | `case X ->` (no fall-through) |
| Multi-case | Stack labels with fall-through | Comma-separated: `case A, B, C ->` |
| Exhaustiveness | Not enforced | Must be exhaustive (cover all cases or have `default`) |
| Use as expression | No | Yes — assign directly to a variable |

### Arrow vs colon in switch expressions

You can still use the colon form inside an expression, but you must `yield` a value:

```java
var result = switch (status) {
    case 200: yield "OK";
    case 404: yield "Not Found";
    default: yield "Unknown";
};
```

The arrow form (`->`) is preferred — it's shorter and never has fall-through.

---

## Source Code

The runnable example for today is at `src/Day1ModernBasics.java`.

---

## TL;DR

| Feature | Java Version | One-liner |
|---|---|---|
| `var` | Java 10 | Let the compiler infer local variable types |
| `"""` text blocks | Java 15 | Multi-line strings without escape chaos |
| Switch expressions | Java 14 | Switch that returns a value, no fall-through |
