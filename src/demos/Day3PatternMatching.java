package demos;

public class Day3PatternMatching {

  // Let's set up some types to work with
  interface Shape {}
  record Circle(double radius) implements Shape {}
  record Rectangle(double length, double width) implements Shape {}
  static class UnknownShape implements Shape {}

  public static void main(String[] args) {
      Shape shape1 = new Circle(5.0);
      Shape shape2 = new Rectangle(10.0, 20.0);
      Object someObject = "Hello, Modern Java!";

      System.out.println("--- 1. Pattern Matching for instanceof ---");
      // The Java 8 Way
      if (someObject instanceof String) {
          String s = (String) someObject; // Manual cast
          System.out.println("Java 8 String length: " + s.length());
      }

      // The Modern Way
      if (someObject instanceof String s) { // Type check AND cast in one go
          System.out.println("Modern String length: " + s.length());
      }

      System.out.println("\n--- 2. Pattern Matching for switch ---");
      System.out.println("Shape 1 is: " + getShapeDescription(shape1));
      System.out.println("Shape 2 is: " + getShapeDescription(shape2));
      System.out.println("Shape 3 is: " + getShapeDescription(new UnknownShape()));

      System.out.println("\n--- 3. Record Patterns (Deconstruction) ---");
      // Notice how we extract 'length' and 'width' directly, no need for r.length()
      if (shape2 instanceof Rectangle(double length, double width)) {
          System.out.println("Deconstructed Rectangle Area: " + (length * width));
      }
  }

  // Modern Switch using Pattern Matching
  static String getShapeDescription(Shape shape) {
      return switch (shape) {
          case Circle c    -> "A circle with radius " + c.radius();
          case Rectangle r -> "A rectangle measuring " + r.length() + "x" + r.width();
          case null        -> "A null shape";
          default          -> "Some other shape we don't know about";
      };
  }
}
