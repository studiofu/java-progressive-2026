package demos;

import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Day7VirtualThreads {

    public static void main(String[] args) {
        System.out.println("Starting 100,000 Virtual Threads...");
        long startTime = System.currentTimeMillis();

        // We use a try-with-resources block so the executor automatically
        // waits for all submitted tasks to complete before closing.
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            
            IntStream.range(0, 100_000).forEach(i -> {
                executor.submit(() -> {
                    try {
                        // Simulating a blocking I/O operation (like a slow DB query)
                        // In Java 8, this would hold the OS thread hostage.
                        // With Virtual Threads, the JVM immediately frees the OS thread to do other work!
                        Thread.sleep(Duration.ofSeconds(1));
                        System.out.println("Hello from virtual thread #" + i);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            });
            
        } // The main thread blocks here until all 100,000 tasks are done

        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        System.out.println("Successfully completed 100,000 blocking tasks!");
        System.out.println("Total execution time: " + duration + " ms");
    }
}
