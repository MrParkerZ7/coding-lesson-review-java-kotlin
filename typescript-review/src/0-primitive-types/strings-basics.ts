/**
 * 0c. STRINGS: UTF-16 text, immutable, compared by value
 *     Run: npx tsx src/0-primitive-types/strings-basics.ts        (add --plain for ASCII output)
 *     (string operations used in problems: lesson 6; formatting and templates in depth: lesson 1)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("STRINGS  -  UTF-16 text, immutable, compared by value");

section("The type and the literals");
syntax(
  ["let s: string = \"text\";", "double quotes, single quotes: the same thing"],
  ["`text ${expression}`", "backticks: a template literal, may span lines and embed expressions"],
  ["String(x) | x.toString()", "convert to text (String(null) is \"null\", null.toString() throws)"],
);
const s = "hello";  show(() => s);                       // type inferred: string
show(() => 'single' === "single");                       // the quote style does not matter
const n = 3;
show(() => `n is ${n}, double is ${n * 2}`);             // any expression inside ${ }
show(() => typeof new String("a"));                      // "object": the wrapper object, never create one
wontCompile("const t: string = new String('a');", "TS2322: Type 'String' is not assignable to type 'string' (a wrapper object)");
show(() => "a" === "a");                                 // strings are primitives: === compares the characters
show(() => new String("a") === new String("a"));         // false: two wrapper objects are compared by identity

section("UTF-16: length counts code units, not characters");
syntax(
  ["s.length", "the number of 16-bit code units"],
  ["[...s].length | Array.from(s).length", "the number of code points (iteration is by code point)"],
  ["s.codePointAt(i) | s.charCodeAt(i)", "the full code point | the 16-bit unit at i"],
);
const grin = String.fromCodePoint(0x1F600);              // an emoji, built from its code point so this file stays ASCII
show(() => s.length);                                    // 5: every letter here is one code unit
show(() => grin.length);                                 // TRAP: 2, an emoji is a surrogate PAIR of code units
show(() => [...grin].length);                            // 1: spreading walks code points
show(() => grin.codePointAt(0));                         // 128512 = 0x1F600
show(() => grin.charCodeAt(0).toString(16));             // d83d: only the first half of the pair
show(() => grin.split("").length);                       // 2: split("") cuts the pair in half
show(() => [...("a" + grin + "b")].map((c) => c.length));  // the emoji is ONE element but 2 units long
note("Reversing with split('').reverse() breaks emoji; use [...s].reverse() (lesson 6). Even that splits composed accents");

section("Indexing: no char type, strings are immutable");
syntax(
  ["s[i] | s.charAt(i)", "a one-character STRING (there is no char type); out of range: undefined | \"\""],
  ["s.at(i)", "like s[i] but a negative i counts from the end"],
);
show(() => s[0]);                                        // "h": a string of length 1, not a char
show(() => s[10]);                                       // undefined at run time, though tsc types it as string
show(() => s.charAt(10));                                // "": the empty string, never undefined
show(() => s.at(-1));                                    // "o": the last character
show(() => s.at(10));                                    // undefined
wontCompile("s[0] = 'J';", "TS2542: Index signature in type 'String' only permits reading");
const writable = s as unknown as string[];               // a cast hides it from tsc; the runtime still refuses
show(() => thrown(() => { writable[0] = "J"; }));        // TypeError in strict mode (every ES module is strict)
show(() => s.toUpperCase());                             // methods return a NEW string
show(() => s);                                           // the original is untouched

section("+ with numbers: the left-to-right trap");
syntax(
  ["text + x | x + text", "if either side is a string, + concatenates (x is converted to text)"],
  ["text - x | text * x", "numeric operators convert to number: tsc refuses them on strings"],
);
show(() => "1" + 2);                                     // "12": the number becomes text
show(() => 1 + "2");                                     // "12": either side may be the string
show(() => 1 + 2 + "3");                                 // "33": left to right, 1 + 2 is a number first
show(() => "1" + 2 + 3);                                 // "123": once a string, always a string
show(() => "1" + (2 + 3));                               // "15": parentheses force the math first
show(() => "sum: " + [1, 2]);                            // "sum: 1,2": an array becomes its join(",")
wontCompile("const n = '3' - 1;", "TS2362: the left-hand side of an arithmetic operation must be number, bigint or any");
const text = "3" as unknown as number;                    // the cast hides the string from tsc
show(() => text - 1);                                    // 2 at run time: - converts to number, + would not
wontCompile("'10' < 9", "TS2365: Operator '<' cannot be applied to types 'string' and 'number'");

section("Comparing: by UTF-16 code units, not by alphabet");
syntax(
  ["a < b | a === b", "compare code unit by code unit; === compares the whole text"],
  ["a.localeCompare(b, \"en\")", "negative | 0 | positive in the language's order: always pass the locale"],
);
show(() => "10" < "9");                                  // TRAP: true, "1" comes before "9": digits are compared as text
show(() => "a" < "B");                                   // false: every uppercase letter sorts before every lowercase one
show(() => "apple" < "apple pie");                       // true: a prefix is smaller
show(() => ["b", "a", "C"].sort());                      // [ 'C', 'a', 'b' ]: the default sort uses the code unit order
show(() => ["b", "a", "C"].sort((x, y) => x.localeCompare(y, "en")));   // alphabetical, case aside
show(() => "a".localeCompare("B", "en"));                // -1: a before B (only the sign is the contract)

section("Slicing, padding, repeating");
syntax(
  ["s.slice(from, to) | s.substring(from, to)", "the part from..to, to EXCLUDED; they differ for negative or swapped arguments"],
  ["s.padStart(n, fill) | s.padEnd(n, fill) | s.repeat(k)", "grow to n characters | repeat k times"],
);
show(() => s.slice(1, 3));                               // "el"
show(() => s.slice(-3));                                 // "llo": slice counts a negative index from the end
show(() => s.substring(-3));                             // "hello": substring turns a negative into 0
show(() => s.slice(3, 1));                               // "": slice with from > to is empty
show(() => s.substring(3, 1));                           // "el": substring SWAPS the arguments
show(() => "5".padStart(3, "0"));                        // "005": the zero-padding idiom
show(() => "ab".padEnd(5, "-"));
show(() => "ab".repeat(3));
show(() => thrown(() => "ab".repeat(-1)));              // RangeError: the count may not be negative
note("Prefer slice: one rule for negatives. Building a long text in a loop with += is fine in V8; join() is the idiom (lesson 6)");
