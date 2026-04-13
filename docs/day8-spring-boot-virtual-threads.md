# Day 8: Spring Boot 3 + Virtual Threads

Yesterday you saw the raw power of Virtual Threads. Today we're looking at how this practically impacts your day-to-day job: **migrating to Virtual Threads in Spring Boot 3**.

In the Java 8 era, if your Spring Boot app needed to handle a massive spike in traffic, Tomcat would quickly run out of its default 200 platform threads. You either had to scale horizontally (expensive) or rewrite everything with Spring WebFlux and reactive programming (steep learning curve, hard to debug).

As of Spring Boot 3.2, you get the throughput of reactive while keeping the simple blocking code you're used to.

---

## The One-Line Migration

In a Spring Boot 3 application, you don't need to rewrite your controllers or services. Add one line to `application.properties`:

```properties
spring.threads.virtual.enabled=true
```

Or in YAML:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

That's it. Once you flip that switch:

| Component | What changes |
|---|---|
| **Tomcat request handling** | Every incoming HTTP request is assigned to a Virtual Thread instead of a platform thread |
| **`@Async` methods** | Uses Virtual Threads automatically |
| **`@Scheduled` tasks** | Leverages Virtual Threads |
| **`RestTemplate` / `RestClient`** | Blocking HTTP calls no longer waste OS threads |

No code changes. No reactive rewrite. Just one property.

---

## What Spring Boot Does Internally

### Before (Java 8 / Spring Boot 2)

```
Incoming HTTP request → Tomcat assigns a platform thread from a 200-thread pool
                     → Controller calls the database (thread BLOCKS for ~50ms)
                     → That OS thread sits idle, doing nothing, waiting
                     → 201st concurrent request? Rejected or queued.
```

With 200 threads each blocking for 50ms on a database call, you max out at ~4,000 requests/second — and that's assuming perfect pipelining.

### After (Spring Boot 3.2+ with Virtual Threads)

```
Incoming HTTP request → Tomcat creates a NEW Virtual Thread (cheap!)
                     → Controller calls the database (thread blocks)
                     → JVM UNMOUNTS the VT, OS thread goes to serve another request
                     → Database responds → VT remounts, response sent
                     → 1,001st concurrent request? No problem. 10,001st? Still fine.
```

Same controller code. Same blocking style. Orders of magnitude more throughput.

---

## The Simulator

Since this course uses single-file standalone programs, we can't boot a full Spring application. Instead, the demo simulates exactly what Spring Boot's Tomcat does internally — 1,000 concurrent HTTP requests, comparing the old and new approaches.

### Old Tomcat (200 platform threads)

```java
try (ExecutorService oldTomcatPool = Executors.newFixedThreadPool(200)) {
    runWebSimulator(oldTomcatPool, 1000, "Old Tomcat");
}
```

200 threads. 1,000 requests. Each request blocks for 50ms (simulating a DB query). The pool can only process 200 at a time — the rest queue up.

### Modern Tomcat (Virtual Threads)

```java
try (ExecutorService modernTomcatPool = Executors.newVirtualThreadPerTaskExecutor()) {
    runWebSimulator(modernTomcatPool, 1000, "Modern Tomcat");
}
```

One Virtual Thread per request. All 1,000 launch concurrently. When each one blocks on `Thread.sleep()`, the JVM frees the carrier thread. All 1,000 complete in roughly 50ms.

### Expected output

```
--- Simulating Old Spring Boot (Platform Threads) ---
Request handled by: Thread[pool-1-thread-1,5,main]
Old Tomcat handled 1000 requests in ~300 ms     ← 5 batches of 200

--- Simulating Modern Spring Boot 3 (Virtual Threads) ---
Request handled by: Thread[],5,main]             ← note: no pool name, it's virtual
Modern Tomcat handled 1000 requests in ~60 ms    ← all 1000 run concurrently
```

The exact times vary, but the Virtual Thread version is consistently faster because it doesn't serialize requests through a small pool.

---

## Real-World Migration Checklist

### Prerequisites

- Spring Boot 3.2+ (3.3+ recommended)
- Java 21+ (required for Virtual Threads)
- Tomcat (default), Jetty, or Undertow as the embedded server

### Step 1: Enable the property

```properties
spring.threads.virtual.enabled=true
```

### Step 2: Check for `synchronized` blocks

Virtual Threads **pin** to carrier threads inside `synchronized` blocks. Search your codebase:

```bash
grep -r "synchronized" src/
```

Replace with `ReentrantLock` where found:

```java
// Before — pins carrier thread
synchronized (this) {
    sharedState.update();
}

// After — VT-friendly
private final ReentrantLock lock = new ReentrantLock();

lock.lock();
try {
    sharedState.update();
} finally {
    lock.unlock();
}
```

### Step 3: Check for thread-local overuse

If you're storing large objects in `ThreadLocal`, remember that Virtual Threads are created per-request now, not pooled. Each VT gets its own `ThreadLocal` copy. For most cases this is fine, but if you're caching expensive objects in `ThreadLocal` as a "pool optimization," it becomes wasteful. (Day 10 covers the modern replacement: Scoped Values.)

### Step 4: Remove custom thread pool sizing

If you tuned Tomcat's thread pool (`server.tomcat.threads.max=400`), you no longer need to. Virtual Threads make pool sizing irrelevant — each request gets its own thread.

### Step 5: Test and deploy

Your controller and service code doesn't change. Run your existing tests. Deploy.

---

## When You Still Need Reactive

Virtual Threads are not a silver bullet for everything:

| Scenario | Recommendation |
|---|---|
| Typical CRUD / REST APIs | Virtual Threads — perfect fit |
| Database-heavy workloads | Virtual Threads — blocking I/O is the sweet spot |
| Chat / WebSocket servers | WebFlux / reactive still has an edge |
| Streaming large responses (SSE) | WebFlux can be more memory-efficient |
| Pure CPU-bound processing | Use parallel streams, not VTs |

For the vast majority of Spring Boot applications — REST APIs talking to databases and other services — Virtual Threads are the right choice.

---

## Source Code

The runnable example for today is at `src/Day8SpringBootSimulator.java`.

---

## TL;DR

| What | One-liner |
|---|---|
| `spring.threads.virtual.enabled=true` | One property to enable VTs in Spring Boot 3.2+ |
| No code changes | Controllers and services stay exactly the same |
| Replace `synchronized` with `ReentrantLock` | Avoids carrier thread pinning |
| Don't tune thread pools | VTs make pool sizing irrelevant |
| Best for I/O-bound REST APIs | CRUD apps get the biggest win |
