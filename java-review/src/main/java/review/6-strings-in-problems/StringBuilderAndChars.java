package review.strings;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;

/**
 * 6c. STRINGBUILDER AND CHARS: build text fast, work with letters
 *     (char basics are lesson 0 CharType; this file is the problem-solving side)
 *     Run: mvn -q compile exec:java -Dmain.class=review.strings.StringBuilderAndChars
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class StringBuilderAndChars {

    /** Caesar shift of one lowercase letter, wrapping from z back to a: the alphabet index trick. O(1). */
    static char shiftLetter(char c, int k) {
        return (char) ('a' + Math.floorMod(c - 'a' + k, 26));
    }

    public static void main(String[] args) {
        title("STRINGBUILDER AND CHARS  -  build text fast, count letters");

        section("StringBuilder: a text you can change");
        syntax(
                "StringBuilder sb = new StringBuilder();", "a mutable text buffer (StringBuffer is the old synchronized twin)",
                "sb.append(x) | sb.insert(i, x) | sb.reverse()", "add at the end | add at index i | reverse in place",
                "sb.deleteCharAt(i) | sb.setCharAt(i, c)", "remove one char | replace one char (also sb.delete(a, b))",
                "sb.toString()", "the finished String"
        );
        StringBuilder sb = new StringBuilder("hello");
        sb.append(" world");  show(() -> sb);
        sb.insert(0, ">> ");  show(() -> sb);
        sb.deleteCharAt(0);  show(() -> sb);                            // removes the first >
        sb.setCharAt(0, '*');  show(() -> sb);                          // replaces one char in place
        sb.setCharAt(2, 'H');  show(() -> sb);
        sb.reverse();  show(() -> sb);                                  // reverses in place and returns the same builder
        show(() -> sb.length());
        wontCompile("String s = \"abc\"; s[0] = 'x';", "a String is not an array and has no setter; a StringBuilder has setCharAt");
        show(() -> new StringBuilder("a").equals(new StringBuilder("a")));   // TRAP: false, no equals() override: compare toString()
        show(() -> "a".contentEquals(new StringBuilder("a")));          // contentEquals compares a String with any CharSequence

        section("s += x in a loop: a new String every round");
        syntax("text += x", "copies the old text into a NEW String: O(n) per round, O(n^2) for the loop");
        String text = "";
        int plusCopies = 0;
        for (int i = 1; i <= 1000; i++) {
            text += "x";
            plusCopies += text.length();                                // the characters this round had to copy
        }
        int copied = plusCopies;  show(() -> copied);                   // 1 + 2 + ... + 1000: about n^2 / 2 characters copied
        StringBuilder builder = new StringBuilder();
        for (int i = 1; i <= 1000; i++) builder.append("x");
        show(() -> builder.length());                                   // the builder wrote each character once: about n
        String before = text;
        text += "!";
        boolean sameString = before == text;  show(() -> sameString);   // false: += made a NEW String, the old one is untouched
        StringBuilder same = builder;
        builder.append("!");
        show(() -> same == builder);                                    // true: append changes the one object

        section("Char helpers");
        syntax(
                "Character.isDigit(c) | isLetter(c) | isWhitespace(c)", "what kind of character (also isLetterOrDigit)",
                "Character.isUpperCase(c) | toLowerCase(c) | toUpperCase(c)", "case test and conversion",
                "c - '0' | Character.digit(c, 10)", "'7' becomes 7"
        );
        show(() -> Character.isDigit('7'));
        show(() -> Character.isLetter('x'));
        show(() -> Character.isLetterOrDigit('_'));                     // underscore is neither
        show(() -> Character.isUpperCase('Q'));
        show(() -> Character.toLowerCase('Q'));
        show(() -> (int) 'a');                                          // a char is a number: 97
        show(() -> '7' - '0');                                          // the manual way: 7
        show(() -> Character.getNumericValue('7'));
        show(() -> Character.getNumericValue('x'));                     // TRAP: 33, letters count as digits of a bigger base (a = 10 ... z = 35)
        show(() -> Character.digit('x', 10));                           // -1 for a non-digit: the safe test

        section("Char arithmetic: the alphabet index trick");
        syntax("c - 'a'", "position of a lowercase letter in the alphabet (a = 0); (char) ('a' + n) goes back");
        show(() -> 'a' + 1);                                            // char + int is an INT
        show(() -> (char) ('a' + 1));                                   // cast back to get a char
        char next = 'a' + 1;  show(() -> next);                         // a constant that fits in a char needs no cast
        show(() -> 'z' - 'a');
        show(() -> shiftLetter('c', 3));                                // Caesar shift by 3
        show(() -> shiftLetter('x', 3));                                // wraps around from z to a
        show(() -> shiftLetter('b', -3));                               // floorMod keeps a negative shift positive (plain % would not)
        show(() -> 'a' + 'b');                                          // TRAP: 195, two chars ADD as numbers (it compiles)
        show(() -> "" + 'a' + 'b');                                     // start from a String to join chars
        show(() -> 'a' + 'b' + "");                                     // TRAP: "195", evaluated left to right: the int sum comes first
        StringBuilder mix = new StringBuilder();
        mix.append('a' + 1);
        mix.append((char) ('a' + 1));
        show(() -> mix);                                                // TRAP: the first append got an int (98), the second a char (b)
        wontCompile("int i = 1; char c = 'a' + i;", "possible lossy conversion from int to char: write (char) ('a' + i)");

        section("Counting letters with int[26]");
        syntax("int[] counts = new int[26]; counts[c - 'a']++", "one counter per letter a..z: O(1) space, no hashing");
        int[] counts = new int[26];
        for (char c : "banana".toCharArray()) counts[c - 'a']++;
        show(() -> counts['a' - 'a']);                                  // how many a
        show(() -> counts['n' - 'a']);                                  // how many n
        List<String> seen = new ArrayList<>();
        for (int i = 0; i < 26; i++) if (counts[i] > 0) seen.add((char) ('a' + i) + "=" + counts[i]);
        show(() -> seen);                                               // index back to the letter
        show(() -> thrown(() -> counts['A' - 'a']));                    // TRAP: 'A' - 'a' is -32, an index error: lowercase first
        int[] ascii = new int[128];
        for (char c : "Hello".toCharArray()) ascii[c]++;
        show(() -> ascii['l']);                                         // a char works as an index directly: int[128] covers ASCII
        note("For any Unicode character use HashMap<Character, Integer> and map.merge(c, 1, Integer::sum)");

        section("Cost");
        bigO(
                "sb.append(x) | sb.charAt(i) | sb.setCharAt", "O(1) (append is O(1) amortized)",
                "sb.insert(0, x) | sb.deleteCharAt(0)", "O(n): the rest of the text shifts",
                "sb.reverse() | sb.toString()", "O(n)",
                "text += x in a loop of n rounds", "O(n^2) in total",
                "int[26] counting", "O(n) time, O(1) space: 26 counters whatever n is",
                "HashMap<Character, Integer> counting", "O(n) time, O(k) space for k distinct characters"
        );
    }
}
