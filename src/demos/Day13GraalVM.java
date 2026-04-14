package demos;

// in wsl using java 25 graalvm native image, run the following command to build the native image:
// javac demos/Day13GraalVM.java
// native-image demos.Day13GraalVM

public class Day13GraalVM {

  // This captures the exact nanosecond the JVM (or Native Image) begins loading the class
  private static final long START_TIME = System.nanoTime();

  public static void main(String[] args) {
      long endTime = System.nanoTime();
      
      // Convert nanoseconds to milliseconds
      double startupTimeMs = (endTime - START_TIME) / 1_000_000.0;
      
      System.out.println("Hello, Cloud-Native Java!");
      System.out.println("Time taken to boot and execute: " + startupTimeMs + " ms");
  }
}
