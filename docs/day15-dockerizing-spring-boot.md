# Day 15: Dockerizing Modern Spring Boot (No More Dockerfiles)

You now know how to compile a Java application into a raw OS binary. Today we look at how to package and ship it to the cloud.

In the Java 8 era, containerizing a Spring Boot app was a manual chore: write a Dockerfile, pick a heavy base image like `openjdk:8`, copy your 50MB fat JAR, and run it. Change one line of code and Docker had to rebuild and push the entire layer.

Modern Spring Boot replaces all of that with **Cloud-Native Buildpacks**.

---

## Cloud-Native Buildpacks (Paketo)

With Buildpacks, you don't write a Dockerfile. Spring Boot analyzes your application and builds an optimized, OCI-compliant container image for you.

```bash
# That's it. No Dockerfile. No manual layers.
./mvnw spring-boot:build-image
```

The buildpack:
1. Selects a minimal, secure Linux base image
2. Installs the exact JRE version your project needs
3. Extracts your application into intelligent layers
4. Produces a ready-to-run Docker image

---

## Layered Containers: The Secret to Fast Deployments

Instead of one massive JAR, Spring Boot's Buildpacks split your container into distinct layers:

```
┌──────────────────────────────┐
│  Application Code (YOUR code)│  ← changes every deploy (~10 KB)
├──────────────────────────────┤
│  Spring Boot Loader          │  ← changes on Spring version upgrade
├──────────────────────────────┤
│  Dependencies (Spring,       │  ← almost never changes
│  Hibernate, Jackson, etc.)   │     cached permanently by Docker
└──────────────────────────────┘
```

When you change one line of code and rebuild, Docker only uploads the tiny application layer. The dependency layer — the bulk of the image — is already cached in your registry.

| What changed | Layer rebuilt | Upload size |
|---|---|---|
| Fixed a bug in your controller | Application code only | ~10 KB |
| Upgraded Spring Boot version | Loader + dependencies | ~80 MB (rare) |
| Added a new Maven dependency | Dependencies | ~5-20 MB (occasional) |

Compare that to the Java 8 approach where every deploy re-uploaded the entire 50+ MB fat JAR.

---

## Building the Containers

Make sure Docker Desktop (or your Docker engine) is running. Open your terminal in your Spring Boot project directory.

### 1. Standard JVM Container

```bash
./mvnw spring-boot:build-image
```

This builds a layered container running the standard JVM. The result is a Docker image like `my-spring-api:latest` that you can run immediately:

```bash
docker run --rm -p 8080:8080 my-spring-api:latest
```

### 2. Native Image Container (The Ultimate Cloud Setup)

This combines Day 14 (GraalVM AOT) and Day 15 (Buildpacks) into a single command:

```bash
./mvnw spring-boot:build-image -Pnative
```

The buildpack spins up a **temporary builder container** that already has GraalVM and the C++ toolchain installed — you don't need GraalVM on your local machine at all. It compiles your Spring app into a Linux native binary, then packages only that binary into a tiny distroless container.

```bash
docker run --rm -p 8080:8080 my-spring-api:latest
```

---

## The Size Difference

Run `docker images` after both builds:

| Image type | Size | Contents |
|---|---|---|
| **JVM container** | ~250-300 MB | OS + full JVM + layered JAR |
| **Native container** | ~50-80 MB | Minimal OS + native binary only |

That 50-80 MB native container contains a fully functioning web server, database drivers, and your application code. It boots in milliseconds and uses a fraction of the memory.

---

## Real-World Deployment Flow

```bash
# 1. Build the native container image
./mvnw spring-boot:build-image -Pnative

# 2. Tag it for your registry
docker tag my-spring-api:latest registry.example.com/my-spring-api:v1.0

# 3. Push to your container registry
docker push registry.example.com/my-spring-api:v1.0

# 4. Deploy to Kubernetes, Cloud Run, Fargate, etc.
kubectl set image deployment/my-api app=registry.example.com/my-spring-api:v1.0
```

With layered images, step 3 is nearly instant on subsequent deploys — only the changed application layer is pushed.

---

## The Full Picture: Day 13 + 14 + 15

| Day | Topic | What you get |
|---|---|---|
| Day 13 | GraalVM Native Image | `native-image` compiles Java to a native binary |
| Day 14 | Spring Boot 3 + AOT | Spring generates reflection config, builds native at scale |
| Day 15 | Buildpacks + Docker | Package it all into an optimized, layered container |

Together: write standard Spring Boot code, run one command, get a 50MB container that boots in 40ms.

---

## Source Code

Like Day 14, today doesn't have a single-file demo — it requires a full Spring Boot project with Docker. Generate one from [Spring Initializr](https://start.spring.io/) with "GraalVM Native Support" selected and Docker running locally.

---

## TL;DR

| What | One-liner |
|---|---|
| `./mvnw spring-boot:build-image` | Build a layered JVM container — no Dockerfile needed |
| `./mvnw spring-boot:build-image -Pnative` | Build a native image container — GraalVM runs inside Docker |
| Layered JARs | Only your changed code is re-uploaded (~10 KB per deploy) |
| JVM container size | ~250-300 MB |
| Native container size | ~50-80 MB |
| No local GraalVM needed | The buildpack uses its own builder container |
