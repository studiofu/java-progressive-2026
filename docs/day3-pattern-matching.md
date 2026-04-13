# Day 3: Pattern Matching — instanceof, switch, and Record Patterns

Now that you understand Records (Day 2), let's look at how modern Java lets you extract data from them (and other objects) using **Pattern Matching**. This feature evolved over several Java versions and reached maturity in Java 21.

In Java 8, if you wanted to check an object's type and use it, you had to check the type, cast it, and assign it to a new variable — three steps every time. Pattern matching eliminates that repetitive dance.

---

## 1. Pattern Matching for `instanceof` — Java 16+

### Java 8 (check, cast, assign)

```java
if (someObject instanceof String) {
    String s = (String) someObject;   // manual cast
    System.out.println("Length: " + s.length());
}
```

Three lines just to use the object as its actual type. The compiler already knows it's a `String` inside the block — why do you have to say it again?

### Modern Java (check and bind in one step)

```java
if (someObject instanceof String s) {
    System.out.println("Length: " + s.length());
}
```

The pattern `String s` does two things: tests the type **and** binds the casted value to `s`. The variable `s` is scoped to the `if` block — it doesn't exist outside it.

### Flow scoping

The binding variable is only available where the pattern definitely matches:

```java
if (obj instanceof String s && s.length() > 5) {
    // s is usable here — the && ensures the instanceof succeeded first
    System.out.println(s);
}

if (!(obj instanceof String s)) {
    // s is NOT usable here — the match failed
    return;
}
// s IS usable here — the negated check guarantees it's a String
System.out.println(s.toUpperCase());
```

---

## 2. Pattern Matching for `switch` — Java 21+

You can now `switch` on an object's **type**, not just its value. Combined with the arrow syntax from Day 1, this is incredibly powerful.

### Java 8 (chained instanceof)

```java
static String describe(Shape shape) {
    if (shape instanceof Circle) {
        Circle c = (Circle) shape;
        return "A circle with radius " + c.radius();
    } else if (shape instanceof Rectangle) {
        Rectangle r = (Rectangle) shape;
        return "A rectangle measuring " + r.length() + "x" + r.width();
    } else {
        return "Unknown shape";
    }
}
```

### Modern Java (type patterns in switch)

```java
static String getShapeDescription(Shape shape) {
    return switch (shape) {
        case Circle c    -> "A circle with radius " + c.radius();
        case Rectangle r -> "A rectangle measuring " + r.length() + "x" + r.width();
        case null        -> "A null shape";
        default          -> "Some other shape we don't know about";
    };
}
```

### What's happening here

- `case Circle c` — tests if `shape` is a `Circle`, binds it to `c`
- `case null` — you can now match `null` directly in a switch (no more NPE!)
- The compiler checks **exhaustiveness** — if you don't cover all subtypes of `Shape`, it won't compile (unless you have a `default`)

### Guards with `when`

You can add conditions to a case using `when`:

```java
return switch (shape) {
    case Circle c when c.radius() > 10 -> "A large circle";
    case Circle c                       -> "A small circle";
    case Rectangle r                    -> "A rectangle";
    default                             -> "Unknown";
};
```

Guarded cases must appear **before** the unguarded version of the same pattern.

---

## 3. Record Patterns (Deconstruction) — Java 21+

This is where Records and Pattern Matching truly combine. You can "deconstruct" a Record directly inside an `instanceof` or `switch`, extracting its fields into variables without calling accessors.

### In `instanceof`

```java
Shape shape = new Rectangle(10.0, 20.0);

// No accessor calls — fields extracted directly
if (shape instanceof Rectangle(double length, double width)) {
    System.out.println("Area: " + (length * width));
}
```

Instead of `r.length()` and `r.width()`, the variables `length` and `width` are bound directly from the record's components.

### In `switch`

```java
return switch (shape) {
    case Circle(double radius)     -> "Circle area: " + (Math.PI * radius * radius);
    case Rectangle(double l, double w) -> "Rectangle area: " + (l * w);
    default                        -> "Unknown";
};
```

### Nested deconstruction

Records inside records? No problem:

```java
record Point(double x, double y) {}
record Line(Point start, Point end) {}

if (obj instanceof Line(Point(double x1, double y1), Point(double x2, double y2))) {
    double length = Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
    System.out.println("Line length: " + length);
}
```

The nesting goes as deep as your records do.

---

## Evolution Timeline

| Java Version | Feature |
|---|---|
| Java 16 | Pattern matching for `instanceof` (finalized) |
| Java 17 | Pattern matching for `switch` (preview) |
| Java 19 | Record patterns (preview) |
| Java 21 | Pattern matching for `switch` + record patterns (finalized) |

Java 21 is the milestone — all three pieces are production-ready.

---

## Source Code

The runnable example for today is at `src/Day3PatternMatching.java`.

---

## TL;DR

| Feature | What it does |
|---|---|
| `instanceof String s` | Type check + cast + bind in one step |
| `switch` type patterns | Branch logic by object type, not value |
| `case null` | Handle `null` directly in switch |
| `when` guards | Add conditions to case patterns |
| Record deconstruction | Extract record fields into variables without accessors |
