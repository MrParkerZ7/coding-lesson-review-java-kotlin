package review.variables;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * 1e. TEXT AND FORMATTING: text blocks, String.format, + on Strings, StringBuilder, join
 *     (the String type is lesson 0 StringType; string operations for problems are lesson 6)
 *     Run: mvn -q compile exec:java -Dmain.class=review.variables.TextAndFormatting
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class TextAndFormatting {

    public static void main(String[] args) {
        title("TEXT AND FORMATTING  -  put values into text");
        Locale.setDefault(Locale.ROOT);   // only so that this file prints the same on every machine (see the Locale section)

        section("Text blocks (Java 15): several lines in one literal");
        syntax(
                "\"\"\" + line break ... \"\"\"", "text block: may span lines, a double quote needs no escape",
                "position of the closing quotes", "the leftmost line (the closing quotes count) decides how much indentation is removed",
                "\\ at the end of a line | \\s", "join with the next line (no line break) | keep one trailing space"
        );
        String roses = """
                roses
                  are red
                """;
        show(() -> roses.replace("\n", "|"));                         // closing quotes on their own line: text ends with a line break
        String noEnd = """
                roses
                  are red""";
        show(() -> noEnd.replace("\n", "|"));                          // closing quotes right after the text: no final line break
        String deeper = """
                roses
              """;
        show(() -> deeper.replace("\n", "|"));                         // closing quotes are the leftmost line: 2 spaces of indent stay
        String joined = """
                one \
                two""";
        show(() -> joined);                                            // a backslash at the end of a line joins the lines
        String spaced = """
                ab\s
                cd""";
        show(() -> spaced.replace("\n", "|"));                         // trailing spaces are removed unless you write \s
        String json = """
                {"name": "Ann", "age": 30}
                """;
        show(() -> json.strip());                                      // quotes inside need no escape
        show(() -> roses.lines().count());                             // lines() splits the text at its line breaks: 2 lines

        section("String.format and formatted()");
        syntax(
                "String.format(\"%d %s %.2f\", a, b, c)", "printf style; \"...\".formatted(a, b, c) is the same (Java 15)",
                "%d | %s | %f | %x | %c | %b | %n | %%", "integer | text | decimal | hex | char | boolean | line break | a percent sign",
                "%5d | %-5s | %05d | %,d | %+d | %.3f | %8.2f", "width 5 | left-aligned | zero padded | thousands | with sign | 3 decimals | width 8, 2 decimals"
        );
        show(() -> String.format("%d + %d = %d", 2, 3, 5));
        show(() -> "%s is %d years".formatted("Ann", 30));
        show(() -> "%.2f".formatted(3.14159));                         // rounds to 2 decimals
        show(() -> "%5d|".formatted(42));                              // right-aligned in 5 columns
        show(() -> "%-5d|".formatted(42));                             // left-aligned
        show(() -> "%05d".formatted(42));                              // padded with zeros
        show(() -> "%,d".formatted(1234567));                          // thousands separator
        show(() -> "%+d".formatted(5));                                // always show the sign
        show(() -> "%8.2f|".formatted(3.14159));                       // width 8, 2 decimals
        show(() -> "%x".formatted(255));                               // hexadecimal
        show(() -> "%d%%".formatted(50));                              // %% prints one percent sign
        show(() -> "%.0f".formatted(2.5));                             // %f rounds HALF_UP: 3 (Math.rint(2.5) is 2.0)
        show(() -> "%b %c".formatted(true, 'A'));
        String none = null;
        show(() -> "%s".formatted(none));                              // a null argument prints the text null
        String nl = System.lineSeparator();
        show(() -> "a%nb".formatted().equals("a" + nl + "b"));        // %n is the platform line break (CRLF on Windows), \n is always LF
        show(() -> thrown(() -> "%d".formatted(2.5)));                 // TRAP: %d with a double fails at run time, not at compile time
        show(() -> thrown(() -> "%.2f".formatted(3)));                 // TRAP: %f with an int fails too: write 3.0
        show(() -> thrown(() -> "%s %s".formatted("a")));              // too few arguments

        section("Locale trap");
        show(() -> String.format(Locale.US, "%.2f", 3.14159));
        show(() -> String.format(Locale.GERMANY, "%.2f", 3.14159));    // TRAP: German decimal comma, the default Locale decides
        show(() -> String.format(Locale.GERMANY, "%,d", 1234567));     // and the thousands separator becomes a dot
        note("Locale.setDefault(Locale.ROOT) at the top of this file keeps the output stable; for files, JSON or tests pass Locale.ROOT or Locale.US");

        section("+ on Strings: left to right");
        syntax("a + b", "a String on either side: the other side becomes text; two numbers: added");
        show(() -> 1 + 2 + "a");                                       // (1 + 2) + "a": the numbers are added first
        show(() -> "a" + 1 + 2);                                       // ("a" + 1) + 2: after the first String every + appends
        show(() -> "a" + (1 + 2));                                     // parentheses do the number math first
        show(() -> 'a' + 'b' + "c");                                   // TRAP: char + char is the number 195 (CharType)
        show(() -> "" + 'a' + 'b');                                    // start with a String to get "ab"
        show(() -> "n=" + 10 / 4);                                     // int division first: 2
        show(() -> "n=" + 10 / 4.0);

        section("null in text");
        show(() -> "value: " + none);                                  // + turns null into the four letters null
        show(() -> "" + null);
        show(() -> String.valueOf((Object) none));                     // valueOf(Object) is safe too
        show(() -> Objects.toString(none, "n/a"));                     // with a default for null
        show(() -> thrown(() -> "a".concat(none)));                    // TRAP: concat(null) throws, + does not
        show(() -> thrown(() -> String.valueOf(null)));                // TRAP: the literal null picks valueOf(char[]) and throws
        note("Anything that prints a null prints the text null; check for null first when that matters (lesson 7)");

        section("StringBuilder, join, repeat");
        syntax(
                "new StringBuilder() ... sb.append(x) ... sb.toString()", "one growing buffer (lesson 6)",
                "String.join(sep, parts) | \"ab\".repeat(n)", "parts with a separator, no trailing one | the text n times"
        );
        StringBuilder csv = new StringBuilder();
        for (int i = 1; i <= 3; i++) {
            csv.append(i).append(',');
        }
        show(() -> csv.toString());                                    // TRAP: a trailing comma
        show(() -> String.join(",", "1", "2", "3"));                   // no trailing separator
        show(() -> String.join(" | ", List.of("a", "b", "c")));        // join also takes a List
        show(() -> new StringBuilder("n=").append(5).append('!').toString());   // append returns the builder: calls chain
        show(() -> "ab".repeat(3));
        show(() -> "-".repeat(0));                                     // zero times is the empty String
        note("text += x in a loop copies the whole String every round, O(n^2): use StringBuilder (lesson 6); Collectors.joining is lesson 4");
    }
}
