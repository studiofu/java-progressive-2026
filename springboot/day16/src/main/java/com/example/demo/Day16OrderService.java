import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationRegistry;
import org.springframework.stereotype.Service;

@Service
public class Day16OrderService {

    // Spring Boot automatically provides this registry out of the box
    private final ObservationRegistry observationRegistry;

    public Day16OrderService(ObservationRegistry observationRegistry) {
        this.observationRegistry = observationRegistry;
    }

    public String placeOrder(String orderId) {
        
        // We create a single "Observation" named "order.processing"
        return Observation.createNotStarted("order.processing", observationRegistry)
                
                // We add tags (Key-Value pairs) so we can filter our dashboards later!
                .lowCardinalityKeyValue("order.type", "premium_user")
                
                // .observe() automatically starts the timer, runs the code, 
                // records any errors, and stops the timer when finished.
                .observe(() -> {
                    
                    // --- Your actual business logic goes here ---
                    simulateDatabaseCall();
                    
                    // If you look at your console logs here, Spring Boot automatically
                    // injects a [TraceID, SpanID] into the log prefix!
                    System.out.println("Order " + orderId + " successfully processed.");
                    
                    return "Success: " + orderId;
                });
    }

    private void simulateDatabaseCall() {
        try {
            Thread.sleep(150); // Simulating a 150ms database insert
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}