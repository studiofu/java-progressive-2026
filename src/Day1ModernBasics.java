import java.util.Map;

public class Day1ModernBasics {

    public static void main(String[] args) {
        System.out.println("--- 1. Local Variable Type Inference ---");
        // The Java 8 Way
        Map<String, Integer> java8Map = Map.of("Alice", 25, "Bob", 30);
        
        // The Modern Way (var)
        var modernMap = Map.of("Alice", 25, "Bob", 30); 
        System.out.println("Variables inferred cleanly: " + modernMap);

        System.out.println("\n--- 2. Text Blocks ---");
        // The Java 8 Way
        String java8Json = "{\n" +
                           "  \"name\": \"John\",\n" +
                           "  \"age\": 30\n" +
                           "}";
                           
        // The Modern Way (Text Blocks)
        var modernJson = """
                {
                  "name": "John",
                  "age": 30
                }
                """;
        System.out.println("JSON without escapes:\n" + modernJson);

        System.out.println("--- 3. Switch Expressions ---");
        String day = "TUESDAY";
        
        // The Java 8 Way
        String typeOfDay8;
        switch (day) {
            case "MONDAY":
            case "TUESDAY":
            case "WEDNESDAY":
            case "THURSDAY":
            case "FRIDAY":
                typeOfDay8 = "Weekday";
                break;
            case "SATURDAY":
            case "SUNDAY":
                typeOfDay8 = "Weekend";
                break;
            default:
                throw new IllegalArgumentException("Invalid day: " + day);
        }

        // The Modern Way (Switch Expressions returning a value)
        var typeOfDayModern = switch (day) {
            case "MONDAY", "TUESDAY", "WEDNESDAY", "THURSDAY", "FRIDAY" -> "Weekday";
            case "SATURDAY", "SUNDAY" -> "Weekend";
            default -> throw new IllegalArgumentException("Invalid day: " + day);
        };
        
        System.out.println("Today is a: " + typeOfDayModern);
    }
}