/**
 * 6c. CHARS AND CODES: there is no char type, so use character codes
 *     (code units and UTF-16 are lesson 0 strings-basics; this file is the problem-solving side)
 *     Run: npx tsx src/6-strings-in-problems/chars-and-codes.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** Position of a lowercase letter in the alphabet: a = 0 ... z = 25. O(1). */
const alphabetIndex = (c: string): number => c.charCodeAt(0) - 97;

/** Caesar shift of one lowercase letter, wrapping around; the double % also handles a negative k. O(1). */
const shiftLetter = (c: string, k: number): string => String.fromCharCode(((alphabetIndex(c) + k) % 26 + 26) % 26 + 97);

title("CHARS AND CODES  -  a character is a string of length 1");

section("No char type: s[i] is a string");
syntax(
  ["s[i] | s.at(i) | s.charAt(i)", "one-character STRING (s[99] is undefined, charAt(99) is the empty string)"],
  ["c.charCodeAt(0) | c.codePointAt(0)", "the number of the character: a UTF-16 unit | a whole code point"],
  ["String.fromCharCode(n)", "the character with that number"],
);
const word = "cab";
show(() => typeof word[0]);                             // string: there is no separate char type
show(() => word.charCodeAt(0));                         // 99 is the code of c
show(() => "a".charCodeAt(0));                          // 97
show(() => "A".charCodeAt(0));                          // 65: upper case letters come BEFORE lower case
show(() => "0".charCodeAt(0));                          // 48: the digits are 48..57
show(() => String.fromCharCode(97 + 2));                // c
show(() => word.charAt(99));                            // "": charAt never gives undefined
show(() => word[99]);                                   // undefined: indexing does
show(() => "a" + 1);                                    // TRAP: "a1", + on a string is concatenation, not a code shift
show(() => "a" + "b");                                  // "ab": not 195
wontCompile("const idx = c - \"a\";", "TS2362: the left-hand side of an arithmetic operation must be of type 'number'");
wontCompile("const digit = \"7\" - \"0\";", "TS2362: the same: strings do not do arithmetic (Kotlin and Java allow it for chars)");
show(() => Number("c") - Number("a"));                  // NaN at run time: the conversion fails, so use charCodeAt

section("The alphabet index trick");
syntax(
  ["c.charCodeAt(0) - 97", "position of a lowercase letter in the alphabet (a = 0)"],
  ["String.fromCharCode(97 + i)", "back from the position to the letter"],
);
show(() => alphabetIndex("a"));
show(() => alphabetIndex("z"));
show(() => String.fromCharCode(97 + 25));
show(() => shiftLetter("c", 3));                        // Caesar shift by 3
show(() => shiftLetter("x", 3));                        // wraps around from z to a
show(() => shiftLetter("a", -1));                       // z: the double modulo handles negative shifts
show(() => (0 - 1) % 26);                               // TRAP: -1, % keeps the sign, so a single % would give a bad index
show(() => Array.from("abc", (c) => c.charCodeAt(0))); // map every character to its code
show(() => String.fromCharCode(...[104, 105]));         // a list of codes back to text

section("Counting letters with an array of 26");
syntax(
  ["const counts = new Array<number>(26).fill(0);", "one counter per letter a..z: O(1) space, no hashing"],
  ["counts[c.charCodeAt(0) - 97]++", "count one letter"],
);
const counts = new Array<number>(26).fill(0);           // not Array(26).fill(0): that is typed any[]
for (const c of "banana") counts[alphabetIndex(c)]++;
show(() => counts[alphabetIndex("a")]);                 // how many a
show(() => counts[alphabetIndex("n")]);                 // how many n
show(() => counts.length);                              // still 26
const seen = counts.map((n, i) => [String.fromCharCode(97 + i), n] as const).filter(([, n]) => n > 0);
show(() => seen);                                       // index back to the letter, zero counts dropped
const broken = new Array<number>(26).fill(0);
broken["A".charCodeAt(0) - 97]++;                      // 65 - 97 = -32: not an index, and JavaScript does not complain
show(() => broken.length);                              // TRAP: still 26 and no element changed, yet no error ...
show(() => broken[-32]);                                // ... NaN: -32 became a property name and undefined + 1 is NaN
note("An out-of-range index never throws in JavaScript: lower-case the text first, or guard 'a' <= c && c <= 'z'");
note("Any ASCII character: new Array<number>(128) and counts[c.charCodeAt(0)]; unknown characters: a Map (lesson 5)");

section("Is it a digit, a letter, a space?");
syntax(
  ["c >= \"0\" && c <= \"9\"", "comparing one-character strings compares their codes"],
  ["/\\d/.test(c) | /[a-z]/i.test(c)", "the regex versions (ASCII letters only)"],
  ["c.toLowerCase() !== c.toUpperCase()", "true for any letter of a script that has two cases"],
);
show(() => "7" >= "0" && "7" <= "9");
show(() => "x" >= "0" && "x" <= "9");
show(() => "7".charCodeAt(0) - 48);                     // the manual way: 7 (a number)
show(() => Number("x"));                                // NaN: not a digit
show(() => Number(" "));                                // TRAP: 0, an empty or blank string converts to 0, so Number is a bad digit test
show(() => Number.isInteger(Number(" ")));             // true: " " looks like an integer
show(() => "a".toLowerCase() !== "a".toUpperCase());    // letter
show(() => "_".toLowerCase() !== "_".toUpperCase());    // underscore is not a letter
show(() => "5".toLowerCase() !== "5".toUpperCase());    // neither is a digit

section("Case flipping");
const flip = (c: string): string => (c === c.toUpperCase() ? c.toLowerCase() : c.toUpperCase());
show(() => [..."Hello World"].map(flip).join(""));
show(() => String.fromCharCode("a".charCodeAt(0) ^ 32));   // ASCII only: bit 32 is the difference between a and A (97 vs 65)
show(() => "A".charCodeAt(0) + 32);                     // 97: lower case is 32 above upper case
const sharpS = String.fromCharCode(0xdf);               // the German sharp s
show(() => sharpS.toUpperCase());                       // TRAP: "SS", case conversion can change the LENGTH
show(() => sharpS.toUpperCase().length);

section("Building text: push and join");
syntax(
  ["const parts: string[] = []; parts.push(x);", "the StringBuilder of JavaScript: collect the pieces ..."],
  ["parts.join(\"\")", "... and join once at the end"],
);
const parts: string[] = [];
parts.push("a", String(1), "b");
show(() => parts.join(""));
show(() => [..."hello"].map((c) => c.toUpperCase()).join("-"));
note("s += x in a loop is fast in V8 (it builds a rope), but it is O(n^2) in the model an interviewer has in mind: join is the idiom");

section("Cost");
bigO(
  ["c.charCodeAt(0) | String.fromCharCode(n) | s[i]", "O(1)"],
  ["array of 26 counters", "O(n) time, O(1) space whatever n is"],
  ["Map<string, number> counting", "O(n) time, O(k) space for k distinct characters"],
  ["parts.push(x) then parts.join(\"\")", "O(n) in total: push is O(1) amortized, join copies once"],
  ["[...s].map(...).join(\"\")", "O(n) time and O(n) space: an intermediate array of characters"],
);
