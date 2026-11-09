/**
 * 6a. STRING OPERATIONS: split, slice, search, replace, transform
 *     (the string type itself is lesson 0 strings-basics; regular expressions are 6b regex-basics)
 *     Run: npx tsx src/6-strings-in-problems/string-operations.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("STRING OPERATIONS  -  the tools of every string problem");
note("A string is immutable: every operation below returns a NEW string and leaves the original alone");

const s = "Hello, TypeScript";

section("split and join");
syntax(
  ["s.split(separator, limit?)", "string[]; a string separator is plain text, a RegExp is a pattern"],
  ["s.split(\"\")", "one piece per UTF-16 unit (see the emoji trap below)"],
  ["xs.join(separator)", "the reverse: array -> string (default separator is a comma)"],
);
show(() => "a,b,c".split(","));
show(() => "a,,b".split(","));                         // an empty piece stays in the array
show(() => "a, b;c".split(/[,;]\s*/));                  // a RegExp separator: comma or semicolon plus spaces
show(() => "a,b,c,d".split(",", 2));                    // TRAP: the limit CUTS the result, the rest is lost (Kotlin keeps it)
show(() => "a.b.c".split("."));                         // "." is plain text here, so this works
show(() => "  a  b ".split(" "));                       // TRAP: every single space splits, empty pieces appear
show(() => "  a  b ".trim().split(/\s+/));              // trim, then split on runs of whitespace
show(() => "".split(",").length);                       // 1: ONE empty string, not an empty array
show(() => "".split("").length);                        // 0: splitting on "" gives no pieces
const digitsSplit = /(\d)/;
show(() => "a1b2c".split(digitsSplit));                 // a capture group puts the separators in the result
show(() => [1, null, undefined, 2].join(","));          // null and undefined become empty text
show(() => "a-b-c".split("-").join("+"));

section("Slicing: slice, substring, the old substr");
syntax(
  ["s.slice(start, end?)", "end EXCLUDED; a negative index counts from the end; never throws"],
  ["s.substring(start, end?)", "end EXCLUDED; negatives become 0 and the two arguments SWAP when start > end"],
  ["s.substr(start, length)", "deprecated: the second argument is a LENGTH; do not use"],
);
show(() => s.slice(7));
show(() => s.slice(0, 5));                              // indexes 0..4
show(() => s.slice(-10));                               // the last 10 characters
show(() => s.slice(-10, -4));                           // from the end to just before the 4th from the end
show(() => s.slice(5, 2));                              // start > end: the empty string
show(() => s.substring(5, 2));                          // TRAP: substring swaps them, so this is slice(2, 5)
show(() => s.substring(-3).length);                     // TRAP: a negative start becomes 0, so the whole string (17)
show(() => s.slice(5, 99));                             // out of range is clamped, no exception
show(() => s.substr(7, 4));                             // old API: start 7, LENGTH 4 (slice(7, 11) is the same)
note("Prefer slice: one rule (negatives count from the end), same name as the array method");

section("Search and test");
syntax(
  ["s.indexOf(x, from?) | s.lastIndexOf(x)", "first | last index, or -1 when missing"],
  ["s.includes(x) | s.startsWith(x) | s.endsWith(x)", "boolean tests"],
  ["s.at(i)", "like s[i] but a negative i counts from the end"],
);
show(() => s.includes("Type"));
show(() => s.includes("type"));                         // case-sensitive
show(() => s.startsWith("Hell"));
show(() => s.endsWith("pt"));
show(() => s.indexOf("T"));                             // first match
show(() => s.lastIndexOf("t"));                         // last match
show(() => s.indexOf("t", 8));                          // search from index 8
show(() => s.indexOf("xyz"));                           // -1 when missing, never an exception
show(() => "abc".indexOf("a") ? "found" : "missing");   // TRAP: the index is 0, and 0 is falsy: test !== -1
show(() => "abc".indexOf(""));                          // the empty string is found at 0
show(() => s.at(-1));
show(() => s[99]);                                      // out of range: undefined, no exception
show(() => "".at(0));

