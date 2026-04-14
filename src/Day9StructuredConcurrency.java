import java.util.concurrent.StructuredTaskScope;
import java.time.Duration;

// java --enable-preview --source 25 Day9StructuredConcurrency.java

public class Day9StructuredConcurrency {

    public static void main(String[] args) {
        System.out.println("Starting concurrent data fetch...\n");
        long startTime = System.currentTimeMillis();

        try {
            fetchDashboardData();
        } catch (Exception e) {
            System.out.println("Dashboard fetch failed: " + e.getMessage());
        }

        long duration = System.currentTimeMillis() - startTime;
        System.out.println("\nTotal time taken: " + duration + " ms");
    }

    static void fetchDashboardData() throws Exception {
        // MODERN JAVA 25 API:
        // open() defaults to the "ShutdownOnFailure" policy (awaitAllSuccessfulOrThrow).
        try (var scope = StructuredTaskScope.open()) {
            
            // Fork (start) the concurrent subtasks
            var userTask = scope.fork(Day9StructuredConcurrency::fetchUser);
            var orderTask = scope.fork(Day9StructuredConcurrency::fetchOrders);

            // Wait for all tasks to finish.
            // In Java 25, join() automatically throws an exception if any task failed!
            scope.join();

            // If we get here, both tasks succeeded!
            System.out.println("Success! Assembled Dashboard:");
            System.out.println("- " + userTask.get());
            System.out.println("- " + orderTask.get());
        }
    }

    // --- Simulated API Calls ---

    static String fetchUser() throws InterruptedException {
        System.out.println("-> [User API] Started fetching user...");
        Thread.sleep(Duration.ofMillis(500)); // Simulate network delay
        
        // Let's simulate an error happening!
        throw new RuntimeException("User API is down!"); 
    }

    static String fetchOrders() throws InterruptedException {
        System.out.println("-> [Order API] Started fetching orders...");
        try {
            Thread.sleep(Duration.ofMillis(2000)); // This takes 2 seconds
            System.out.println("-> [Order API] Successfully finished fetching orders!");
            return "Orders: 5 items";
        } catch (InterruptedException e) {
            // When the scope cancels this task, an InterruptedException is thrown
            System.out.println("-> [Order API] Task was CANCELLED to save resources!");
            throw e;
        }
    }
}