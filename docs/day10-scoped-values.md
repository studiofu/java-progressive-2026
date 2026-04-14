# Day 10: Scoped Values (The Modern ThreadLocal)

Now that you're running 100,000 Virtual Threads and managing them safely with Structured Concurrency, we need to talk about data sharing. How do you pass context — a logged-in user ID, a correlation ID, a transaction token — deeply through 10 layers of method calls without polluting every single signature?

In Java 8, the answer was `ThreadLocal`. It worked, but it has critical flaws in the era of Virtual Threads.

---

## The Problem with ThreadLocal

### Mutability

Anyone down the call stack can change a `ThreadLocal` value:

```java
// Layer 1: set the user
CurrentUser.set("Alice");

// Layer 7: someone silently overwrites it
CurrentUser.set("Eve");  // <-- bug! and no compiler will catch it

// Layer 10: reads the wrong user
String user = CurrentUser.get();  // "Eve" — not "Alice"
```

These bugs are incredibly hard to track because the mutation can happen anywhere in a deep call stack.

### Memory Leaks

If you forget to call `.remove()` when you're done, the data stays in memory forever. This is one of the most common sources of `OutOfMemoryError` in Java 8 web applications — especially in servlet containers that pool and reuse threads.

```java
try {
    CurrentUser.set("Alice");
    processRequest();
} finally {
    CurrentUser.remove();  // forget this line → leak
}
```

### Memory Bloat

With 100,000 Virtual Threads, each one gets its own `ThreadLocal` map. If each thread stores three values, that's 300,000 map entries — most of which point to the same data. The memory overhead adds up fast.

---

## The Solution: Scoped Values

Scoped Values solve all three problems. They are designed explicitly for the era of Virtual Threads.

| Problem | ThreadLocal | Scoped Value |
|---|---|---|
| **Mutability** | Anyone can change it at any time | **Immutable** — once set, it cannot be changed |
| **Memory leaks** | Must manually call `.remove()` | **Auto-cleaning** — destroyed when the block exits |
| **Memory bloat** | Each thread gets its own map | **Optimized** — cheap to share across millions of sub-tasks |

---

## The API in Three Steps

### 1. Declare the Scoped Value

```java
public static final ScopedValue<String> LOGGED_IN_USER = ScopedValue.newInstance();
```

This is typically `public static final` so any class in your application can read it.

### 2. Bind it to a block of code

```java
ScopedValue.where(LOGGED_IN_USER, "Admin_Alice").run(() -> {
    controllerLayer();
});
```

The value exists **only** inside this block. The exact millisecond `.run()` finishes, the value is destroyed. No `.remove()` needed.

### 3. Read it anywhere in the call stack

```java
static void repositoryLayer() {
    String user = LOGGED_IN_USER.get();
    System.out.println("Saving record. Audited by: " + user);
}
```

No need to pass it through `controllerLayer()` or `serviceLayer()` as a parameter.

---

## The Demo

The example at `src/Day10ScopedValues.java` simulates a web request where the logged-in user ID needs to reach the database layer for auditing — without being passed through intermediate methods.

### What happens

1. `LOGGED_IN_USER` is bound to `"Admin_Alice"` for the duration of the `.run()` block
2. `controllerLayer()` calls `serviceLayer()` calls `repositoryLayer()` — no user ID in any signature
3. `repositoryLayer()` reads the user via `LOGGED_IN_USER.get()`
4. After `.run()` completes, `LOGGED_IN_USER.isBound()` returns `false` — the value is gone

### Expected output

```
--- Starting Web Request ---
Executing request block...
>> [Database] Saving record. Audited by: Admin_Alice
--- Web Request Finished ---

Is user bound outside the scope? false
```

---

## Multiple Scoped Values

The commented-out section in the source file shows how to bind multiple values at once by chaining `.where()` calls:

```java
ScopedValue.where(LOGGED_IN_USER, "Admin_Alice")
           .where(TENANT_ID, "HK_Datacenter_01")
           .where(TRACE_ID, "req-98765-xyz")
           .run(() -> {
               controllerLayer();
           });
```

Then anywhere in the call stack:

```java
static void repositoryLayer() {
    System.out.println("Audited by : " + LOGGED_IN_USER.get());
    System.out.println("Tenant     : " + TENANT_ID.get());
    System.out.println("Trace ID   : " + TRACE_ID.get());
}
```

This is the modern replacement for the common Java 8 pattern of using multiple `ThreadLocal`s for request-scoped context (user, tenant, trace ID, locale, etc.).

---

## Scoped Values + Virtual Threads

Scoped Values are designed to work hand-in-hand with Virtual Threads and Structured Concurrency:

```java
try (var scope = StructuredTaskScope.open()) {
    ScopedValue.where(LOGGED_IN_USER, "Admin_Alice").run(() -> {
        var task1 = scope.fork(() -> fetchFromServiceA()); // sees LOGGED_IN_USER
        var task2 = scope.fork(() -> fetchFromServiceB()); // sees LOGGED_IN_USER
        scope.join();
    });
}
```

Both subtasks automatically inherit the scoped value from their parent. No manual passing. No thread-local inheritance bugs. No memory leaks.

---

## How to Run

```bash
java --enable-preview --source 25 src/Day10ScopedValues.java
```

---

## Source Code

The runnable example for today is at `src/Day10ScopedValues.java`.

---

## TL;DR

| What | One-liner |
|---|---|
| `ScopedValue.newInstance()` | Declare a scoped value |
| `ScopedValue.where(KEY, value).run(() -> { ... })` | Bind a value for a block of code |
| `KEY.get()` | Read the value anywhere in the call stack |
| Immutable | Cannot be changed once bound |
| Auto-cleaning | Destroyed when the `.run()` block exits |
| Chain `.where()` | Bind multiple values at once |
