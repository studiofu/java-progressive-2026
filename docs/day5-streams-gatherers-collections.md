# Day 5: Stream API, Gatherers, & Collections Updates

This is where you'll really feel the day-to-day coding experience improve. The Stream API and standard Collections from Java 8 were revolutionary, but they still had rough edges. Grouping elements in a stream was notoriously hard, getting the last element of a `List` was clunky (`list.get(list.size() - 1)`), and collecting a stream to a `List` required `Collectors.toList()`.

Modern Java fixes all of this.

---

## 1. Sequenced Collections — Java 21

Java finally introduced standard interfaces for collections that have a defined encounter order. This gives us `.getFirst()`, `.getLast()`, and `.reversed()` natively on `List`, `Deque`, and `LinkedHashSet`.

### The Java 8 way (painful)

```java
List<String> devs = List.of("Alice", "Bob", "Charlie", "Diana", "Eve");

// First element
devs.get(0);                          // works for List, but not Set or Deque

// Last element
devs.get(devs.size() - 1);            // ugly, error-prone, doesn't work on Set

// Reverse
List<String> reversed = new ArrayList<>(devs);
Collections.reverse(reversed);         // mutates in place, 3 lines
```

Every collection type had a different way to access its first and last element. There was no common interface.

### The modern way

```java
devs.getFirst();      // "Alice"
devs.getLast();       // "Eve"
devs.reversed();      // [Eve, Diana, Charlie, Bob, Alice]
```

Three new methods, available on any `SequencedCollection`. `.reversed()` returns a **view** — it doesn't copy or mutate the original.

### The new hierarchy

```
Collection
  └── SequencedCollection
        ├── List
        ├── Deque
        └── SequencedSet
              └── LinkedHashSet
```

`Set` and `HashSet` are **not** sequenced — they have no defined order. But `LinkedHashSet` is, because it maintains insertion order.

---

## 2. Stream `.toList()` — Java 16

### The Java 8 way (verbose)

```java
import java.util.stream.Collectors;

List<String> upper = developers.stream()
        .map(String::toUpperCase)
        .collect(Collectors.toList());
```

That `.collect(Collectors.toList())` appeared in virtually every stream pipeline. It was noise.

### The modern way

```java
List<String> upper = developers.stream()
        .map(String::toUpperCase)
        .toList();
```

One method call. Clean. But there's a catch:

### `.toList()` vs `.collect(Collectors.toList())`

| Behavior | `.toList()` | `.collect(Collectors.toList())` |
|---|---|---|
| Mutability | **Unmodifiable** | **Mutable** |
| Null elements | Throws `NullPointerException` | Allows `null` |
| Use case | Read-only results | When you need to modify after |

You can't add or remove from the list returned by `.toList()`. For most stream pipelines that produce a final result, this is exactly what you want. If you need a mutable list, stick with the collector or wrap it: `new ArrayList<>(stream.toList())`.

---

## 3. Stream Gatherers — Java 24+

This is a massive upgrade to the Stream API. It allows for **custom intermediate operations** — things that go between `.stream()` and `.toList()` that the built-in operations couldn't handle cleanly.

### The problem

Grouping elements into chunks was extremely painful in Java 8 Streams. You had to either:

- Use a messy `Collectors.groupingBy` with index math
- Drop to a manual `for` loop, losing the stream pipeline
- Write a custom `Collector` (complex, verbose)

### The modern way: built-in Gatherers

```java
import java.util.stream.Gatherers;

List<String> developers = List.of("Alice", "Bob", "Charlie", "Diana", "Eve");

List<List<String>> pairs = developers.stream()
        .gather(Gatherers.windowFixed(2))
        .toList();
// [[Alice, Bob], [Charlie, Diana], [Eve]]
```

`.gather()` is the new intermediate operation method. `Gatherers.windowFixed(2)` groups every 2 elements into a sub-list. If the total isn't evenly divisible, the last window is smaller.

### Built-in gatherers

| Gatherer | What it does |
|---|---|
| `Gatherers.windowFixed(n)` | Groups elements into fixed-size windows |
| `Gatherers.windowSliding(n)` | Sliding windows that overlap by `n-1` elements |
| `Gatherers.fold(initial, merger)` | Accumulates all elements into a single result (like a mutable reduce) |
| `Gatherers.scan(initial, scanner)` | Running accumulation — produces intermediate results |

### Sliding window example

```java
List<List<Integer>> sliding = Stream.of(1, 2, 3, 4, 5)
        .gather(Gatherers.windowSliding(3))
        .toList();
// [[1, 2, 3], [2, 3, 4], [3, 4, 5]]
```

This was near-impossible with the Java 8 Stream API without writing significant boilerplate.

### Custom gatherers

You can also write your own gatherer for domain-specific operations:

```java
Gatherer<String, ?, String> dedupe = Gatherer.of(
    // initializer, integrator, combiner, finisher
    // ... custom logic here
);

stream.gather(dedupe).toList();
```

Custom gatherers fill the gap between what `Stream` provides built-in (`map`, `filter`, `flatMap`) and what previously required dropping out of the stream pipeline entirely.

---

## At a Glance

| Feature | Java Version | Replaces |
|---|---|---|
| `SequencedCollection` | Java 21 | `get(0)`, `get(size()-1)`, manual reverse |
| `.toList()` | Java 16 | `.collect(Collectors.toList())` |
| `Gatherers.windowFixed()` | Java 24 | Custom grouping collectors / for-loops |
| `Gatherers.windowSliding()` | Java 24 | Moving averages, overlapping windows |
| Custom `Gatherer` | Java 24 | Dropping out of stream pipelines |

---

## Source Code

The runnable example for today is at `src/Day5ApiEnhancements.java`.

---

## TL;DR

| Feature | One-liner |
|---|---|
| `list.getFirst()` / `.getLast()` | First and last element without index math |
| `list.reversed()` | Reversed view, no mutation |
| `stream.toList()` | Collect to unmodifiable list in one call |
| `stream.gather(Gatherers.windowFixed(n))` | Group elements into fixed-size chunks |
