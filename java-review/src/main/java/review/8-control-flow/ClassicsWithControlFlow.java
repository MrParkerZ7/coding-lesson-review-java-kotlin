package review.control;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

/**
 * 8e. CLASSICS WITH CONTROL FLOW: FizzBuzz, leap year, grade, day name, sign
 *     (the small problems every interview starts with; the bigger drills are lesson 9)
 *     Run: mvn -q compile exec:java -Dmain.class=review.control.ClassicsWithControlFlow
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ClassicsWithControlFlow {

    static String[] fizzBuzz(int n) {
        String[] out = new String[n];
        for (int i = 1; i <= n; i++) out[i - 1] = fizzBuzzWord(i);
        return out;
    }

    /** The combined case (15) must be tested FIRST, or the earlier tests swallow it. */
    static String fizzBuzzWord(int n) {
        if (n % 15 == 0) return "FizzBuzz";
        if (n % 3 == 0) return "Fizz";
        if (n % 5 == 0) return "Buzz";
        return String.valueOf(n);
    }

    /** FizzBuzz with the tests in the wrong order. */
    static String fizzBuzzWrong(int n) {
        if (n % 3 == 0) return "Fizz";
        if (n % 5 == 0) return "Buzz";
        if (n % 15 == 0) return "FizzBuzz";
        return String.valueOf(n);
    }

    /** Leap year: divisible by 400, or by 4 but not by 100. */
    static boolean isLeapYear(int y) {
        return y % 400 == 0 || (y % 4 == 0 && y % 100 != 0);
    }

    /** Ranges become comparisons: a switch cannot test "90 to 100", so the guards go from the top down. */
    static String gradeOf(int score) {
        if (score < 0 || score > 100) return "invalid";
        if (score >= 90) return "A";
        if (score >= 80) return "B";
        if (score >= 70) return "C";
        return "F";
    }

    /** A switch EXPRESSION is the natural fit for one value per branch. */
    static String dayName(int n) {
        return switch (n) {
            case 1 -> "Mon";
            case 2 -> "Tue";
            case 3 -> "Wed";
            case 4 -> "Thu";
            case 5 -> "Fri";
            case 6 -> "Sat";
            case 7 -> "Sun";
            default -> "?";
        };
    }

    static int signOf(int n) {
        return n > 0 ? 1 : n < 0 ? -1 : 0;
    }

    public static void main(String[] args) {
        title("CLASSICS  -  FizzBuzz, leap year, grade, day name, sign");

        section("FizzBuzz: order of the tests");
        syntax("n % 15 == 0 first, then % 3, then % 5", "the most specific test goes first, or the general ones swallow it");
        show(() -> fizzBuzz(15));                                       // 15 is FizzBuzz, not Fizz
        show(() -> fizzBuzz(0));                                        // no numbers, an empty array
        show(() -> fizzBuzzWrong(15));                                  // TRAP: n % 3 matches first, so FizzBuzz is never produced

        section("Leap year");
        show(() -> isLeapYear(1900));                                   // divisible by 100 but not by 400
        show(() -> isLeapYear(2000));
        show(() -> isLeapYear(2024));
        show(() -> isLeapYear(2023));

        section("Grade (ranges) and day name (single values)");
        show(() -> gradeOf(90));                                        // boundary: 90 is an A
        show(() -> gradeOf(89));
        show(() -> gradeOf(101));
        show(() -> gradeOf(-1));
        show(() -> dayName(3));
        show(() -> dayName(9));

        section("Sign");
        show(() -> signOf(-9));
        show(() -> signOf(0));
        show(() -> Integer.signum(-9));                                 // the library already has it
        note("Range or condition: if / else if. One value per branch: switch. Two outcomes: the ternary");
    }
}
