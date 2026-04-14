package demos;

public class Day4SealedClasses {

  // 1. The Sealed Interface
  // We explicitly state that ONLY these three types can be a PaymentMethod.
  sealed interface PaymentMethod permits CreditCard, PayPal, Cash {}

  // 2. The Implementations
  // Records are implicitly final, which satisfies the sealed hierarchy rules.
  record CreditCard(String cardNumber, String expiry) implements PaymentMethod {}
  record PayPal(String email) implements PaymentMethod {}
  
  // A standard class must explicitly declare itself final, sealed, or non-sealed.
  final class Cash implements PaymentMethod {
      private final double amount;
      Cash(double amount) { this.amount = amount; }
      public double getAmount() { return amount; }
  }

  public static void main(String[] args) {
      PaymentMethod payment1 = new CreditCard("1234-5678-9012-3456", "12/28");
      PaymentMethod payment2 = new PayPal("user@modernjava.com");
      PaymentMethod payment3 = new Day4SealedClasses().new Cash(50.0); // Inner class instantiation

      System.out.println(processPayment(payment1));
      System.out.println(processPayment(payment2));
      System.out.println(processPayment(payment3));
  }

  // 3. Pattern Matching with Exhaustiveness Checking
  static String processPayment(PaymentMethod payment) {
      return switch (payment) {
          case CreditCard cc -> "Processing card ending in " + cc.cardNumber().substring(15);
          case PayPal pp     -> "Sending redirect link to " + pp.email();
          case Cash c        -> "Please collect " + c.getAmount() + " at the counter.";
          // Notice: NO 'default' branch! The compiler knows we covered all permitted types.
          // Try removing the 'Cash' case above, and watch the compiler throw an error.
      };
  }
}
