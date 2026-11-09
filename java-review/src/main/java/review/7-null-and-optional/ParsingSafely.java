package review.nullsafety;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;

/**
 * 7d. PARSING SAFELY: text to number without a crash, and the lookups that return null or throw
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.ParsingSafely
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ParsingSafely {

    /** Parse to an OptionalInt: empty for null, blank text, non-numbers and numbers outside the int range. */
    static OptionalInt tryParse(String text) {
        if (text == null) return OptionalInt.empty();
        try {
            return OptionalInt.of(Integer.parseInt(text.strip()));
        } catch (NumberFormatException e) {
            return OptionalInt.empty();
        }
    }

    public static void main(String[] args) {
        title("PARSING SAFELY  -  turn bad input into 'no value' instead of a crash");

        section("Parsing text to a number");
        syntax(
                "Integer.parseInt(s)", "an int, or NumberFormatException",
                "Integer.valueOf(s)", "the same parse, but returns a boxed Integer",
                "Long.parseLong(s) | Double.parseDouble(s)", "the same idea for the other numbers",
                "Boolean.parseBoolean(s)", "never fails: anything except true (any case) is false"
        );
        show(() -> Integer.parseInt("42"));
        show(() -> thrown(() -> Integer.parseInt("4x2")));
        show(() -> thrown(() -> Integer.parseInt(" 42")));              // TRAP: no trimming, a space makes it invalid
        show(() -> thrown(() -> Integer.parseInt("")));
        show(() -> thrown(() -> Integer.parseInt(null)));               // null gives NumberFormatException here, not NullPointerException
        show(() -> Integer.parseInt("+5"));                             // a leading plus is accepted
        show(() -> thrown(() -> Integer.parseInt("2147483648")));       // one past Integer.MAX_VALUE
        show(() -> Long.parseLong("2147483648"));                       // fits in a long
        show(() -> Double.parseDouble(" 3.5 "));                        // TRAP the other way: parseDouble trims the spaces
        show(() -> thrown(() -> Double.parseDouble(null)));             // and null IS a NullPointerException here
        show(() -> Boolean.parseBoolean("TRUE"));
        show(() -> Boolean.parseBoolean("yes"));                        // TRAP: false, no exception for text that is not true

        section("tryParse: an OptionalInt instead of an exception");
        syntax("OptionalInt.of(parseInt(s)) | catch -> OptionalInt.empty()", "Java has no toIntOrNull() (Kotlin does): write this helper once");
        show(() -> tryParse("42"));
        show(() -> tryParse(" 42 "));                                   // stripped first, so it parses
        show(() -> tryParse("4x2"));
        show(() -> tryParse(null));
        show(() -> tryParse("2147483648"));                             // too big for an int: empty
        show(() -> tryParse("abc").orElse(0));                          // the idiom: parse with a default
        note("Exceptions as control flow are slow for masses of bad input; for interview code a try/catch is fine. Exceptions: lesson 15");

        section("Lookups: null, exception or Optional?");
        syntax(
                "map.get(key)", "null for a missing key (so unbox with care: int n = map.get(k) can throw)",
                "map.getOrDefault(key, d)", "d for a missing key",
                "list.get(i)", "IndexOutOfBoundsException, never null",
                "stream.max(cmp) | findFirst()", "an Optional: empty for an empty stream"
        );
        Map<String, Integer> stock = Map.of("apple", 3);
        List<Integer> nums = new ArrayList<>(List.of(4, 8, 1));
        List<Integer> none = new ArrayList<>();
        show(() -> stock.get("pear"));
        show(() -> stock.getOrDefault("pear", 0));
        show(() -> thrown(() -> stock.get("pear") + 1));                // TRAP: null unboxed to int
        show(() -> thrown(() -> nums.get(10)));
        show(() -> thrown(() -> none.get(0)));
        show(() -> thrown(() -> List.of(4, 8, 1).get(10)));             // List.of reports the subclass ArrayIndexOutOfBoundsException
        show(() -> thrown(() -> none.getFirst()));                      // Java 21: a different exception than get(0)
        show(() -> nums.stream().max(Integer::compare));
        show(() -> none.stream().max(Integer::compare));                // empty list: empty Optional
        show(() -> none.stream().mapToInt(Integer::intValue).max().orElse(-1));
        show(() -> thrown(() -> Collections.max(none)));                // Collections.max has no answer for an empty collection
        show(() -> none.stream().findFirst());

        section("Digits in text");
        syntax(
                "Character.digit(c, 10)", "0..9, or -1 when c is not a digit",
                "Character.getNumericValue(c)", "also gives numbers for letters: a = 10 ... z = 35"
        );
        show(() -> Character.digit('7', 10));
        show(() -> Character.digit('x', 10));                           // -1: the safe test
        show(() -> Character.getNumericValue('x'));                     // TRAP: 33, not -1
        show(() -> Character.getNumericValue('-'));                     // -1
        show(() -> "a1b2c3".chars().filter(Character::isDigit).map(c -> c - '0').sum());   // sum of the digits in a text
        note("Null checks and Objects.requireNonNull: 7a NullBasics. Exceptions in depth: lesson 15");
    }
}
