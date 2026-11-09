/**
 * 1d. EQUALITY AND IDENTITY: == vs ===, Object.is, objects by reference, copies
 *     Run: npx tsx src/1-variables-and-types/equality-and-identity.ts        (add --plain for ASCII output)
 */
import { inspect, isDeepStrictEqual } from "node:util";
import { block, note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** The loose comparison with both sides typed unknown: tsc rejects most mixed-type == on purpose. */
function loose(a: unknown, b: unknown): boolean {
  return a == b;
}

/** The strict comparison with both sides typed unknown, to put the two operators side by side in a table. */
function strict(a: unknown, b: unknown): boolean {
  return a === b;
}

class Point {
  x = 1;
}

title("EQUALITY AND IDENTITY  -  == converts, === does not, objects are references");

section("== vs ===");
syntax(
  ["a === b | a !== b", "strict: same type AND same value, no conversion (use this)"],
  ["a == b | a != b", "loose: converts the sides to a common type first (surprising)"],
  ["Object.is(a, b)", "like === except NaN equals NaN and 0 differs from -0"],
);
show(() => 1 === 1);
show(() => loose(1, "1"));                               // true: the text is converted to a number first
show(() => strict(1, "1"));                              // false: different types are never ===
show(() => loose(null, undefined));                      // true: the one pair worth knowing (x == null tests both, lesson 7)
show(() => strict(null, undefined));                     // false
wontCompile("1 == '1'", "TS2367: this comparison appears to be unintentional (types 'number' and 'string' have no overlap)");
wontCompile("'a' === 1", "TS2367: this comparison appears to be unintentional (types 'string' and 'number' have no overlap)");

section("The == table (built at run time)");
const pairs: [unknown, unknown][] = [
  [0, ""], [0, "0"], ["", "0"], [false, "0"], [false, ""], [null, 0], [null, false], [undefined, 0],
  [NaN, NaN], [[], false], [[1], 1], [[1, 2], "1,2"], [{}, "[object Object]"], [true, "true"], ["1", true],
];
const cell = (v: unknown, width: number): string => inspect(v).padEnd(width);
const rows = ["a".padEnd(11) + "b".padEnd(20) + "a == b".padEnd(8) + "a === b"];
for (const [a, b] of pairs) rows.push(cell(a, 11) + cell(b, 20) + cell(loose(a, b), 8) + String(strict(a, b)));
block(rows.join("\n"));
note("== is not transitive: 0 == '' and 0 == '0' are true, yet '' == '0' is false. Always write ===");
note("[] == false is true although [] is truthy: == does not use truthiness (lesson 11 explains the steps)");

section("Objects and arrays: compared by reference");
const first = { n: 1 };
const second = { n: 1 };
const sameAsFirst = first;
show(() => first === second);                            // false: two objects with equal contents are still two objects
show(() => first === sameAsFirst);                       // true: the same object under two names
wontCompile("{} === {}", "TS2839: this condition will always return 'false': objects compare by reference");
wontCompile("[1] == [1]", "TS2839: this condition will always return 'false': objects compare by reference");
show(() => isDeepStrictEqual({ a: 1, b: [1] }, { a: 1, b: [1] }));   // node:util compares the contents, recursively
show(() => isDeepStrictEqual({ a: 1 }, { a: "1" }));     // false: strict about types too
show(() => JSON.stringify({ a: 1, b: 2 }) === JSON.stringify({ b: 2, a: 1 }));   // TRAP: false, the key order differs
note("No built-in value equality for objects: write equals(), compare fields, or use isDeepStrictEqual (lesson 13)");

section("NaN, -0 and Object.is");
const nan = Number("abc");
show(() => nan === nan);                                 // false: NaN is the one value not equal to itself
show(() => Object.is(nan, NaN));                         // true
show(() => Number.isNaN(nan));                           // the usual test
show(() => 0 === -0);                                    // true
show(() => Object.is(0, -0));                            // false
show(() => [NaN].includes(NaN));                         // true: includes uses SameValueZero
show(() => [NaN].indexOf(NaN));                          // -1: indexOf uses ===, so it never finds NaN
show(() => new Set([NaN, NaN]).size);                    // 1: Set and Map also use SameValueZero

section("Copies: alias, shallow, deep");
syntax(
  ["const b = a;", "an ALIAS: one object, two names"],
  ["{ ...a } | [...a] | Object.assign({}, a)", "a SHALLOW copy: new top level, the nested objects are shared"],
  ["structuredClone(a)", "a DEEP copy: nested objects copied too"],
);
const original = { name: "Ann", tags: ["a"] };
const alias = original;
alias.name = "Zed";
show(() => original.name);                               // "Zed": the alias changed the original
const shallow = { ...original };
shallow.name = "Bob";
show(() => original.name);                               // still "Zed": a top-level field of a copy is its own
shallow.tags.push("b");
show(() => original.tags);                               // TRAP: [ 'a', 'b' ], the nested array is shared
const deep = structuredClone(original);
deep.tags.push("c");
show(() => original.tags);                               // unchanged: the deep copy has its own array
show(() => deep.tags);
show(() => structuredClone(new Date(0)) instanceof Date);   // true: Date, Map, Set, arrays survive
show(() => structuredClone(new Point()) instanceof Point);  // false: a class instance becomes a plain object
show(() => thrown(() => structuredClone({ f: () => 1 })));  // DOMException (DataCloneError): functions cannot be cloned
