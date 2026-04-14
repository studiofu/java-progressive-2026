public class Day10ScopedValues {


  // Never put request-specific state in class-level variables. Controllers, Services, and Repositories must be completely "stateless". If you need to share state, you either pass it as method arguments, or you use ScopedValue (or ThreadLocal).

  // 1. Declare the Scoped Value. 
  // It is typically public static final so any class can read it.
  public static final ScopedValue<String> LOGGED_IN_USER = ScopedValue.newInstance();

  public static void main(String[] args) {
      System.out.println("--- Starting Web Request ---");

      // Simulate an Admin Request
      // 2. Bind the value to a specific block of code using .where() and .run()
      ScopedValue.where(LOGGED_IN_USER, "Admin_Alice").run(() -> {
          System.out.println("Executing request block...");
          controllerLayer(); 
      });

      System.out.println("--- Web Request Finished ---");
      
      // If we try to access it out here, it will fail cleanly.
      System.out.println("\nIs user bound outside the scope? " + LOGGED_IN_USER.isBound());
  }

  // Notice we do NOT pass the user ID as a parameter here!
  static void controllerLayer() {
      serviceLayer();
  }

  // Or here!
  static void serviceLayer() {
      repositoryLayer();
  }

  // 3. Retrieve the value deep in the stack
  static void repositoryLayer() {
      // We use .get() to read the immutable value
      String user = LOGGED_IN_USER.get();
      System.out.println(">> [Database] Saving record. Audited by: " + user);
  }
}


// import java.lang.ScopedValue;

// public class Day10MultipleScopedValues {

//     // 1. Declare multiple Scoped Values
//     public static final ScopedValue<String> LOGGED_IN_USER = ScopedValue.newInstance();
//     public static final ScopedValue<String> TENANT_ID = ScopedValue.newInstance();
//     public static final ScopedValue<String> TRACE_ID = ScopedValue.newInstance();

//     public static void main(String[] args) {
//         System.out.println("--- Starting Web Request ---");

//         // 2. Chain multiple .where() calls before executing .run()
//         ScopedValue.where(LOGGED_IN_USER, "Admin_Alice")
//                    .where(TENANT_ID, "HK_Datacenter_01")
//                    .where(TRACE_ID, "req-98765-xyz")
//                    .run(() -> {
//                        System.out.println("Executing request block...");
//                        controllerLayer(); 
//                    });

//         System.out.println("--- Web Request Finished ---");
//     }

//     static void controllerLayer() {
//         serviceLayer();
//     }

//     static void serviceLayer() {
//         repositoryLayer();
//     }

//     static void repositoryLayer() {
//         // 3. Retrieve any of the bound values wherever you need them
//         System.out.println(">> [Database] Saving record.");
//         System.out.println("   - Audited by : " + LOGGED_IN_USER.get());
//         System.out.println("   - Tenant     : " + TENANT_ID.get());
//         System.out.println("   - Trace ID   : " + TRACE_ID.get());
//     }
// }