import java.util.Objects;

public class Day2Records {

    // --- The Java 8 Way (Traditional POJO) ---
    static class UserPojo {
        private final String name;
        private final int age;

        public UserPojo(String name, int age) {
            this.name = name;
            this.age = age;
        }

        public String getName() { return name; }
        public int getAge() { return age; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UserPojo userPojo = (UserPojo) o;
            return age == userPojo.age && Objects.equals(name, userPojo.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, age);
        }

        @Override
        public String toString() {
            return "UserPojo{name='" + name + "', age=" + age + "}";
        }
    }

    // --- The Modern Way (Record) ---
    // This single line does exactly the same thing as the 30 lines above!
    public record UserRecord(String name, int age) {}

    public static void main(String[] args) {
        System.out.println("--- 1. Instantiating and toString() ---");
        
        UserPojo pojo = new UserPojo("Alice", 28);
        UserRecord record = new UserRecord("Alice", 28);
        
        System.out.println("POJO Output:   " + pojo);
        System.out.println("Record Output: " + record);

        System.out.println("\n--- 2. Accessing Data ---");
        
        System.out.println("POJO Name:   " + pojo.getName());
        // Notice the lack of "get" in the record accessor
        System.out.println("Record Name: " + record.name()); 

        System.out.println("\n--- 3. Equality ---");
        
        UserRecord record2 = new UserRecord("Alice", 28);
        // equals() and hashCode() are provided automatically, so this is true
        System.out.println("Are the two records equal? " + record.equals(record2));

        System.out.println("\n--- 4. Local Records ---");
        
        // You can declare a record right inside a method for quick data grouping!
        record Point(int x, int y) {}
        var point = new Point(10, 20);
        System.out.println("Local Point Record: " + point);
    }
}