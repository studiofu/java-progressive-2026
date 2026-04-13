# Day 6: Quality of Life — Unnamed Variables, Optional, & Modern APIs

Let's wrap up Phase 1! This day is all about the small improvements that polish the rough edges you likely experienced daily in Java 8. None of these are architecturally earth-shattering on their own — but together they remove an enormous amount of friction from everyday coding.

---

## 1. Unnamed Variables (`_`) — Java 22

Finalized in Java 22, the underscore (`_`) can now be used as a variable name to intentionally ignore a value. It clearly signals to both the compiler and other developers: "I don't care about this value."

### `catch` blocks

```java
// Java 8 — unused variable, IDE warning, noise
try {
    int number = Integer.parseInt(badInput);
} catch (NumberFormatException e) {
    System.out.println("Failed to parse, moving on.");
    // e is never used — IDE flags a warning
}

// Modern Java — explicit: "I don't need the exception"
try {
    int number = Integer.parseInt(badInput);
} catch (NumberFormatException _) {
    System.out.println("Failed to parse, moving on.");
}
```

### Lambdas with unused parameters

```java
// Java 8 — unused 'count' parameter
map.forEach((key, count) -> System.out.println(key));

// Modern Java — skip what you don't need
map.forEach((key, _) -> System.out.println(key));
```

### In pattern matching (with multiple components)

```java
// Only care about the x coordinate
if (obj instanceof Point(int x, _)) {
    System.out.println("x = " + x);
}

// Only care about the record type, not its fields
if (shape instanceof Circle(_)) {
    System.out.println("It's a circle");
}
```

### Rules

- You can use `_` multiple times in the same scope (unlike named variables)
- You cannot read from `_` — it has no value you can reference
- The compiler completely ignores the binding

---

## 2. Advanced Optional — Java 9/10/11+

Java 8's `Optional` was a step forward for null safety, but it often forced you back into imperative style with `if (opt.isPresent()) { ... } else { ... }`. Modern Java adds methods that make `Optional` fully functional and chainable.

### `ifPresentOrElse()` — Java 9

Handle both the present and empty cases in one call, no `if` needed:

```java
// Java 8 — branching with isPresent()
Optional<String> user = Optional.empty();
if (user.isPresent()) {
    System.out.println("Found: " + user.get());
} else {
    System.out.println("No user found, creating guest session.");
}

// Modern Java — functional, chainable
user.ifPresentOrElse(
    u  -> System.out.println("Found: " + u),
    () -> System.out.println("No user found, creating guest session.")
);
```

### `or()` — Java 9

Provide a fallback `Optional` instead of a raw value:

```java
Optional<String> primary = Optional.empty();
Optional<String> result = primary.or(() -> Optional.of("guest"));
// result = Optional["guest"]
```

This differs from `orElse()` — `or()` returns another `Optional`, keeping you in the pipeline.

### `.stream()` — Java 9

Convert an `Optional` into a `Stream` of zero or one element — perfect for flat-mapping:

```java
List<Optional<String>> maybeNames = List.of(
    Optional.of("Alice"),
    Optional.empty(),
    Optional.of("Charlie")
);

List<String> present = maybeNames.stream()
        .flatMap(Optional::stream)
        .toList();
// ["Alice", "Charlie"]
```

### `isEmpty()` — Java 11

The inverse of `isPresent()`, for when the negative check reads better:

```java
if (user.isEmpty()) {
    System.out.println("No user found");
}
```

### Quick reference

| Method | Added | Returns | Use case |
|---|---|---|---|
| `ifPresentOrElse()` | Java 9 | `void` | Handle both cases functionally |
| `or()` | Java 9 | `Optional` | Fallback to another Optional |
| `.stream()` | Java 9 | `Stream<T>` | FlatMap into stream pipelines |
| `isEmpty()` | Java 11 | `boolean` | Negative presence check |
| `orElseThrow()` | Java 10 | `T` | Get value or throw `NoSuchElementException` (no message needed) |

---

## 3. String One-Liners — Java 11+

### `String.isBlank()`

