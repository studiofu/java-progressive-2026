package com.example.demo;

import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.annotation.Observed;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class DemoApplication {

	public static void main(String[] args) {
		SpringApplication.run(DemoApplication.class, args);

	}

		// CRITICAL FIX: You MUST declare this bean, or the @Observed annotation will be completely ignored!
		@Bean
		public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
				return new ObservedAspect(observationRegistry);
		}			

}


// We put the Controller here just to easily trigger the code from a browser
@RestController
class OrderController {
    
    private final OrderService orderService;

    OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/buy")
    public String buy() {
        return orderService.placeOrder("ORD-999");
    }
}

@RestController
class OrderService {

    // The annotation automatically tracks this method
    @Observed(name = "order.processing", contextualName = "process-premium-order")
    public String placeOrder(String orderId) {
        System.out.println("-> Executing business logic for: " + orderId);
        try {
            Thread.sleep(150); // Simulate database delay
        } catch (InterruptedException e) { }
        return "Successfully processed: " + orderId;
    }
}