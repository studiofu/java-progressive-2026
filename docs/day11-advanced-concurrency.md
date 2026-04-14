# Day 11: CompletableFuture Updates & Modern Locks

We're wrapping up Phase 2: Next-Gen Concurrency. You might be wondering: *if Virtual Threads are so amazing, why do we still care about CompletableFuture or explicit locks?*

Great question. While Virtual Threads and Structured Concurrency are the future, you'll still encounter three scenarios where these tools matter:

1. **Interfacing with legacy async code** — many libraries still return `CompletableFuture`
2. **Granular timeouts** — you want a specific fallback value if an operation doesn't complete within exactly 50ms
3. **High-performance read-heavy data** — thousands of threads reading a shared in-memory cache need modern locking to prevent CPU bottlenecks

---

## Part 1: Modern CompletableFuture Timeouts

### The Java 8 Nightmare

Adding a timeout to a `CompletableFuture` in Java 8 required a custom `ScheduledExecutorService`, a fallback future, and manual `acceptEither()` chaining. It was verbose and error-prone.

### Java 9+: `.orTimeout()` and `.completeOnTimeout()`

Two methods replaced all of that boilerplate:

```java
// Fail with a TimeoutException if it takes longer than 1 second
future.orTimeout(1, TimeUnit.SECONDS);

// Return a default value instead of failing
future.completeOnTimeout("DEFAULT_CACHED_VALUE", 1, TimeUnit.SECONDS);
```

| Method | Behavior | Use when |
|---|---|---|
| `.orTimeout(duration, unit)` | Throws `TimeoutException` if not done in time | The operation must succeed — failure is an error |
| `.completeOnTimeout(value, duration, unit)` | Returns the fallback value if not done in time | You have a safe default and want graceful degradation |

### The Demo

The example in `src/Day11AdvancedConcurrency.java` shows both scenarios:

**Scenario A — Strict timeout:**

```java
CompletableFuture<String> strictTask = CompletableFuture.supplyAsync(() -> slowDatabaseCall(2000))
        .orTimeout(1, TimeUnit.SECONDS);

strictTask.join(); // throws because the DB call takes 2s but timeout is 1s
```

**Scenario B — Fallback timeout:**

```java
CompletableFuture<String> fallbackTask = CompletableFuture.supplyAsync(() -> slowDatabaseCall(2000))
        .completeOnTimeout("DEFAULT_CACHED_VALUE", 1, TimeUnit.SECONDS);

fallbackTask.join(); // returns "DEFAULT_CACHED_VALUE" after 1 second
```

### Expected output

```
--- 1. Modern CompletableFuture Timeouts ---
Fetching data with 1-second strict timeout...
Task timed out safely! Error: TimeoutException

Fetching data with 1-second fallback timeout...
Result: DEFAULT_CACHED_VALUE
```

---

## Part 2: StampedLock (Optimistic Reading)

### The Problem with ReadWriteLock

`ReentrantReadWriteLock` from Java 5 forces every read to acquire a lock, even when no writes are happening. For read-heavy workloads (caches, configuration maps, metric counters), this creates unnecessary contention — threads line up for a lock they don't really need.

### The StampedLock Solution

`StampedLock` introduces **optimistic reads**. The idea:

1. Get a "stamp" (a version number) — this does **not** block other threads
2. Read the data
3. Check if the stamp is still valid (no writes happened during the read)
4. If valid: you're done — zero contention
5. If invalid: upgrade to a full read lock and try again

In read-heavy workloads, step 4 succeeds the vast majority of the time. Threads almost never wait.

### The Code

```java
StampedLock lock = new StampedLock();
double sharedBalance = 1000.0;

// Optimistic read — no blocking!
long stamp = lock.tryOptimisticRead();
double balance = sharedBalance;             // read the data

// ... some processing happens ...

if (lock.validate(stamp)) {
    // Stamp still valid — the read is correct, no lock was needed
} else {
    // Someone wrote while we were reading — fall back to a real read lock
    stamp = lock.readLock();
    try {
        balance = sharedBalance;
    } finally {
        lock.unlockRead(stamp);
    }
}
```

### Why It's Fast

| Scenario | ReentrantReadWriteLock | StampedLock (optimistic) |
|---|---|---|
| 100 threads reading, 0 writing | All 100 acquire read lock (coordination overhead) | All 100 read lock-free, validate stamp (near-zero overhead) |
| 100 threads reading, 1 writing | Readers block behind the writer | Writer gets a write lock; readers detect the change and retry |

For the common case — many reads, few writes — optimistic reads eliminate almost all lock contention.

### The Demo

The example simulates a concurrent write happening during an optimistic read:

1. Thread acquires an optimistic read stamp
2. A writer thread updates `sharedBalance` from 1000.0 to 1500.0
3. The optimistic read stamp is validated — it fails because a write occurred
4. The code falls back to a full read lock and reads the correct value

### Expected output

```
--- 2. StampedLock (Optimistic Reading) ---
[Writer] Added $500. New balance: 1500.0
Data changed while reading! Upgrading to a full read lock...
Final Read Balance: 1500.0
```

---

## How to Run

```bash
java --enable-preview --source 25 src/Day11AdvancedConcurrency.java
```

---

## Source Code

The runnable example for today is at `src/Day11AdvancedConcurrency.java`.

---

## TL;DR

| What | One-liner |
|---|---|
| `.orTimeout(1, TimeUnit.SECONDS)` | Throw `TimeoutException` if not done in time |
| `.completeOnTimeout(value, 1, TimeUnit.SECONDS)` | Return a fallback value if not done in time |
| `StampedLock.tryOptimisticRead()` | Read without locking — validate afterward |
| `lock.validate(stamp)` | Check if the read is still valid |
| `lock.readLock()` | Fall back to a blocking read if optimistic read fails |
| Optimistic reads | Near-zero overhead for read-heavy workloads |
