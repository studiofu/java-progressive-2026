import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class Day8SpringBootSimulator {

    public static void main(String[] args) {
        int totalRequests = 1000;

        System.out.println("--- Simulating Old Spring Boot (Platform Threads) ---");
        // Traditional Tomcat defaults to a pool of 200 heavy OS threads
        try (ExecutorService oldTomcatPool = Executors.newFixedThreadPool(200)) {
            runWebSimulator(oldTomcatPool, totalRequests, "Old Tomcat");
        }

        System.out.println("\n--- Simulating Modern Spring Boot 3 (Virtual Threads) ---");
        // Modern Spring Boot creates a cheap Virtual Thread for EVERY single request
        try (ExecutorService modernTomcatPool = Executors.newVirtualThreadPerTaskExecutor()) {
            runWebSimulator(modernTomcatPool, totalRequests, "Modern Tomcat");
        }
    }

    private static void runWebSimulator(ExecutorService serverPool, int requests, String serverName) {
        long startTime = System.currentTimeMillis();

        IntStream.range(0, requests).forEach(i -> {
            serverPool.submit(() -> {
                try {
                    // Simulate a controller making a 50-millisecond database query
                    Thread.sleep(Duration.ofMillis(50));
                    
                    // Print out the thread type for the very first request only to prove what's running
                    if (i == 0) {
                        System.out.println("Request handled by: " + Thread.currentThread());
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });

        // The try-with-resources block ensures we wait for all tasks to finish here
        serverPool.close(); 
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println(serverName + " handled " + requests + " requests in " + duration + " ms");


        IntStream.range(0,10).forEach(i -> {
            System.out.println("Hello from range #" + i);
        });

        IntStream.rangeClosed(0,10).forEach(i -> {
            System.out.println("Hello from rangeClosed #" + i);
        });

        IntStream.of(99,100,101).average().ifPresent((avg) -> {
            System.out.println("Average: " + avg);
        });



    }
}
