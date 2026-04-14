package demos;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.StampedLock;

public class Day11AdvancedConcurrency {

    // A modern lock optimized for read-heavy workloads
    private static final StampedLock lock = new StampedLock();
    private static double sharedBalance = 1000.0;

    public static void main(String[] args) throws Exception {
        System.out.println("--- 1. Modern CompletableFuture Timeouts ---");
        
        // Scenario A: Fails if it takes longer than 1 second
        System.out.println("Fetching data with 1-second strict timeout...");
        try {
            CompletableFuture<String> strictTask = CompletableFuture.supplyAsync(() -> slowDatabaseCall(2000))
                    .orTimeout(1, TimeUnit.SECONDS); // MODERN JAVA FEATURE
            
            System.out.println(strictTask.join());
        } catch (Exception e) {
            System.out.println("Task timed out safely! Error: " + e.getCause().getClass().getSimpleName());
        }


// // Real-world CompletableFuture Pipeline
// CompletableFuture<Dashboard> dashboardTask = CompletableFuture.supplyAsync(() -> fetchUser(userId))
//     .thenCompose(user -> {
//         // Step 3: Chain a new future based on the result of the first one
//         CompletableFuture<List<Product>> recommendations = fetchRecommendationsAsync(user.getId());
//         CompletableFuture<List<Order>> orders = fetchOrdersAsync(user.getId());
        
//         // Step 4: Combine the two independent futures when they are both done
//         return recommendations.thenCombine(orders, (recs, ords) -> new Dashboard(user, recs, ords));
//     });        


// IN Java 21+, if we are using virtual thread, we want to run the fetch in parallel.
// we need to use StructuredTaskScope to run the fetch in parallel.

// // Running on a Virtual Thread, but spawning sub-tasks in PARALLEL
// try (var scope = StructuredTaskScope.open()) {
    
//   // We fork (spawn) two NEW Virtual Threads to do the fetching at the same time
//   var userTask = scope.fork(() -> fetchUser());
//   var orderTask = scope.fork(() -> fetchOrders());

//   // We wait for both to finish. Total wait time: 1 second!
//   scope.join(); 
  
//   String user = userTask.get();
//   String orders = orderTask.get();
// }



        // Scenario B: Returns a default value if it takes longer than 1 second
        System.out.println("\nFetching data with 1-second fallback timeout...");
        CompletableFuture<String> fallbackTask = CompletableFuture.supplyAsync(() -> slowDatabaseCall(2000))
                .completeOnTimeout("DEFAULT_CACHED_VALUE", 1, TimeUnit.SECONDS); // MODERN JAVA FEATURE
        
        System.out.println("Result: " + fallbackTask.join());


        System.out.println("\n--- 2. StampedLock (Optimistic Reading) ---");
        
        // Step 1: Get a stamp for an optimistic read (does NOT block other threads)
        long stamp = lock.tryOptimisticRead();
        
        // Step 2: Read the data
        double currentBalance = sharedBalance;
        
        // Simulate some tiny delay where another thread MIGHT have changed the balance
        Thread.sleep(10); 

        // Simulate a concurrent write: another thread updates the balance while we're "reading"
        Thread writer = new Thread(() -> {
            long writeStamp = lock.writeLock();
            try {
                sharedBalance += 500.0;
                System.out.println("[Writer] Added $500. New balance: " + sharedBalance);
            } finally {
                lock.unlockWrite(writeStamp);
            }
        });
        writer.start();
        writer.join(); // Ensure the write happens before we validate the stamp

        
        // Step 3: Check if the stamp is still valid (meaning no one called writeLock() in the meantime)
        if (!lock.validate(stamp)) {
            System.out.println("Data changed while reading! Upgrading to a full read lock...");
            // The optimistic read failed, so we fall back to a traditional blocking read lock
            stamp = lock.readLock();
            try {
                currentBalance = sharedBalance;
            } finally {
                lock.unlockRead(stamp);
            }
        } else {
            System.out.println("Optimistic read succeeded! No locks were required.");
        }
        
        System.out.println("Final Read Balance: " + currentBalance);
    }

    private static String slowDatabaseCall(int sleepMillis) {
        try {
            Thread.sleep(sleepMillis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return "DATABASE_RESULT";
    }
}
