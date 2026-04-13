# Day 4: Domain Modeling — Sealed Classes & Interfaces

In Java 8, inheritance was the Wild West. If you created a public interface `Shape`, absolutely anyone, anywhere, could implement it. Your only tool to stop inheritance was the `final` keyword, which stops **everyone** from extending a class. There was no middle ground.

Introduced in Java 17, **Sealed Classes and Interfaces** give you exact control over your class hierarchies. You can declare exactly which classes are allowed to extend or implement your type.

---

## The Problem

```java
// Java 8 — anyone can implement this
interface PaymentMethod {}

// In your codebase
class CreditCard implements PaymentMethod {}
class PayPal implements PaymentMethod {}

// ... but somewhere in another module, someone adds:
class CryptoScam implements PaymentMethod {}  // you can't stop this
```

Your `switch` statements always needed a `default` branch because you could never be sure you'd seen every implementation. Miss a type? Silent bug. The compiler couldn't help you.

## The Solution

```java
sealed interface PaymentMethod permits CreditCard, PayPal, Cash {}
```

The `sealed` keyword plus `permits` lists exactly which types are allowed. The compiler now **guarantees** this is the complete set.

---

## The Three Keywords

When a class or record inherits from a sealed type, it must declare its own inheritance status using one of three keywords:

### `final` — closed forever

```java
final class Cash implements PaymentMethod {
    private final double amount;
    Cash(double amount) { this.amount = amount; }
    public double getAmount() { return amount; }
}
```

No one can extend `Cash`. The hierarchy stops here.

### `sealed` — selectively open

```java
sealed interface PaymentMethod permits CreditCard, PayPal, Cash {}
```

The subtype itself is sealed — it permits its own specific list of subtypes, creating a deeper controlled hierarchy.

### `non-sealed` — back to the Wild West

```java
non-sealed class DigitalWallet implements PaymentMethod {
    // Anyone can now extend DigitalWallet again
}
```

Opens the hierarchy back up. Anyone can extend it. Use this when you want to restrict the top level but allow an open extension point at a specific branch.

### Records — implicitly `final`

```java
record CreditCard(String cardNumber, String expiry) implements PaymentMethod {}
record PayPal(String email) implements PaymentMethod {}
```

Records are `final` by default, so they satisfy sealed hierarchy rules without any extra keyword.

---

## Why This Is a Game-Changer: Exhaustiveness Checking

When you restrict who can implement an interface, the compiler knows **every possible subtype** that exists. This transforms your `switch` statements:

```java
static String processPayment(PaymentMethod payment) {
    return switch (payment) {
        case CreditCard cc -> "Processing card ending in " + cc.cardNumber().substring(15);
        case PayPal pp     -> "Sending redirect link to " + pp.email();
        case Cash c        -> "Please collect " + c.getAmount() + " at the counter.";
        // No 'default' branch needed!
    };
}
```

### What the compiler now does for you

- **No `default` needed** — it knows every permitted type is covered
- **Missing case = compile error** — try removing the `Cash` case, the build breaks
- **New type = broken build** — if you add `Bitcoin` to the `permits` list later, every `switch` that doesn't handle it fails to compile

This is the compiler working **for** you. It's like having tests that the type system runs automatically.

### Compare with Day 3

In Day 3, our `Shape` interface was unsealed, so we needed a `default` branch:

```java
// Day 3 — unsealed, needs default
return switch (shape) {
    case Circle c    -> ...;
    case Rectangle r -> ...;
    default          -> "Unknown shape";  // safety net required
};
```

With a sealed hierarchy, the `default` disappears:

```java
// Day 4 — sealed, compiler knows all types
return switch (payment) {
    case CreditCard cc -> ...;
    case PayPal pp     -> ...;
    case Cash c        -> ...;
    // done — no default needed
};
```

---

## Rules for Sealed Hierarchies

| Rule | Detail |
|---|---|
| `permits` is required | Lists the exact subtypes allowed |
| Every permit must be `final`, `sealed`, or `non-sealed` | The compiler enforces this |
| Permitted types must be in the same package | Or the same compilation unit (same file) |
| Records are implicitly `final` | No keyword needed for record subtypes |
| Sealed + pattern matching = exhaustiveness | The killer combo |

---

## Practical Use Cases

- **Domain modeling** — payment methods, order statuses, user roles — anywhere the set of types is known and closed
- **Algebraic Data Types (ADTs)** — functional programming pattern where data is modeled as a closed set of variants
- **API responses** — sealed result types (`Success | Error | Timeout`) that force callers to handle every case
- **State machines** — sealed state types where every transition must be explicitly handled

---

## Source Code

The runnable example for today is at `src/Day4SealedClasses.java`.

---

## TL;DR

| Keyword | What it means |
|---|---|
| `sealed` | Only listed types can extend/implement |
| `permits` | Names the allowed subtypes |
| `final` | Subtype cannot be extended further |
| `non-sealed` | Subtype opens back up to anyone |
| Records | Implicitly `final` — just implement the sealed type |

| Why it matters | One-liner |
|---|---|
| Exhaustiveness | Compiler forces you to handle every permitted type |
| No more `default` | Switches are complete by construction |
| Safe refactoring | Adding a new type breaks every incomplete switch |
