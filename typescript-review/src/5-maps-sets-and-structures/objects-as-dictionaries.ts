/**
 * 5c. OBJECTS AS DICTIONARIES: Record<string, V>, key order, and the prototype traps
 *     Run: npx tsx src/5-maps-sets-and-structures/objects-as-dictionaries.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

title("OBJECTS AS DICTIONARIES  -  Record<string, number> and its traps");

section("Typing a dictionary");
syntax(
  ["Record<string, number>  |  { [key: string]: number }", "any number of string keys, every value a number"],
  ["Record<'a' | 'b', number>", "EXACTLY these keys, all required (a mapped type, not a dictionary)"],
  ["Partial<Record<K, V>>", "keys of a union type, each one optional"],
);
const scores: Record<string, number> = { ann: 3, bob: 5 };
scores.cid = 4;  show(() => scores);                    // add a key: dot or bracket, scores["cid"] = 4 is the same
scores["ann"] = 10;  show(() => scores.ann);            // overwrite
show(() => scores["zed"]);                              // undefined at run time, though the type says number
wontCompile("const r: Record<'a' | 'b', number> = { a: 1 }", "TS2741: property 'b' is missing (a union key type needs every key)");
wontCompile("const n: number = scores['zed']", "only with --noUncheckedIndexedAccess: number | undefined (TS2322)");
note("Without that flag a missing key is typed as a number: check with `in`, Object.hasOwn or ?? before you use it");
show(() => scores["zed"] ?? 0);                         // the usual default

section("Keys, values, entries, delete");
show(() => Object.keys(scores));                        // own enumerable string keys, as an array
show(() => Object.values(scores));
show(() => Object.entries(scores));                     // [key, value] pairs
show(() => Object.keys(scores).length);                 // there is no .size: count the keys
show(() => delete scores.cid);                          // true: removes the property
show(() => delete scores.nope);                         // also true: deleting a missing key is not an error
show(() => Object.fromEntries(Object.entries(scores).map(([k, v]) => [k, v * 2])));   // transform every value
show(() => ({ ...scores, ann: 0, dan: 7 }));            // spread copies; a later key wins; the original is untouched

section("in vs Object.hasOwn");
syntax(
  ["key in obj", "true for OWN and INHERITED properties (toString, constructor ...)"],
  ["Object.hasOwn(obj, key)", "true only for the object's own properties (ES2022); replaces obj.hasOwnProperty(key)"],
  ["obj[key] !== undefined", "also false for a key that exists with the value undefined"],
);
const empty: Record<string, number> = {};
show(() => "toString" in empty);                        // TRAP: true, inherited from Object.prototype
show(() => Object.hasOwn(empty, "toString"));           // false: the right question
show(() => typeof empty["toString"]);                   // "function": TS says number, the run time says function
show(() => "ann" in scores);
show(() => Object.hasOwn({ a: undefined }, "a"));       // true: the key exists, its value is undefined

section("Key order: integer-like keys come FIRST");
show(() => Object.keys({ b: 1, 2: 1, a: 1, 1: 1 }));    // TRAP: ['1', '2', 'b', 'a']: integer-like keys ascending, then the rest as inserted
show(() => Object.keys({ "10": 1, "9": 1, a: 1 }));     // '9' before '10': numeric, not text order
show(() => Object.keys({ "01": 1, "-1": 1, "1.5": 1 }));   // only canonical non-negative integers count: these stay in insertion order
note("For a dictionary whose order matters (ids like '10', '9'), use a Map: it keeps insertion order for every key type");

section("TRAP: keys that collide with Object.prototype");
const counts: Record<string, number> = {};
for (const w of ["a", "constructor", "a"]) counts[w] = (counts[w] ?? 0) + 1;
show(() => counts.a);                                   // 2
show(() => typeof counts.constructor);                  // "string": (Object function) + 1 built a STRING, not a count
const polluted: Record<string, number> = {};
polluted["__proto__"] = 5;
show(() => Object.keys(polluted));                      // []: assigning a non-object to __proto__ is silently ignored
const safe: Record<string, number> = Object.create(null);   // no prototype at all
for (const w of ["a", "constructor", "__proto__", "a"]) safe[w] = (safe[w] ?? 0) + 1;
show(() => safe);                                       // a: 2, constructor: 1, __proto__: 1: every key is just a key
show(() => "toString" in safe);                         // false: nothing inherited
show(() => Object.getPrototypeOf(safe));                // null
note("Three fixes: Object.create(null), a Map, or check Object.hasOwn(obj, key) before reading. Counting words from user input: use a Map");

section("Cost");
bigO(
  ["obj[key] read | write | in | hasOwn", "O(1) on average"],
  ["Object.keys | values | entries | spread", "O(n): a new array or object"],
  ["delete obj[key]", "O(1) on average (a Map is the cleaner tool when keys come and go often)"],
  ["a dictionary of n keys", "O(n) space; a Map has the same cost"],
);
