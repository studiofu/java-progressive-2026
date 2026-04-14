# Day 13: GraalVM Native Image

Yesterday you saw how the Java Module System lets you strip unused parts of the JDK. Today we go further: **compiling Java directly to a native executable** that doesn't need a JVM at all.

GraalVM Native Image performs ahead-of-time (AOT) compilation — it analyzes your Java bytecode at build time and produces a standalone machine-code binary. The result: startup times in milliseconds instead of seconds, and a fraction of the memory footprint.

---

## Why This Matters

| Metric | JVM mode | Native Image |
|---|---|---|
| Startup time | ~200-500ms (JVM must load, JIT-compile, warm up) | ~5-20ms (already compiled, just runs) |
| Memory usage | ~150-300 MB baseline | ~15-30 MB |
| Peak throughput | Higher (JIT optimizes over time) | Lower (no runtime JIT) |
| Build time | Seconds | Minutes |

Native Image is ideal for:
- **Serverless / Lambda functions** — pay-per-invocation, cold start matters
- **CLI tools** — instant startup, no JVM warm-up
- **Microservices in containers** — smaller images, less memory, faster scaling

It's *not* ideal for long-running, CPU-heavy workloads where the JIT has time to optimize — the JVM will eventually outperform native image there.

---

## The Demo

The example at `src/demos/Day13GraalVM.java` is intentionally minimal — it measures its own startup time:

```java
public class Day13GraalVM {
    private static final long START_TIME = System.nanoTime();

    public static void main(String[] args) {
        long endTime = System.nanoTime();
        double startupTimeMs = (endTime - START_TIME) / 1_000_000.0;
        System.out.println("Hello, Cloud-Native Java!");
        System.out.println("Time taken to boot and execute: " + startupTimeMs + " ms");
    }
}
```

Run it on the JVM and you'll see something like ~50-100ms. Compile it as a native image and it drops to ~2-5ms.

---

## Setting Up on Windows 11

Native Image translates Java bytecode into machine code, which requires a C/C++ compiler for the final linking step. Windows doesn't ship with one, so setup is more involved than on macOS or Linux.

### Step 1: Install Visual Studio Build Tools

Native Image on Windows requires the Microsoft Visual C++ (MSVC) toolchain.

1. Download **Visual Studio Build Tools** from the Microsoft website (you don't need the full IDE)
2. Run the installer
3. Check **"Desktop development with C++"**
4. On the right side under "Installation details", ensure **Windows 11 SDK** is checked
5. Click Install (large download, a few GB)

### Step 2: Install GraalVM JDK

A standard OpenJDK won't have the `native-image` command. You need GraalVM.

1. Download **GraalVM JDK** for Windows x64 (JDK 21 or 25) from the official GraalVM website
2. Extract the `.zip` to a permanent location (e.g. `C:\Program Files\Java\graalvm-jdk-25`)
3. Update environment variables:
   - Press Windows key, type **"Environment Variables"**, hit Enter
   - Set or edit `JAVA_HOME` to point to the GraalVM folder
   - Add `%JAVA_HOME%\bin` to `Path`
4. Verify in a command prompt: `java -version` should mention GraalVM

### Step 3: Open the x64 Native Tools Command Prompt

This is the most critical Windows step. A normal Command Prompt won't have the C++ compiler on its path.

1. Press Windows key
2. Type **x64 Native** in the search bar
3. Open **"x64 Native Tools Command Prompt for VS 2022"**
4. This terminal has all MSVC environment variables pre-configured

### Step 4: Compile and Build

Inside the x64 Native Tools terminal:

```bash
# Compile the Java file
javac src/demos/Day13GraalVM.java

# Build the native image
native-image demos.Day13GraalVM

# Run the resulting executable
.\demos.Day13GraalVM.exe
```

### Expected output (native image)

```
Hello, Cloud-Native Java!
Time taken to boot and execute: 2.3 ms
```

Compare that to the JVM version — typically 50-100ms for the same program.

---

## How It Works

```
Your Java source
      │
      ▼
   javac (compile to bytecode)
      │
      ▼
   native-image (AOT analysis + compilation)
      │
      ├── Static analysis: which classes/methods are actually used?
      ├── Removes unreachable code (dead code elimination)
      ├── Compiles to machine code (no JVM needed)
      │
      ▼
   Standalone executable (.exe on Windows)
```

Key trade-offs to be aware of:

| Feature | Supported | Notes |
|---|---|---|
| Reflection | Limited | Must configure via `reflect-config.json` or use agent to trace |
| Dynamic class loading | Limited | Classes must be known at build time |
| JNI | Supported | Requires configuration |
| Serialization | Limited | Requires configuration |
| All standard libraries | Mostly | Some dynamic features need explicit registration |

For Spring Boot applications, Spring Native / Spring Boot 3+ handles most of this configuration automatically.

---

## Source Code

The runnable example for today is at `src/demos/Day13GraalVM.java`.

---

## TL;DR

| What | One-liner |
|---|---|
| `native-image demos.Day13GraalVM` | Compile Java to a standalone native executable |
| Startup time | Drops from ~100ms to ~2-5ms |
| Memory usage | Drops from ~200MB to ~20MB |
| Best for | Serverless, CLI tools, container microservices |
| Windows setup | Visual Studio Build Tools + GraalVM JDK + x64 Native Tools terminal |
