package review.exceptions;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 15b. THE EXCEPTIONS YOU WILL MEET, WITH THE CODE THAT TRIGGERS EACH
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.CommonExceptions
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class CommonExceptions {

    /** Returns 0, but the compiler cannot see it (10 / 0 written literally draws a lint warning). */
    static int zero() {
        return 0;
    }

    /** Returns null, hidden from the compiler. */
    static String nothing() {
        return null;
    }

    /** Always "text", but typed Object: the cast cannot be judged at compile time. */
    static Object mystery() {
        return "text";
    }

    /** Recursion with no end: every call waits for the next one, so the stack runs out. */
    static int endless(int n) {
        return endless(n + 1) + 1;
    }

    public static void main(String[] args) {
        title("COMMON EXCEPTIONS  -  the trigger and the class name");

        section("Arithmetic: int divides by zero with an exception, double does not");
        show(() -> thrown(() -> 10 / zero()));                             // ArithmeticException
        show(() -> thrown(() -> 10 % zero()));                             // so does int %
        show(() -> 10.0 / zero());                                         // double / 0 is Infinity, no exception
        show(() -> 0.0 / zero());                                          // 0.0 / 0 is NaN, still no exception
        show(() -> thrown(() -> Math.addExact(Integer.MAX_VALUE, 1)));     // overflow normally wraps silently; addExact throws
        show(() -> thrown(() -> Math.toIntExact(3_000_000_000L)));         // so does the long to int check

        section("NumberFormatException: parseInt on bad text");
        show(() -> thrown(() -> Integer.parseInt("abc")));
        show(() -> thrown(() -> Integer.parseInt("")));                    // empty text is bad too
        show(() -> thrown(() -> Integer.parseInt(" 7")));                  // a space is bad: parseInt does not trim
        show(() -> thrown(() -> Integer.parseInt("2147483648")));          // one past Integer.MAX_VALUE
        show(() -> thrown(() -> Integer.parseInt(null)));                  // null is NumberFormatException, not NullPointerException
        show(() -> Integer.parseInt("+7"));                                // a leading + or - is fine

        section("IndexOutOfBoundsException and its subclasses");
        List<Integer> empty = new ArrayList<>();
        int[] one = new int[1];
        show(() -> thrown(() -> empty.get(0)));                            // IndexOutOfBoundsException
        show(() -> thrown(() -> one[3]));                                  // arrays throw the subclass ArrayIndexOutOfBoundsException
        show(() -> thrown(() -> "abc".charAt(5)));                         // strings throw StringIndexOutOfBoundsException
        show(() -> thrown(() -> "abc".substring(2, 9)));
        show(() -> thrown(() -> new int[zero() - 1]));                     // a negative array size
        show(() -> ArrayIndexOutOfBoundsException.class.getSuperclass().getSimpleName());

        section("NoSuchElementException and its look-alikes");
        show(() -> thrown(() -> new ArrayList<Integer>().iterator().next()));
        show(() -> thrown(() -> new ArrayDeque<Integer>().removeFirst())); // an empty deque; poll() would return null
        show(() -> thrown(() -> new ArrayList<Integer>().getFirst()));     // Java 21: getFirst on an empty list
        show(() -> thrown(() -> Optional.empty().get()));                  // Optional.get() on empty (lesson 7)
        show(() -> thrown(() -> List.of(1, 2).stream().filter(n -> n > 5).findFirst().orElseThrow()));
        show(() -> Map.of("a", 1).get("b"));                               // Map.get is null, not an exception

        section("ClassCastException, NullPointerException, StackOverflowError");
        show(() -> thrown(() -> (Integer) mystery()));                     // a String is not an Integer
        show(() -> thrown(() -> nothing().length()));                      // a method call on null
        show(() -> thrown(() -> { int n = (Integer) null; return n; }));   // unboxing null is also an NPE
        show(() -> thrown(() -> endless(0)));                              // an Error, not an Exception

        section("The unsupported, illegal and concurrent family");
        show(() -> thrown(() -> List.of(1).add(2)));                       // List.of is immutable: UnsupportedOperationException
        show(() -> thrown(() -> "ab".repeat(-1)));                         // a bad ARGUMENT: IllegalArgumentException
        show(() -> thrown(() -> { new ArrayList<>(List.of(1)).iterator().remove(); return 0; }));   // remove() before next(): IllegalStateException (bad STATE)
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4));
        show(() -> thrown(() -> { for (int n : numbers) if (n == 2) numbers.remove((Integer) n); return 0; }));   // ConcurrentModificationException
        show(() -> numbers);                                               // the remove did happen before the next iteration failed
        note("The ConcurrentModificationException loop and its three safe fixes are in lesson 3 (ListMutate)");
        note("Catch the narrowest type you can handle; catching Throwable also swallows Errors");
    }
}
