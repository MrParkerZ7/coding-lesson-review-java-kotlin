package review.strings;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.Arrays;
import java.util.Comparator;

/**
 * 6a. STRING OPERATIONS: split, slice, search, transform, convert, compare
 *     (the String type itself is lesson 0 StringType; regular expressions are 6b RegexBasics)
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.StringOperations
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StringOperations {

    public static void main(String[] args) {
        title("STRING OPERATIONS  -  the tools of every string problem");
        note("A String is immutable: every operation below returns a NEW String and leaves the original alone");

        String s = "Hello, World";

        section("split and join");
        syntax(
                "s.split(regex)", "String[]; the delimiter is a REGEX, and trailing empty pieces are dropped",
                "s.split(regex, limit)", "limit n: at most n pieces; limit -1 keeps the trailing empty pieces",
                "String.join(sep, parts...)", "the reverse: pieces -> one String"
        );
        show(() -> "a,b,c".split(","));
        show(() -> "a,,b".split(","));                                  // an empty piece in the middle stays
        show(() -> "a,b,,".split(",").length);                          // TRAP: trailing empty pieces are removed: 2, not 4
        show(() -> "a,b,,".split(",", -1).length);                      // limit -1 keeps them
        show(() -> "a,b,c,d".split(",", 2));                            // at most 2 pieces: the rest stays in the last one
        show(() -> "a.b.c".split(".").length);                          // TRAP: "." is a regex (any char): all pieces empty, all dropped
        show(() -> "a.b.c".split("\\."));                               // escape the dot: a regex needs a doubled backslash
        show(() -> "a|b".split("|"));                                   // TRAP: | is "or" in a regex, so it splits between every char
        show(() -> "  a  b ".split(" ").length);                        // every single space splits: empty pieces at the front and middle
        show(() -> "  a  b ".trim().split("\\s+"));                     // trim, then split on runs of whitespace
        show(() -> "".split(",").length);                               // 1: ONE empty String, not an empty array
        show(() -> String.join("+", "a-b-c".split("-")));

        section("Slicing: substring and charAt");
        syntax(
                "s.substring(start, endExclusive)", "start included, end EXCLUDED; throws when out of range",
                "s.substring(start)", "from start to the end",
                "s.charAt(i)", "one char; no negative index, throws when out of range"
        );
        show(() -> s.substring(7));
        show(() -> s.substring(0, 5));                                  // indexes 0..4
        show(() -> s.substring(7, 7));                                  // start == end: the empty String
        show(() -> thrown(() -> s.substring(5, 20)));                   // TRAP: end beyond the length throws
        show(() -> thrown(() -> s.charAt(99)));
        show(() -> s.substring(0, Math.min(99, s.length())));          // no take(n) in Java: cap the end yourself
        show(() -> s.charAt(s.length() - 1));                           // the last char: there is no s.charAt(-1)
        wontCompile("int n = \"abc\".length;", "length is a METHOD on a String: length() (an array has the field length)");

        section("Search and test");
        show(() -> s.contains("Wor"));
        show(() -> s.toLowerCase().contains("world"));                  // no ignore-case flag: lower both sides
        show(() -> s.startsWith("Hell"));
        show(() -> s.endsWith("rld"));
        show(() -> s.indexOf('o'));                                     // first match
        show(() -> s.lastIndexOf('o'));                                 // last match
        show(() -> s.indexOf('o', 5));                                  // search from index 5
        show(() -> s.indexOf("xyz"));                                   // -1 when missing, never an exception
        show(() -> thrown(() -> "".charAt(0)));                         // charAt on "" throws StringIndexOutOfBoundsException
        wontCompile("s.contains('o')", "contains wants a CharSequence, a char is not one: use \"o\" or indexOf('o') >= 0");

        section("Case, trim, replace, repeat, lines");
        show(() -> s.toUpperCase());
        show(() -> "  hi \n".strip());                                  // strip removes whitespace on both ends, newline included
        String em = (char) 0x2003 + "hi" + (char) 0x2003;               // "hi" between two EM SPACE characters (U+2003)
        show(() -> em.trim().length());                                 // 4: trim only removes chars up to U+0020
        show(() -> em.strip().length());                                // 2: strip (Java 11) knows Unicode whitespace
        show(() -> s.replace("l", "L"));                                // replaces ALL matches, plain text
        show(() -> s.replaceFirst("l", "L"));                           // replaceFirst takes a REGEX
        show(() -> "ab".repeat(3));
        show(() -> "a\nb\r\nc".lines().count());                        // 3 lines: lines() understands both newline styles
        show(() -> "   ".isBlank());                                    // blank = empty or only whitespace
        show(() -> "   ".isEmpty());                                    // isEmpty = length 0 only

        section("String <-> char[] <-> chars()");
        syntax(
                "s.toCharArray()", "a char[] you CAN change in place",
                "String.valueOf(chars) | new String(chars)", "back to a String",
                "s.chars()", "an IntStream of the char codes (ints, not chars)"
        );
        wontCompile("\"abc\".reverse()", "String has no reverse(): use new StringBuilder(s).reverse().toString()");
        char[] chars = "hello".toCharArray();
        Arrays.sort(chars);
        show(() -> chars);                                              // sorted in place
        show(() -> String.valueOf(chars));
        show(() -> "hello".chars().filter(c -> c == 'l').count());      // count one letter
        show(() -> "abc".chars().sum());                                // 294 = 97 + 98 + 99: chars() gives numbers
        show(() -> new StringBuilder("hello").reverse().toString());
        show(() -> chars.toString().startsWith("[C@"));                 // TRAP: toString() of a char[] is an address like [C@1b2c3d

        section("Comparing text");
        show(() -> "abc".compareTo("abd"));                             // negative: the first different char decides
        show(() -> "ab".compareTo("abc"));                              // a prefix is smaller: the length difference
        show(() -> "apple".compareTo("Banana"));                        // positive: 'a' (97) is bigger than 'B' (66)
        show(() -> "apple".compareToIgnoreCase("Banana"));
        show(() -> "10".compareTo("9"));                                // TRAP: text order compares char by char, '1' < '9'
        String[] numbers = {"10", "9", "2"};
        Arrays.sort(numbers);
        show(() -> numbers);                                            // sorted as TEXT: 10, 2, 9
        Arrays.sort(numbers, Comparator.comparingInt(Integer::parseInt));
        show(() -> numbers);                                            // sorted by the number inside
        show(() -> "%s has %d items".formatted("Ann", 3));              // more formatting: lesson 1 TextAndFormatting
        note("compareTo promises only the SIGN (negative, zero, positive), never -1 or 1. == versus equals: lesson 0 StringType");

        section("Cost");
        bigO(
                "s.charAt(i) | s.length()", "O(1)",
                "substring | replace | repeat | toUpperCase | s + t", "O(n): a new String is copied",
                "contains | indexOf", "O(n) typical, O(n * m) worst case for a pattern of length m",
                "equals | compareTo", "O(n): char by char, stops at the first difference",
                "split | lines | toCharArray | chars()", "O(n) time and O(n) space",
                "s += x in a loop", "O(n^2) in total: see 6c StringBuilderAndChars"
        );
    }
}