Checks if a string is empty **or contains only whitespace** — unlike `isEmpty()` which only checks length:

```java
"   ".isEmpty();   // false — length is 3
"   ".isBlank();   // true  — only whitespace
"".isBlank();      // true
"hello".isBlank(); // false
```

### `String.lines()`

Splits a string into a `Stream<String>` by line terminators, stripping trailing line breaks:

```java
String text = """
        First Line

        Third Line
        """;

text.lines()
    .filter(line -> !line.isBlank())
    .forEach(System.out::println);
// Prints: First Line
//         Third Line
```

The empty middle line (just whitespace) is filtered out. No `split("\\R")`, no `trim()`, no manual iteration.

### Other useful String additions

| Method | Java Version | What it does |
|---|---|---|
| `isBlank()` | Java 11 | Empty or whitespace-only |
| `lines()` | Java 11 | `Stream<String>` of lines |
| `strip()` | Java 11 | Unicode-aware trim (use instead of `trim()`) |
| `stripLeading()` / `stripTrailing()` | Java 11 | Strip one side only |
| `repeat(n)` | Java 11 | `"ha".repeat(3)` → `"hahaha"` |
| `formatted(args...)` | Java 15 | `"Hello %s".formatted("World")` — String's own `format` |

---

## 4. File One-Liners — Java 11+

### Reading and writing entire files

```java
// Creating, writing, and reading a file in 3 lines
Path tempFile = Files.createTempFile("modern_java_", ".txt");
Files.writeString(tempFile, "File I/O is finally simple in Java!");
String content = Files.readString(tempFile);
Files.delete(tempFile);
```

No `BufferedReader`, no `InputStream`, no `try-with-resources` for simple operations.

### Compare with Java 8

```java
// Java 8 — reading a file
BufferedReader reader = new BufferedReader(new FileReader("data.txt"));
StringBuilder sb = new StringBuilder();
String line;
while ((line = reader.readLine()) != null) {
    sb.append(line);
}
String content = sb.toString();
reader.close();

// Java 8 — writing a file
BufferedWriter writer = new BufferedWriter(new FileWriter("data.txt"));
writer.write("Hello");
writer.close();
```

9 lines of boilerplate reduced to 1. The `Files` methods handle charset (UTF-8 by default), resource cleanup, and buffering internally.

### Quick reference

| Method | What it does |
|---|---|
| `Files.readString(path)` | Read entire file as a `String` |
| `Files.writeString(path, text)` | Write a `String` to a file |
| `Files.writeString(path, text, CREATE, APPEND)` | Append to a file |
| `Files.createTempFile(prefix, suffix)` | Create a temp file |

---

## Phase 1 Complete

Six days in, and you've overhauled your daily Java experience:

| Day | Feature | Lines saved |
|---|---|---|
| 1 | `var`, text blocks, switch expressions | Generic declarations, escapes, fall-through |
| 2 | Records | 30-line POJOs → 1 line |
| 3 | Pattern matching | `instanceof` + cast + assign → one step |
| 4 | Sealed classes | `default` branches eliminated |
| 5 | Sequenced collections, `.toList()`, gatherers | Index math, collectors, grouping boilerplate |
| 6 | `_`, modern Optional, String/File APIs | Unused vars, `isPresent()` checks, I/O loops |

Phase 2 takes this foundation into **concurrency** — where Project Loom's Virtual Threads will feel just as transformative.

---

## Source Code

The runnable example for today is at `src/Day6QualityOfLife.java`.

---

## TL;DR

| Feature | One-liner |
|---|---|
| `catch (Exception _)` | Ignore the exception object intentionally |
| `(key, _) -> ...` | Skip unused lambda parameters |
| `opt.ifPresentOrElse()` | Handle present and empty in one call |
| `opt.or(() -> fallback)` | Chain fallback Optionals |
| `opt.stream()` | FlatMap Optionals into streams |
| `str.isBlank()` | Empty or whitespace-only check |
| `str.lines()` | Stream of lines from a string |
| `Files.readString(path)` | Read a file in one call |
| `Files.writeString(path, text)` | Write a file in one call |
