package demos;

import java.util.List;
import java.util.stream.Gatherers;

public class Day5ApiEnhancements {

    public static void main(String[] args) {
        
        // List.of() was added in Java 9 to easily create immutable lists.
        List<String> developers = List.of("Alice", "Bob", "Charlie", "Diana", "Eve");

        System.out.println("--- 1. Sequenced Collections ---");
        // The Java 8 Way: developers.get(developers.size() - 1)
        // The Modern Way:
        System.out.println("First dev: " + developers.getFirst());
        System.out.println("Last dev:  " + developers.getLast());
        System.out.println("Reversed:  " + developers.reversed());

        System.out.println("\n--- 2. Stream .toList() ---");
        // The Java 8 Way: .collect(Collectors.toList())
        // The Modern Way:
        List<String> upperDevs = developers.stream()
                .map(String::toUpperCase)
                .toList(); 
        System.out.println("Uppercase: " + upperDevs);

        System.out.println("\n--- 3. Stream Gatherers (Windowing) ---");
        // Grouping elements into chunks was extremely painful in Java 8 Streams.
        // The Modern Way uses Gatherers:
        List<List<String>> pairs = developers.stream()
                .gather(Gatherers.windowFixed(2)) // Groups elements into sub-lists of 2
                .toList();
                
        System.out.println("Developers in pairs: " + pairs);

        var data = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

        var result = data.stream().gather(Gatherers.windowFixed(4)).toList();
        System.out.println("Windowed data: " + result);
    }
}
