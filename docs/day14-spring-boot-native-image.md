# Day 14: Spring Boot 3 & GraalVM AOT

Yesterday you compiled a raw Java file into a blazing-fast native executable. Today we bring that superpower to enterprise web development.

For years, Node.js and Go developers mocked Java for its heavy memory usage and slow startup times in cloud environments. With Spring Boot 3, Java struck back.

---

## The Challenge

GraalVM Native Image and Spring Boot were fundamentally at odds:

- **Spring Boot** relies on reflection and dynamic classpath scanning. At startup, it scans your packages for `@RestController`, `@Service`, `@Component` annotations and creates beans on the fly.
- **GraalVM** hates reflection. It compiles ahead of time and needs to know exactly which classes exist. If it doesn't see a direct code path to a class, it assumes it's unused and deletes it permanently.

Spring's entire architecture was built on the thing GraalVM tries to eliminate.

---

## The Solution: The Spring AOT Engine

Spring Boot 3 solved this with the **AOT (Ahead-of-Time) Engine**. When you build a native image, the process runs in two phases:

```
Phase 1: Spring AOT Processing
  ├── Spring "boots" during the build (not at runtime)
  ├── Scans all your annotations, configurations, and bean definitions
  ├── Evaluates conditional logic (@Conditional, @Profile, etc.)
  ├── Writes reflection hints to META-INF/native-image/.../reflect-config.json
  │
  ▼
Phase 2: GraalVM Compilation
  ├── GraalVM reads your code + Spring's generated config files
  ├── Knows exactly which classes need reflection
  ├── Performs dead code elimination
  ├── Compiles to a native OS binary
```

The key insight: Spring does its dynamic discovery at **build time** instead of runtime, then hands GraalVM a complete roadmap.

---

## Configuring Your Project

Generate a Spring Boot 3 project from [Spring Initializr](https://start.spring.io/) with **"GraalVM Native Support"** selected. This adds the required plugins to your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.graalvm.buildtools</groupId>
            <artifactId>native-maven-plugin</artifactId>
        </plugin>
        <plugin>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-maven-plugin</artifactId>
            <configuration>
                <processAot>true</processAot>
            </configuration>
        </plugin>
    </plugins>
</build>
```

The `processAot` flag tells Spring to run the AOT engine during the build.

---

## Building and Running

Make sure GraalVM is installed and on your path (see Day 13 for setup). Then:

```bash
# Build the native image (takes 2-5 minutes, uses ~100% CPU)
./mvnw -Pnative native:compile

# Run the compiled binary
./target/my-spring-api
```

The `-Pnative` flag activates Spring's GraalVM Maven profile. The build is slow because GraalVM aggressively optimizes and strips unused framework code.

---

## The Results

### Traditional JIT Spring Boot

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Started Application in 3.456 seconds (process running for 3.9)
Memory usage: ~450 MB
```

### Spring Boot 3 Native Image

```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_\__, | / / / /
 =========|_|==============|___/=/_/_/_/

Started Application in 0.041 seconds (process running for 0.045)
Memory usage: ~35 MB
```

| Metric | JIT (JVM) | Native Image |
|---|---|---|
| Startup time | ~3.5 seconds | ~0.04 seconds |
| Memory usage | ~450 MB | ~35 MB |
| Build time | ~5 seconds | ~3 minutes |
| Peak throughput | Higher (JIT optimizes over time) | Lower (no runtime JIT) |

For serverless and container scaling, that 85x faster startup and 12x less memory changes everything.

---

## The Catch: Build-Time Decisions

Because the AOT engine evaluates your application at build time, certain decisions get **locked in**:

### Profiles are baked in

```java
@Profile("dev")
@Configuration
class DevConfig {
    // If you compile with "prod", this class is PERMANENTLY DELETED
}
```

You compile with a specific profile and it cannot be changed at runtime. The "wrong" profile's beans don't exist in the binary.

### Conditional beans are resolved once

```java
@ConditionalOnProperty(name = "feature.enabled", havingValue = "true")
```

This is evaluated at build time. If `feature.enabled=false` during the build, that code is gone.

### What still works at runtime

| Works dynamically at runtime | Locked in at build time |
|---|---|
| `application.properties` / `application.yml` values | `@Profile` bean selection |
| Environment variable overrides | `@Conditional` bean creation |
| Log levels | Component scanning results |

---

## When to Use Native vs JIT

| Scenario | Recommendation |
|---|---|
| Serverless functions (AWS Lambda, Azure Functions) | Native — cold start is everything |
| Kubernetes microservices that scale up/down frequently | Native — faster scaling, less memory |
| Long-running monoliths or batch processors | JIT — the runtime optimizer wins over time |
| Applications using heavy reflection (some legacy libraries) | JIT — avoiding configuration headaches |

---

## Source Code

Today doesn't have a single-file demo — it requires a full Spring Boot Maven/Gradle project. Generate one from [Spring Initializr](https://start.spring.io/) with "GraalVM Native Support" selected to follow along.

---

## TL;DR

| What | One-liner |
|---|---|
| Spring AOT Engine | Evaluates Spring annotations at build time, generates reflection config |
| `./mvnw -Pnative native:compile` | Build a native Spring Boot executable |
| Startup: ~3.5s → ~0.04s | 85x faster startup |
| Memory: ~450MB → ~35MB | 12x less memory |
| Profiles are baked in | `@Profile` decisions are locked at build time, not runtime |
| Best for | Serverless, container microservices, CLI tools |
