/**
 * 6b. REGEX BASICS: literals, flags, test, match, matchAll, groups, replace
 *     Run: npx tsx src/6-strings-in-problems/regex-basics.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("REGEX BASICS  -  patterns for parsing and cleaning text");

section("Making a RegExp");
syntax(
  ["/pattern/flags", "a literal: the backslash is written once, checked when the file is parsed"],
  ["new RegExp(\"pattern\", \"flags\")", "built from a string at run time: a backslash must be doubled"],
  ["re.source | re.flags", "the pattern text | the flags as one string"],
);
const digits = /\d+/;
show(() => digits.source);
const fromText = new RegExp("\\d+", "g");
show(() => fromText.source);                            // the same pattern, written with a doubled backslash
show(() => fromText.flags);
show(() => new RegExp("\d+").source);                   // TRAP: in a normal string "\d" is just d, so the pattern is d+
show(() => thrown(() => new RegExp("(")));              // an invalid pattern in new RegExp throws SyntaxError at run time
show(() => new RegExp("a.b").test("axb"));              // TRAP: text from a user is a PATTERN, the dot matches any character
const escapeRe = /[.*+?^${}()|[\]\\]/g;                 // the regex that escapes the special characters
const escapeText = (text: string): string => text.replace(escapeRe, "\\$&");
show(() => escapeText("a.b"));                          // turn user text into a literal pattern: backslash before each special char
show(() => new RegExp(escapeText("a.b")).test("axb"));
wontCompile("RegExp.escape(\"a.b\")", "TS2339: Property 'escape' does not exist on type 'RegExpConstructor' (Node 24 has it at run time, tsc 5.9 has no type)");

section("Flags");
syntax(
  ["g | i | m", "global (all matches) | ignore case | ^ and $ match at every line"],
  ["s | u | y", "dot matches newline | code points | sticky: match exactly at lastIndex"],
);
show(() => /abc/i.test("xABCx"));
show(() => /^b/.test("a\nb"));                          // without m: ^ is the start of the whole text
show(() => /^b/m.test("a\nb"));                         // with m: ^ is the start of each line
show(() => /a.b/.test("a\nb"));                         // the dot does not match a newline ...
show(() => /a.b/s.test("a\nb"));                        // ... unless the s flag is set
const smile = String.fromCodePoint(0x1f600);
show(() => /^.$/.test(smile));                          // false: without u the dot sees two UTF-16 units
show(() => /^.$/u.test(smile));                         // true: with u it sees one code point
const sticky = /a/y;
sticky.lastIndex = 1;
show(() => sticky.test("ba"));                          // true: an a sits exactly at index 1
sticky.lastIndex = 0;
show(() => sticky.test("ba"));                          // false: sticky does not search ahead, index 0 is b

section("test, and the lastIndex trap of g");
syntax(
  ["re.test(s)", "boolean: is there a match anywhere in s?"],
  ["re.lastIndex", "with g or y, test and exec START here and update it: the regex keeps state"],
);
show(() => /\d/.test("ab1"));
show(() => /^\d+$/.test("123"));                        // anchors ^ $ make it a whole-string test
show(() => /^\d+$/.test("12a"));
const g = /a/g;
show(() => g.test("a"));                                // true; lastIndex is now 1
show(() => g.test("a"));                                // TRAP: false, the search started at index 1 and found nothing
show(() => g.test("a"));                                // true again: a failure resets lastIndex to 0
note("Do not keep a g (or y) regex in a shared constant and call test on it: drop the g flag, or build it inside the function");

section("match and matchAll");
syntax(
  ["s.match(re)", "no g: the first match with details (index, groups) or null; with g: all matched texts or null"],
  ["s.matchAll(re)", "an iterator of full match arrays; the regex MUST have g"],
);
show(() => "ab 42 cd 7".match(/\d+/)?.[0]);            // the first match only
show(() => "ab 42 cd 7".match(/\d+/)?.index);           // where it starts
show(() => "ab 42 cd 7".match(/\d+/g));                 // with g: every match, as plain strings
show(() => "none".match(/\d+/));                        // TRAP: null, not an empty array (with g too)
show(() => "none".match(/\d+/g)?.length ?? 0);          // so write ?.length ?? 0 when counting
const found = [..."ab 42 cd 7".matchAll(/\d+/g)];
show(() => found.map((m) => m[0]));                     // matchAll is lazy; spread it to run it
show(() => found.map((m) => m.index));                  // each match still knows its position
show(() => thrown(() => "a1".matchAll(/\d/)));          // TypeError: matchAll needs the g flag (tsc does not check it)
wontCompile("const m = s.match(/\\d/); m[0]", "TS18047: 'm' is possibly 'null' (match returns RegExpMatchArray | null)");

section("Groups: parentheses capture parts");
syntax(
  ["(...) | $1", "a numbered group | its text inside a replacement"],
  ["(?<name>...) | m.groups.name | $<name>", "a named group, read back by name"],
);
const date = /(\d{4})-(\d{2})-(\d{2})/;
const parts = "on 2026-11-09 ok".match(date);
show(() => parts?.slice(0, 4));                         // index 0 is the whole match, then group 1, 2, 3
show(() => Number(parts?.[1]));                         // group 1 is the year
const named = /(?<year>\d{4})-(?<month>\d{2})/.exec("2026-11");
show(() => named?.groups?.year);                        // look a group up by name
show(() => "2026-11-09".replace(date, "$3/$2/$1"));     // $1 $2 $3 reorder the captured parts
show(() => "2026-11".replace(/(?<y>\d{4})-(?<m>\d{2})/, "$<m>.$<y>"));
show(() => "a1".match(/(x)?a/)?.[1]);                   // a group that did not take part is undefined

section("replace with a pattern and a function");
show(() => "ab 42 cd 7".replace(/\d+/g, "#"));
show(() => "ab 42 cd 7".replace(/\d+/g, (d) => String(Number(d) * 2)));   // compute each replacement from its match
show(() => "Hello, World 42!".replace(/[^A-Za-z0-9]/g, ""));              // drop everything except letters and digits
show(() => "Hello, World".replace(/[aeiou]/gi, "*"));
show(() => "one  two   three".split(/\s+/));            // split on runs of whitespace
show(() => "hello world".replace(/\b\w/g, (c) => c.toUpperCase()));       // capitalize every word: \b is a word boundary
note("Common pieces: \\d digit, \\w word char, \\s space, . any char, + one or more, * zero or more, ? optional, ^ start, $ end");

section("Greedy versus lazy");
syntax(["+ * ? {n,m}", "greedy: take as much as possible"], ["+? *? ??", "lazy: take as little as possible"]);
show(() => "<a><b>".match(/<.+>/)?.[0]);               // TRAP: greedy runs to the LAST >
show(() => "<a><b>".match(/<.+?>/)?.[0]);               // lazy stops at the first >
show(() => "<a><b>".match(/<[^>]+>/g));                 // or exclude the end character: usually the clearest

section("Cost");
bigO(
  ["test | match | replace on a simple pattern", "O(n) in the text"],
  ["nested quantifiers such as (a+)+ on bad input", "can backtrack exponentially (ReDoS): keep patterns simple"],
  ["new RegExp(...) inside a loop", "compiles the pattern each time: build it once, outside the loop"],
  ["a shared /g regex used with test | exec", "carries lastIndex between calls: a correctness bug, not just a cost"],
);
