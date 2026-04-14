# Day 16: Unified Observability & OpenTelemetry

You've built a blazing-fast native application and packaged it into a microscopic Docker container. But once you deploy it to Kubernetes, a new problem emerges: **how do you know what it's doing?**

If a user complains that "the checkout button is slow," how do you figure out if the problem is the Java app, the database, or the payment gateway API?

---

## The Problem: Fragmented Monitoring in Java 8

In the Spring Boot 2 era, observability was cobbled together from multiple tools:

| Signal | Tool | Export format |
|---|---|---|
| Distributed tracing | Spring Cloud Sleuth | Zipkin / Brave format |
| Metrics | Micrometer | Prometheus format |
| Logs | Logback | Plain text |
| Correlation | Manual MDC entries | None |

They rarely talked to each other. Traces, metrics, and logs lived in separate systems with no common identifiers. And if you wanted a production-grade setup, you had to attach heavy Java agents to your application.

---

## The Solution: Micrometer Observation API + OpenTelemetry

In Spring Boot 3, Spring Cloud Sleuth was **completely removed**. Instead, Spring adopted the Micrometer Observation API with native OpenTelemetry support — the industry standard for cloud monitoring.

The concept: **write once, observe everywhere.** Wrap a piece of code in a single "Observation," and Spring Boot automatically generates three signals:

```
Observation.createNotStarted("order.processing", registry)
        .lowCardinalityKeyValue("order.type", "premium")
        .observe(() -> {
            // your business logic
        });
```

That one wrapper produces:

| Signal | What you get | Where it goes |
|---|---|---|
| **Metric** | Count, timing percentiles (p50, p95, p99) | Prometheus → Grafana dashboards |
| **Trace** | Span with start/stop time, parent/child links | Jaeger / Zipkin / Tempo |
| **Log context** | Auto-injected `traceId` and `spanId` in every log line | ELK / Loki / CloudWatch Logs |

One observation. Three signals. Zero extra code.

---

## The Demo: Two Ways to Instrument

The full Spring Boot project is at `springboot/day16/`. It shows two approaches to instrumenting code.

### Approach 1: Programmatic Observation

File: `Day16OrderService.java`

```java
@Service
public class Day16OrderService {

    private final ObservationRegistry observationRegistry;

    public Day16OrderService(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public String placeOrder(String orderId) {
        return Observation.createNotStarted("order.processing", observationRegistry)
                .lowCardinalityKeyValue("order.type", "premium_user")
                .observe(() -> {
                    simulateDatabaseCall();
                    System.out.println("Order " + orderId + " successfully processed.");
                    return "Success: " + orderId;
                });
    }
}
```

- `Observation.createNotStarted()` — creates a named observation
- `.lowCardinalityKeyValue()` — adds tags for filtering dashboards (e.g., group by `order.type`)
- `.observe()` — starts the timer, runs the code, records errors, stops the timer automatically

### Approach 2: Declarative `@Observed` Annotation

File: `DemoApplication.java`

```java
@Observed(name = "order.processing", contextualName = "process-premium-order")
public String placeOrder(String orderId) {
    // your business logic
}
```

No manual registry code. The annotation does everything for you.

**Critical:** The `@Observed` annotation requires the `ObservedAspect` bean, or it will be silently ignored:

```java
@Bean
public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
    return new ObservedAspect(observationRegistry);
}
```

### The Trigger: REST Endpoint

```java
@RestController
class OrderController {
    @GetMapping("/buy")
    public String buy() {
        return orderService.placeOrder("ORD-999");
    }
}
```

---

## Required Dependencies

The `pom.xml` includes:

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-actuator</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-aop</artifactId>
    </dependency>
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjweaver</artifactId>
    </dependency>
</dependencies>
```

And `application.properties` exposes the actuator endpoints:

```properties
spring.application.name=demo
management.endpoints.web.exposure.include=*
```

---

## How to Run

```bash
cd springboot/day16
./mvnw spring-boot:run
```

Then trigger the endpoint:

```bash
curl http://localhost:8080/buy
```

### Check the metrics

```bash
curl http://localhost:8080/actuator/metrics/order.processing
```

You'll see count, total time, and percentile data for the `order.processing` observation — all generated from that single wrapper.

---

## What Happens with OpenTelemetry Connected

Once you add an OpenTelemetry exporter (e.g., `opentelemetry-exporter-otlp` dependency), the same `order.processing` observation automatically flows to your observability platform:

```
User clicks "Buy"
  → HTTP GET /buy                    [span: http GET]
    → OrderService.placeOrder()      [span: order.processing, parent: http GET]
      → simulateDatabaseCall()       [span: jdbc query, parent: order.processing]
    ← returns "Success: ORD-999"     [span ends]
  ← HTTP 200                         [span ends]
```

Every span carries the same `traceId`. In Jaeger or Grafana Tempo, you see the full request journey visually — no manual correlation needed.

---

## Java 8 vs Java 25 Observability

| Aspect | Java 8 / Spring Boot 2 | Java 25 / Spring Boot 3 |
|---|---|---|
| Tracing library | Spring Cloud Sleuth (removed in 3.x) | Micrometer Observation + OpenTelemetry |
| Metrics + Tracing | Separate code paths | Single `Observation` produces both |
| Log correlation | Manual MDC setup | Automatic `traceId`/`spanId` injection |
| Standard | Proprietary formats | OpenTelemetry (industry standard) |
| Java agents | Required for deep instrumentation | Built into the framework |

---

## Source Code

The full Spring Boot project for today is at `springboot/day16/`.

---

## TL;DR

| What | One-liner |
|---|---|
| `Observation.createNotStarted("name", registry).observe(() -> { ... })` | Wrap code once — get metrics, traces, and log correlation automatically |
| `@Observed(name = "order.processing")` | Declarative annotation alternative (requires `ObservedAspect` bean) |
| `.lowCardinalityKeyValue("key", "value")` | Add tags for filtering dashboards |
| `spring-boot-starter-actuator` | Exposes metrics at `/actuator/metrics/` |
| OpenTelemetry | Industry standard — one observation exports to Prometheus, Jaeger, and logs |
