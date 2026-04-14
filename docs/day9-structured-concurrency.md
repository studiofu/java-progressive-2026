# Day 9: Structured Concurrency

Yesterday you saw how Virtual Threads make concurrency cheap and fast. But cheap threads create a new problem: **how do you manage them safely?**

In Java 8, concurrency was "unstructured." If you fired off three threads to fetch user data, order history, and payment details, those threads lived entirely independent lives. If the user data thread threw an exception, the order and payment threads kept running in the background, wasting CPU and network resources. Canceling those abandoned threads manually required complex `CompletableFuture` chaining or messy try-catch-finally blocks.

Structured Concurrency fixes this.

---

## The Core Idea

Structured Concurrency treats multiple concurrent tasks as a **single unit of work**. It guarantees that if a block of code splits into multiple threads, all of those threads will be finished (or canceled) by the time the block exits. No more thread leaks. No more orphaned background work.

---

## Key Tool: `StructuredTaskScope`

This class provides built-in policies for common concurrency scenarios:

| Policy | When to use | Behavior |
|---|---|---|
| **ShutdownOnFailure** | You need **all** tasks to succeed | If any task fails, instantly cancels all other running tasks |
| **ShutdownOnSuccess** | You only need the **first** success | As soon as one task returns a result, cancels the rest |

In Java 25, `StructuredTaskScope.open()` defaults to the ShutdownOnFailure policy — the most common case.

---

## The Problem It Solves

### Unstructured (Java 8 style)

```java
ExecutorService executor = Executors.newCachedThreadPool();
Future<String> userFuture = executor.submit(() -> fetchUser());
Future<String> orderFuture = executor.submit(() -> fetchOrders());
Future<String> paymentFuture = executor.submit(() -> fetchPayment());

// If fetchUser() throws here...
String user = userFuture.get();   // <-- exception!
// ...orderFuture and paymentFuture are STILL RUNNING in the background
// You have to manually cancel them:
orderFuture.cancel(true);
paymentFuture.cancel(true);
```

Every call site needs its own cancellation logic. Forget it once, and you leak threads.

### Structured (Java 25)

```java
try (var scope = StructuredTaskScope.open()) {
    var userTask = scope.fork(this::fetchUser);
    var orderTask = scope.fork(this::fetchOrders);

    scope.join(); // waits for all; auto-cancels others on failure

    System.out.println(userTask.get());
    System.out.println(orderTask.get());
}
```

If `fetchUser()` throws, `join()` detects the failure and cancels `fetchOrders()` automatically. When the try-with-resources block exits, the scope guarantees no threads are left running.

---

## The Demo

The example at `src/Day9StructuredConcurrency.java` simulates a dashboard that fetches user data and order history concurrently.

### What happens

1. Two subtasks are forked: `fetchUser()` and `fetchOrders()`
2. `fetchUser()` fails after 500ms (simulated API outage)
3. `fetchOrders()` would take 2 seconds — but it gets **cancelled** the moment `fetchUser()` fails
4. `join()` throws because a subtask failed
5. The scope closes cleanly — no leaked threads

### Expected output

```
Starting concurrent data fetch...

-> [User API] Started fetching user...
-> [Order API] Started fetching orders...
-> [Order API] Task was CANCELLED to save resources!
Dashboard fetch failed: null

Total time taken: ~500 ms
```

Notice the total time is only ~500ms, not 2000ms. The Order API was cancelled early, saving both time and resources.

---

## ShutdownOnSuccess: Racing for the First Result

The default `open()` gives you ShutdownOnFailure. If you want the opposite — take the first success and cancel the rest — use a custom policy:

```java
try (var scope = StructuredTaskScope.open(StructuredTaskScope.Policy.shutdownOnSuccess())) {
    scope.fork(() -> pingServer("server-1"));
    scope.fork(() -> pingServer("server-2"));
    scope.fork(() -> pingServer("server-3"));

    scope.join();

    // Returns the result of whichever server responded first
    String fastest = scope.result();
}
```

Use cases:
- Pinging redundant servers and taking the fastest response
- Querying multiple mirror databases and using whichever returns first
- Racing multiple AI model endpoints

---

## Why This Matters

| Problem | Before (Java 8) | After (Java 25) |
|---|---|---|
| Task fails, others keep running | Manual `Future.cancel()` calls | Automatic cancellation via scope |
| Thread leaks | Common, hard to detect | Impossible — scope guarantees cleanup |
| Error propagation | Easy to swallow exceptions | `join()` throws on any failure |
| Code complexity | `CompletableFuture` chains | Simple, linear try-with-resources |

---

## How to Run

```bash
java --enable-preview --source 25 src/Day9StructuredConcurrency.java
```

---

## Source Code

The runnable example for today is at `src/Day9StructuredConcurrency.java`.

---

## TL;DR

| What | One-liner |
|---|---|
| `StructuredTaskScope.open()` | Create a scope with default ShutdownOnFailure policy |
| `scope.fork(task)` | Start a concurrent subtask on a Virtual Thread |
| `scope.join()` | Wait for all tasks; auto-cancel on failure |
| ShutdownOnFailure | Cancel everything if any task fails |
| ShutdownOnSuccess | Cancel everything once one task succeeds |
| Try-with-resources | Guarantees no thread leaks on scope exit |
