# Day 7: Virtual Threads — The Basics

Welcome to Phase 2: Next-Gen Concurrency. Today we're tackling what is arguably the most significant architectural revolution in Java since its inception: **Project Loom** and **Virtual Threads**.

Introduced fully in Java 21, Virtual Threads solve the scalability problem at the language level.

---

## The Problem with Java 8 Threads

In Java 8, threads were a 1:1 wrapper around Operating System (OS) threads:

- **Heavy:** Each OS thread consumes ~1MB of memory just to exist
- **Expensive to switch:** Context switching between OS threads takes a toll on the CPU
- **Limited:** Create 10,000 threads and your server likely crashes with `OutOfMemoryError`

To work around this, developers were forced into complex, hard-to-debug async frameworks (Spring WebFlux, CompletableFuture chains, reactive streams) just to scale. You traded readable code for throughput.

### The Java 8 concurrency dilemma

```
Simple blocking code  →  Doesn't scale
Reactive async code   →  Scales, but hard to write/debug
```

Virtual Threads eliminate this tradeoff entirely.

---

## Key Concepts

### Lightweight

Virtual threads are managed entirely by the JVM, not the OS. They consume a fraction of a kilobyte instead of ~1MB. You can create **millions** of them simultaneously.

### M:N Scheduling

The JVM maps millions of Virtual Threads (**M**) to a small pool of Carrier OS Threads (**N**). You don't manage this mapping — it happens automatically.

```
┌─────────────────────────────────────────────┐
│  Virtual Threads (millions)                  │
│  VT-1  VT-2  VT-3  ...  VT-1000000         │
│    │     │     │           │                 │
│    ▼     ▼     ▼           ▼                 │
│  ┌─────────────────────────────┐             │
│  │  JVM Scheduler (ForkJoinPool)│            │
│  └─────────────────────────────┘             │
│    │     │     │                              │
│    ▼     ▼     ▼                              │
│  Carrier OS Threads (few, e.g. CPU cores)    │
│  CT-1  CT-2  CT-3  CT-4                     │
└─────────────────────────────────────────────┘
```

### Write Sync, Run Async

This is the magic. When your code **blocks** — `Thread.sleep()`, a database call, an HTTP request — the JVM automatically **unmounts** the Virtual Thread from the OS thread, freeing that OS thread to run other Virtual Threads. When the I/O completes, the Virtual Thread **remounts** and picks up right where it left off.

You write simple, blocking, step-by-step code — and it scales as well as reactive.

---

## Creating Virtual Threads

### Method 1: `Thread.startVirtualThread()`

```java
Thread vt = Thread.startVirtualThread(() -> {
    System.out.println("Hello from a virtual thread!");
});
```

### Method 2: `Thread.ofVirtual()` builder

```java
Thread vt = Thread.ofVirtual()
        .name("my-vthread")
        .start(() -> System.out.println("Hello!"));
```

### Method 3: `Executors.newVirtualThreadPerTaskExecutor()` (recommended for batches)

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 100_000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return "Task " + i + " done";
        });
    });
} // blocks until all tasks complete
```

Each `submit()` creates a new Virtual Thread. The try-with-resources ensures the main thread waits for all tasks to finish before proceeding.

---

## The Demo: 100,000 Concurrent Tasks

```java
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 100_000).forEach(i -> {
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));  // blocks — but frees the OS thread!
            System.out.println("Hello from virtual thread #" + i);
        });
    });
}
```

What happens:
1. 100,000 Virtual Threads are created (total memory: ~100MB — trivial)
2. Each one calls `Thread.sleep(1 second)` — a blocking operation
3. When a VT blocks, the JVM unmounts it from its carrier thread
4. That carrier thread immediately picks up another VT
5. After 1 second, all VTs wake up, remount, and print their messages
6. Total wall-clock time: ~1 second (not 100,000 seconds)

With Java 8 platform threads, this would require ~100GB of memory just for thread stacks, and would crash long before reaching 100,000.

---

## Virtual Threads vs Platform Threads

| Property | Platform Thread | Virtual Thread |
|---|---|---|
| Memory per thread | ~1MB (stack) | ~a few KB |
| Managed by | OS | JVM |
| Max practical count | ~thousands | ~millions |
| Blocking cost | Holds OS thread hostage | Unmounts, frees OS thread |
| Creation time | Expensive | Cheap |
| Best for | CPU-bound work | I/O-bound work (HTTP, DB, file) |

---

## What to Keep in Mind

### Virtual Threads are for I/O-bound work

Database calls, HTTP requests, file reads, `Thread.sleep()` — anything where the thread sits waiting. The JVM is optimized to unmount VTs during these operations.

### Don't pool Virtual Threads

Platform threads are expensive, so we pool them (thread pools). Virtual threads are cheap — create a new one for each task. The `newVirtualThreadPerTaskExecutor()` does exactly this. Never put VTs in a fixed-size pool.

### CPU-bound work still uses platform threads

If your task is crunching numbers (encryption, compression, heavy computation), a Virtual Thread offers no benefit — it will just hold onto a carrier thread. Use platform threads or parallel streams for CPU-bound work.

### `synchronized` pins the carrier

Using `synchronized` blocks inside a Virtual Thread **pins** it to the carrier thread, preventing unmounting. Use `ReentrantLock` instead if you need locking inside a VT:

```java
// Avoid inside virtual threads
synchronized (lock) { ... }

// Preferred
lock.lock();
try { ... } finally { lock.unlock(); }
```

---

## Source Code

The runnable example for today is at `src/Day7VirtualThreads.java`.

---

## TL;DR

| Concept | One-liner |
|---|---|
| Lightweight | ~KB per thread, not ~MB |
| M:N scheduling | JVM maps millions of VTs to a few OS threads |
| Write sync, run async | Blocking I/O automatically frees the OS thread |
| `Executors.newVirtualThreadPerTaskExecutor()` | The easiest way to launch many VTs |
| Don't pool them | Create a new VT per task — they're cheap |
| Use `ReentrantLock`, not `synchronized` | Avoids carrier thread pinning |
