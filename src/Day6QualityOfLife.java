import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

public class Day6QualityOfLife {

    public static void main(String[] args) throws Exception {
        
        System.out.println("--- 1. Unnamed Variables (_) ---");
        String badNumber = "NotANumber";
        try {
            int number = Integer.parseInt(badNumber);
        } catch (NumberFormatException _) { 
            // We use '_' because we know it failed and don't need the exception object
            System.out.println("Caught an error parsing the number, moving on!");
        }

        System.out.println("\n--- 2. Advanced Optional ---");
        Optional<String> missingUser = Optional.empty();
        
        // Java 8 usually required isPresent() checks. 
        // Modern Java lets you handle both the present and empty cases functionally:
        missingUser.ifPresentOrElse(
            user -> System.out.println("Found: " + user),
            () -> System.out.println("No user found, creating guest session.") 
        );

        System.out.println("\n--- 3. Modern String APIs ---");
        String textBlock = """
                First Line
                   
                Third Line
                """;
                
        System.out.println("Filtering out blank lines:");
        textBlock.lines() // Automatically splits the string into a Stream<String>
                 .filter(line -> !line.isBlank()) // Removes the empty middle line
                 .forEach(System.out::println);

        System.out.println("\n--- 4. Modern File I/O ---");
        // Creating, writing, and reading a file in 3 lines of code!
        Path tempFile = Files.createTempFile("modern_java_", ".txt");
        Files.writeString(tempFile, "File I/O is finally simple in Java!");
        
        String fileContent = Files.readString(tempFile);
        System.out.println("Read from disk: " + fileContent);
        
        Files.delete(tempFile); // Cleaning up
    }
}