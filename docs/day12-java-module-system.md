# Day 12: The Java Module System (JPMS)

Welcome to Phase 3: Architecture & Cloud-Native. We're shifting gears from writing individual lines of code to how modern Java applications are packaged, deployed, and optimized for the cloud.

This phase starts with the most fundamental architectural change to the Java platform: the Java Module System.

---

## The Problem: JAR Hell

In Java 8, every library you used — Spring, Hibernate, Guava, your own code — went onto a single giant classpath. This caused two critical problems:

### No Real Encapsulation

If a class was `public`, it was public to *everyone*. You couldn't say "this class is public, but only for my own team's packages." Internal implementation details were wide open for anyone to depend on — and when you changed them, downstream code broke.

### Massive Runtime

The entire JRE was bundled as one monolith. Even if your tiny web app didn't use Swing, AWT, CORBA, or java.sql, those libraries were loaded into memory anyway. You paid the cost for everything, whether you used it or not.

---

## The Solution: Modules

Introduced in Java 9, JPMS lets you group packages into **modules**. A module is a fortress around your code — you explicitly declare what it needs and what it exposes.

### Key Concepts

| Keyword | Meaning |
|---|---|
| `module` | Declares a named module |
| `requires` | This module depends on another module |
| `exports` | Makes a specific package visible to other modules |

If a package is **not** exported, it is strictly invisible to the outside world — even if every class inside it is `public`.

---

## The Demo: A Two-Module Banking App

The example is spread across two modules:

```
bank.core/                      ← the core banking library
  module-info.java
  com/bank/core/
    Account.java                ← exported, visible to everyone
    internal/
      InternalAudit.java        ← NOT exported, hidden from everyone

bank.api/                       ← the API application
  module-info.java
  com/bank/api/
    Main.java                   ← uses Account, tries to use InternalAudit
```

### bank.core — The Library Module

```java
// bank.core/module-info.java
module bank.core {
    exports com.bank.core;        // only this package is visible
    // com.bank.core.internal is NOT exported
}
```

This means:
- `Account` in `com.bank.core` — accessible from other modules
- `InternalAudit` in `com.bank.core.internal` — **blocked**, even though it's `public`

### bank.api — The Application Module

```java
// bank.api/module-info.java
module bank.api {
    requires bank.core;           // we need the core module
}
```

### Main.java — What Works and What Doesn't

```java
import com.bank.core.Account;                        // works — exported
// import com.bank.core.internal.InternalAudit;      // compile error! not exported

public class Main {
    public static void main(String[] args) {
        Account myAccount = new Account();            // works
        System.out.println(myAccount.getAccountDetails());

        // InternalAudit audit = new InternalAudit(); // would fail to compile!
    }
}
```

Try uncommenting the `InternalAudit` import — the compiler will reject it. That's the module system enforcing encapsulation at compile time.

---

## How to Run

Since this is a multi-module project, it needs a two-step compile:

```bash
# Step 1: Compile the core module
javac --module-source-path bank.core -d out-bank bank.core/module-info.java bank.core/com/bank/core/Account.java bank.core/com/bank/core/internal/InternalAudit.java

# Step 2: Compile the API module (which requires the core module)
javac --module-path out-bank --module-source-path bank.api -d out-bank bank.api/module-info.java bank.api/com/bank/api/Main.java

# Step 3: Run
java --module-path out-bank -m bank.api/com.bank.api.Main
```

### Expected output

```
--- Starting Bank API ---
Account Balance: $5,000
```

---

## Why This Matters Beyond Encapsulation

### Custom Runtime Images with jlink

Because modules declare their dependencies explicitly, you can use `jlink` to build a minimal JRE that contains **only** the modules your app needs:

```bash
jlink --add-modules bank.api --output my-custom-jre
```

Instead of shipping a 200+ MB JDK, you ship a 30-50 MB custom runtime with only what your application uses. This is a game-changer for containers and cloud deployments.

### Reliable Configuration

If `bank.api` declares `requires bank.core`, and `bank.core` is missing from the module path, the JVM fails at startup — not at runtime when a class is first accessed. You catch missing dependencies immediately.

### Strong Encapsulation

No more `sun.misc.Unsafe` hacks. The JDK itself is modularized — internal APIs are locked down. Libraries that relied on JDK internals were forced to use public APIs instead, making the ecosystem healthier.

---

## Source Code

The example for today is in two directories:
- `bank.core/` — the library module
- `bank.api/` — the application module

---

## TL;DR

| What | One-liner |
|---|---|
| `module-info.java` | Declares a module at the root of your source tree |
| `exports com.my.pkg` | Makes a package visible to other modules |
| `requires other.module` | Declares a dependency on another module |
| Unexported packages | Invisible to the outside world, even if classes are `public` |
| `jlink` | Build a minimal custom JRE with only the modules you need |
