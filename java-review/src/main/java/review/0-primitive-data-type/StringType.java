package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 0h. STRING: not a primitive, but the basic type every program uses
 *     (string operations used in interview problems get their own lesson, 6)
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.StringType
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StringType {

    /** switch on a String compares with equals(); a null selector throws NullPointerException. */
    private static String describe(String command) {
        return switch (command) {
            case "start", "go" -> "starting";
            case "stop" -> "stopping";
            default -> "unknown";
        };
    }

    /** Builds "1,2,...,n" with one StringBuilder instead of n new Strings. */
    private static String counting(int n) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= n; i++) {
            if (i > 1) sb.append(',');
            sb.append(i);
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        title("STRING  -  an immutable sequence of chars");

        section("Literals");
        syntax(
                "\"text\"", "String literal, double quotes (single quotes are a char)",
                "new String(\"text\")", "always a NEW object (almost never needed)",
                "\"\"\" ... \"\"\"", "text block: several lines (lesson 1, TextAndFormatting)"
        );
        String name = "Java";
        show(() -> name);
        show(() -> "a\tb".length());                                   // an escape like \t is ONE char
        show(() -> name + " " + name.length());                        // + joins text (lesson 1)

        section("Immutable: every method returns a NEW String");
        String s = "abc";
        show(() -> s.toUpperCase());                                   // returns a NEW String
        show(() -> s);                                                 // the original is unchanged
        wontCompile("String s = \"abc\"; s[0] = 'x';", "array required, but String found: use s.substring or a StringBuilder");
        note("TRAP: s.toUpperCase(); on its own line does nothing useful, you must keep the result: s = s.toUpperCase();");

        section("Length and indexing");
        syntax("s.length() | s.charAt(i) | s.isEmpty() | s.isBlank()", "length() is a METHOD (an array has the field .length)");
        show(() -> s.length());
        show(() -> s.charAt(0));                                       // index from 0, gives a char
        show(() -> s.charAt(s.length() - 1));                          // the last char
        show(() -> thrown(() -> s.charAt(10)));                        // outside the String: StringIndexOutOfBoundsException
        show(() -> "  ".isEmpty());                                    // TRAP: two spaces are not empty
        show(() -> "  ".isBlank());                                    // isBlank() is true for empty or only whitespace
        show(() -> "".isEmpty());

        section("Equality: == compares objects, equals() compares text");
        syntax("a.equals(b) | a.equalsIgnoreCase(b)", "same characters (use these)");
        String lit = "hello";
        String built = new String("hello");
        show(() -> lit.equals(built));                                 // same characters: equal
        show(() -> lit == built);                                      // TRAP: two different objects
        show(() -> lit == "hello");                                    // two identical literals are ONE shared object (the string pool)
        show(() -> built.intern() == lit);                             // intern() returns the pooled literal
        String part = "hel";
        String joined = part + "lo";
        show(() -> joined == lit);                                     // TRAP: a concatenation at run time is a new object
        show(() -> "hel" + "lo" == lit);                               // but the compiler joins constants, so this one is the pooled literal
        show(() -> "abc".equalsIgnoreCase("ABC"));
        note("== versus equals() on Strings is the classic first interview question: always use equals() for text");

        section("Order: compareTo");
        syntax("a.compareTo(b)", "negative | 0 | positive; trust only the sign");
        show(() -> "apple".compareTo("banana"));                       // negative: apple comes first
        show(() -> "apple".compareTo("apples"));                       // a prefix is smaller
        show(() -> "a".compareTo("A"));                                // 32: lowercase sorts AFTER uppercase
        show(() -> "Apple".compareToIgnoreCase("apple"));              // 0

        section("Everyday operations");
        String h = "Hello, Java";
        show(() -> "  Hello  ".strip());                               // strip() (Java 11) removes whitespace on both sides, like trim()
        String emSpace = (char) 0x2003 + "Hi";
        show(() -> emSpace.trim().length());                           // TRAP: trim() only removes chars up to ' ': the Unicode space stays (3)
        show(() -> emSpace.strip().length());                          // strip() knows Unicode whitespace (2)
        show(() -> h.substring(7));                                    // from index 7 to the end
        show(() -> h.substring(0, 5));                                 // end index is exclusive
        show(() -> h.indexOf("J"));                                    // index of the first match
        show(() -> h.indexOf("z"));                                    // -1 when not found (no exception)
        show(() -> h.contains("Jav"));
        show(() -> h.replace("l", "L"));                               // replaces ALL matches
        show(() -> h.toLowerCase());
        show(() -> "ab".repeat(3));
        show(() -> h.toCharArray().length);                            // the chars as an array (lesson 6)
        show(() -> describe("go"));                                    // switch on a String uses equals()
        show(() -> thrown(() -> describe(null)));                      // TRAP: switch on null throws NullPointerException

        section("null in text");
        String none = null;
        show(() -> "value: " + none);                                  // + turns null into the four letters "null"
        show(() -> String.valueOf((Object) none));                     // valueOf is safe too: "null"
        show(() -> thrown(() -> none.length()));                       // calling a method on null: NullPointerException
        show(() -> "a".equals(none));                                  // false: put the literal on the left to avoid the NPE (lesson 7)

        section("Hash code and building in a loop");
        show(() -> "Aa".hashCode() == "BB".hashCode());                // different Strings can share a hash code (lesson 12)
        show(() -> counting(5));                                       // one StringBuilder, no trailing comma
        note("text += x in a loop copies the whole String every round: O(n^2). Use StringBuilder (lesson 6)");
    }
}