section("replace is FIRST match only");
syntax(
  ["s.replace(pattern, replacement)", "a string pattern replaces only the FIRST match"],
  ["s.replaceAll(pattern, replacement)", "every match; a RegExp must have the g flag"],
  ["s.replace(/x/g, (match) => ...)", "a function builds each replacement"],
);
show(() => "banana".replace("a", "o"));                 // TRAP: only the first a (Kotlin and Java replace them all)
show(() => "banana".replaceAll("a", "o"));
show(() => "banana".replace(/a/g, "o"));                // the g flag: every match, the older way
show(() => thrown(() => "banana".replaceAll(/a/, "o")));   // replaceAll with a RegExp lacking g throws
show(() => "a.b.c".replace(".", "-"));                  // a string pattern is literal text, not a regex
show(() => "a.b.c".replace(/./g, "-"));                 // TRAP: as a RegExp "." matches every character
show(() => "price".replace("price", "$&!"));            // TRAP: $& in the replacement text means "the match": prints price!
show(() => "ab".replace("b", () => "$&"));              // a function result is used as is: no $ rules there

section("Case, trim, repeat, pad, normalize");
show(() => s.toLowerCase());
show(() => s.toUpperCase());
show(() => "  hi \n".trim());                           // whitespace on both ends, newline included
show(() => "  hi ".trimStart() + "|");
show(() => "ab".repeat(3));
show(() => "7".padStart(3, "0"));
show(() => "ab".padEnd(5, "."));
show(() => "a\nb\r\nc".split(/\r?\n/));                 // no lines(): split on a pattern that accepts both endings
const composed = String.fromCharCode(0xe9);             // e with an acute accent as ONE code unit
const decomposed = "e" + String.fromCharCode(0x301);    // the letter e plus a combining accent
show(() => composed === decomposed);                    // false: they look the same, the code units differ
show(() => composed.normalize("NFD") === decomposed);   // true: normalize makes them comparable
show(() => [composed.length, decomposed.length]);

section("Characters, code points and the emoji trap");
const smile = String.fromCodePoint(0x1f600);            // one emoji, built from its code point to keep the source ASCII
show(() => smile.length);                               // 2: length counts UTF-16 code units
show(() => [...smile].length);                          // 1: spreading walks code points
show(() => smile.split("").length);                     // 2: split("") cuts the emoji into two broken halves, like length
show(() => smile.codePointAt(0));                     // the whole code point (128512)
show(() => smile.charCodeAt(0));                        // only the first half, a surrogate (55357)
show(() => [..."a" + smile + "b"].length);              // 3 characters a user sees, but length says 4
wontCompile("s.reverse()   (s: string)", "TS2339: Property 'reverse' does not exist on type 'string'");
wontCompile("s.sorted()    (s: string)", "TS2339: Property 'sorted' does not exist on type 'string'");
const word = "hello";
show(() => [...word].reverse().join(""));               // reverse a string: spread, reverse the array, join
show(() => [...word].sort().join(""));                  // sorting the letters of a word: the anagram key

section("Comparing text");
show(() => "abc" < "abd");                              // the first different code unit decides
show(() => "ab" < "abc");                               // a prefix is smaller than the longer text
show(() => "10" < "9");                                 // TRAP: text order compares char by char, "1" < "9"
show(() => "a" < "B");                                  // false: "a" is 97, "B" is 66, upper case sorts first
show(() => ["10", "9", "2"].sort());                    // lexicographic
const byLocale = (x: string, y: string): number => x.localeCompare(y, "en");
show(() => ["b", "a", "C"].sort(byLocale));             // locale order: a, b, C (always pass the locale)
note("Numbers inside text: sort with (a, b) => Number(a) - Number(b) (lesson 3)");

section("Cost");
bigO(
  ["s[i] | s.at(i) | s.length", "O(1)"],
  ["slice | substring | toUpperCase | trim | repeat | s + t", "O(n): a new string (V8 may share memory, but plan for a copy)"],
  ["includes | indexOf | startsWith", "O(n) typical, O(n * m) worst case for a pattern of length m"],
  ["s === t | localeCompare", "O(n): up to the first difference"],
  ["split | join | [...s] | replaceAll", "O(n) time and O(n) space"],
  ["s += x in a loop", "fine in V8 (it builds a rope), but pieces.join(\"\") is the portable idiom"],
);
