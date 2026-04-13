# 21-Day Modern Java Mastery Roadmap

A guided tour from Java 8 verbosity to Java 25 fluency. Each day introduces a feature with runnable code and a focused explanation.

---

## Phase 1: Modern Syntax & Expressiveness

Shed the boilerplate. These are the day-to-day improvements you'll feel immediately.

| Day | Topic | Focus Area |
|---|---|---|
| [Day 1](day1-modern-basics.md) | Modern Basics: `var`, Text Blocks, and Switch Expressions | Upgrading everyday syntax |
| [Day 2](day2-records.md) | Data Carriers: Records | Eliminating POJO boilerplate |
| [Day 3](day3-pattern-matching.md) | Pattern Matching: `instanceof`, `switch`, and Record Patterns | Writing safer, more concise logic |
| [Day 4](day4-sealed-classes.md) | Domain Modeling: Sealed Classes & Interfaces | Controlling inheritance for Data-Oriented Programming |
| [Day 5](day5-streams-gatherers-collections.md) | API Enhancements: Stream API, Gatherers, & Collections Updates | Modern functional data processing |
| [Day 6](day6-quality-of-life.md) | Quality of Life: Unnamed Variables (`_`), Advanced Optional, String/File APIs | Cleaning up edge cases and file I/O |

**Why this phase matters:** These features compound. Records give you immutable data carriers. Sealed classes let you model closed hierarchies. Pattern matching with switch gives you exhaustive handling of those hierarchies. By Day 5, you're writing code that is half the length and twice as safe as the Java 8 equivalent.

---

## Phase 2: Next-Gen Concurrency (Project Loom)

Virtual Threads change everything about Java concurrency. No more reactive frameworks, no more callback hell — just plain blocking code that scales like async.

| Day | Topic | Focus Area |
|---|---|---|
| [Day 7](day7-virtual-threads.md) | Virtual Threads: The Basics | Achieving massive throughput without async callbacks |
| [Day 8](day8-spring-boot-virtual-threads.md) | Migrating to Virtual Threads: Spring Boot 3 Integration | Using Loom in web applications |
| Day 9 | Structured Concurrency: Managing subtasks cleanly | Grouping related threads as a single unit of work |
| Day 10 | Scoped Values: The modern ThreadLocal | Safe and efficient data sharing across threads |
| Day 11 | Advanced Concurrency: CompletableFuture updates & modern locks | Handling complex non-blocking workflows |

**Why this phase matters:** A single Virtual Thread costs ~1KB vs ~1MB for a platform thread. You can create millions of them. This means the traditional tradeoff between simple blocking code and high throughput no longer exists. You get both.

---

## Phase 3: Architecture & Cloud-Native

Java for the cloud era — modules, native compilation, modern testing, and foreign function interfaces.

| Day | Topic | Focus Area |
|---|---|---|
| Day 12 | Java Module System (JPMS): `module-info.java` | Building strongly encapsulated architectures |
| Day 13 | GraalVM & Native Image: AOT Compilation Basics | Compiling Java to fast-booting native binaries |
| Day 14 | Spring Boot 3 + GraalVM: Cloud-Native deployment | Optimizing Spring Boot for Kubernetes/Serverless |
| Day 15 | Modern Testing: JUnit 5 advanced features & Testcontainers | Testing with Docker dependencies |
| Day 16 | Project Panama: Foreign Function & Memory API | Calling C/C++ without JNI (High performance) |
| Day 17 | Vector API: SIMD Operations | Hardware-accelerated math/data processing |

**Why this phase matters:** Startup time, memory footprint, and interop. GraalVM Native Image gives you millisecond startup for serverless. JPMS gives you real encapsulation. Panama replaces JNI with a safe, modern FFI. This is Java competing with Go and Rust on their own terms.

---

## Phase 4: Capstone & Mastery

Tie it all together — performance tuning, observability, migration strategy, and a final project.

| Day | Topic | Focus Area |
|---|---|---|
| Day 18 | Performance Tuning: JVM internals, ZGC, Generational ZGC | Tuning the modern garbage collectors |
| Day 19 | Observability: JFR (JDK Flight Recorder) & Micrometer | Monitoring modern apps in production |
| Day 20 | Refactoring Strategy: Migrating Java 8 to Java 25 | Best practices for modernizing legacy codebases |
| Day 21 | Capstone Project: Modern API Build | A complete Spring Boot 3 app using Records, Loom, and GraalVM |

**Why this phase matters:** Features are useless if you can't deploy, monitor, and migrate to them. Days 18-19 give you production confidence. Day 20 gives you the migration playbook. Day 21 proves it all works together.

---

## How to Use This Course

- Each day has a runnable source file in `src/` and a detailed doc in `docs/`
- Days build on each other within a phase, but phases are loosely independent
- Read the doc, run the code, then modify it. Break things. That's how you learn.
- The source files are self-contained — no build tools needed, just `java DayXFeature.java`

```
java-progressive-2026/
├── docs/           # Daily explanations (you are here)
│   ├── overview.md
│   ├── day1-modern-basics.md
│   ├── day2-records.md
│   └── ...
└── src/            # Runnable examples
    ├── Day1ModernBasics.java
    ├── Day2Records.java
    └── ...
```

## Minimum Java Version

| Phase | Minimum Version | Recommended |
|---|---|---|
| Phase 1 (Days 1-6) | Java 21 | Java 21 |
| Phase 2 (Days 7-11) | Java 21 | Java 24 |
| Phase 3 (Days 12-17) | Java 21 | Java 24 + GraalVM |
| Phase 4 (Days 18-21) | Java 21 | Java 24 + GraalVM |

Java 21 is the baseline (LTS). Java 24 adds Gatherers (Day 5) and other preview features. Use an LTS for production, but use the latest release for learning.
