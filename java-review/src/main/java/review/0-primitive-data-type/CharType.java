package review.primitive;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 0d. CHAR
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.CharType
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class CharType {

    /** char in a switch: several labels share one result. */
    private static boolean isVowel(char c) {
        return switch (Character.toLowerCase(c)) {
            case 'a', 'e', 'i', 'o', 'u' -> true;
            default -> false;
        };
    }

    /** ++ and += keep the type char: the hidden cast of a compound assignment. */
    private static char next(char c) {
        c++;
        return c;
    }

    public static void main(String[] args) {
        title("CHAR  -  single quotes 'A' = char, double quotes \"A\" = String");

        section("A char is an unsigned 16-bit number");
        syntax(
                "char c = 'A';", "char literal, single quotes",
                "(int) c", "the number (UTF-16 code unit) of a char",
                "(char) n", "number -> char"
        );
        char c = 'A';        show(() -> c);
        show(() -> (int) 'A');                                         // the code of 'A'
        show(() -> (int) 'a');                                         // lowercase is 32 higher
        show(() -> (int) '0');                                         // the digit zero is 48, not 0
        show(() -> (int) Character.MAX_VALUE);                         // 16 bit unsigned: 0 .. 65535
        show(() -> (int) (char) -1);                                   // the only unsigned type in Java: -1 wraps to 65535
        wontCompile("char c = -1;", "possible lossy conversion from int to char: a char is never negative");

        section("Char arithmetic: the result is an int");
        syntax(
                "char + int | char - char", "int (numeric promotion): cast back with (char)",
                "c++ | c += 1", "stay char (the compiler adds the cast)"
        );
        show(() -> 'A' + 1);                                           // TRAP: char + int = int 66, not 'B'
        show(() -> (char) ('A' + 1));                                  // cast back to get 'B'
        show(() -> 'C' - 'A');                                         // char - char = int
        show(() -> 'e' - 'a');                                         // alphabet index of 'e' (a = 0)
        show(() -> next('A'));                                         // c++ stays a char
        show(() -> (char) 65);                                         // code -> char
        show(() -> 'a' - 'A');                                         // 32: the gap between lowercase and uppercase
        wontCompile("char c = 'A'; char d = c + 1;", "possible lossy conversion from int to char");
        note("char e = 'A' + 1; compiles: a constant expression that fits in a char needs no cast (a variable does)");

        section("TRAP: char + char is a number, not a String");
        show(() -> 'A' + 'B');                                         // 65 + 66 = 131
        show(() -> "" + 'A' + 'B');                                    // start with a String and + appends text
        show(() -> 'A' + 'B' + "");                                    // the number first, then the text "131"
        show(() -> "abc".charAt(1) + 1);                               // charAt gives a char: + 1 makes it an int

        section("Digits and letters");
        syntax(
                "c - '0'", "digit char -> int: '7' - '0' is 7",
                "(char) ('0' + n)", "int 0..9 -> digit char",
                "getNumericValue(c) | digit(c, radix)", "Character methods: digit value, also for letters"
        );
        show(() -> '7' - '0');                                         // the classic trick
        show(() -> (char) ('0' + 7));
        show(() -> Character.getNumericValue('7'));
        show(() -> Character.digit('f', 16));                          // hex digit f = 15
        show(() -> Character.forDigit(7, 10));                         // int -> digit char
        show(() -> Character.getNumericValue('a'));                    // TRAP: letters count as 10..35 (a = 10)
        show(() -> Character.getNumericValue('-'));                    // -1: not a digit

        section("Tests and case");
        syntax("Character.isLetter(c) | isDigit | isUpperCase | ...", "static tests; toUpperCase(c) | toLowerCase(c) change case");
        show(() -> Character.isLetter('x'));
        show(() -> Character.isDigit('x'));
        show(() -> Character.isUpperCase('x'));
        show(() -> Character.toUpperCase('x'));
        show(() -> Character.toLowerCase('X'));
        show(() -> Character.isLetterOrDigit('_'));                    // underscore is neither
        show(() -> Character.isWhitespace('\t'));
        show(() -> isVowel('E'));                                      // char in a switch with several labels

        section("Escape sequences (shown as codes)");
        syntax("'\\t' | '\\n' | '\\\\' | '\\'' | '\"' | '$'", "tab | newline | backslash | single quote | double quote | dollar sign");
        show(() -> (int) '\t');                                        // tab
        show(() -> (int) '\n');                                        // newline
        show(() -> (int) '\\');                                        // backslash
        show(() -> (int) '\'');                                        // single quote
        show(() -> (int) '"');                                         // a double quote needs no escape inside single quotes

        section("char is a UTF-16 code unit, not always a whole character");
        syntax("s.codePointCount(0, s.length())", "number of real characters (code points)");
        String smile = new String(Character.toChars(0x1F600));
        show(() -> smile.length());                                    // TRAP: one emoji is TWO chars (a surrogate pair)
        show(() -> smile.codePointCount(0, smile.length()));           // but one code point
        note("Plain letters and digits are one char each; emoji and rare symbols are two, so length() can exceed the number of visible characters");
    }
}
