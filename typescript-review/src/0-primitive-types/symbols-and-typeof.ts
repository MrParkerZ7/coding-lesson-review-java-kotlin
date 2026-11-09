/**
 * 0e. TYPEOF, INSTANCEOF AND SYMBOL: asking what a value is
 *     Run: npx tsx src/0-primitive-types/symbols-and-typeof.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

declare const notDeclared: unknown; // tsc believes it exists; at run time there is no such variable

const KEY: unique symbol = Symbol("key");
type Tagged = { [KEY]: number; name: string };

class Dog {}

/** typeof extended with the two cases it gets wrong: null and arrays. */
function kindOf(x: unknown): string {
  if (x === null) return "null";
  if (Array.isArray(x)) return "array";
  return typeof x;
}

/** typeof narrows an unknown inside each branch, so x has the right type there. */
function describe(x: unknown): string {
  if (typeof x === "string") return `string of length ${x.length}`;
  if (typeof x === "number") return `number ${x.toFixed(1)}`;
  return "something else";
}

/** instanceof with a constructor passed as a value: tsc allows an unknown left side here. */
function isA(value: unknown, ctor: Function): boolean {
  return value instanceof ctor;
}

title("TYPEOF, INSTANCEOF AND SYMBOL  -  what is this value?");

section("typeof: eight possible answers");
syntax(
  ["typeof x", "one of: undefined boolean number bigint string symbol function object"],
  ["typeof x === \"string\"", "the check TS understands: it narrows x in the branch (a typo is TS2367)"],
);
show(() => typeof undefined);
show(() => typeof true);
show(() => typeof 42);
show(() => typeof NaN);                                  // "number": NaN is a number value
show(() => typeof 10n);
show(() => typeof "text");
show(() => typeof Symbol());
show(() => typeof (() => 1));                            // "function": the only object type typeof names separately
show(() => typeof Dog);                                  // "function": a class is a function
show(() => typeof null);                                 // TRAP: "object", a bug from the first JavaScript version, kept for compatibility
show(() => typeof []);                                   // "object": arrays have no typeof of their own
show(() => typeof { a: 1 });
show(() => typeof new Map());                            // "object" for every other built-in object too
wontCompile("typeof x === 'strnig'", "TS2367: this comparison appears to be unintentional (no overlap with the 8 results)");

section("Telling null, arrays and plain objects apart");
syntax(
  ["Array.isArray(x)", "the reliable array test (instanceof fails across frames and realms)"],
  ["x === null", "the only null test; typeof x === \"object\" alone is not enough"],
);
show(() => Array.isArray([]));
show(() => Array.isArray({ length: 0 }));               // false: an array-like is not an array
show(() => Array.isArray(new Uint8Array(1)));            // false: a typed array is not an Array
show(() => kindOf(null));
show(() => kindOf([1]));
show(() => kindOf({}));
show(() => Object.prototype.toString.call(null));        // the old catch-all: "[object Null]"
show(() => Object.prototype.toString.call([]));
show(() => describe("abc"));                             // typeof narrowed x to string, so x.length compiles
show(() => describe(3));
show(() => describe(null));
wontCompile("typeof reallyUndeclared", "TS2304: Cannot find name (tsc knows every name; plain JavaScript would answer \"undefined\")");
show(() => typeof notDeclared);                          // "undefined": typeof never throws, even for a name that does not exist
show(() => thrown(() => notDeclared));                   // ReferenceError: reading the same name does
note("In plain JavaScript typeof undeclaredName is the safe feature test; in TypeScript you declare the name first");

section("instanceof: is this object made by that constructor?");
syntax(["obj instanceof Ctor", "true when Ctor.prototype is on obj's prototype chain; the left side must be an object"]);
show(() => new Dog() instanceof Dog);
show(() => [] instanceof Array);
show(() => [] instanceof Object);                        // true: arrays inherit from Object too
show(() => new Date(0) instanceof Date);
show(() => isA(Object.create(null), Object));            // false: no prototype chain at all
show(() => isA("text", String));                         // false: a primitive is not an instance of anything
show(() => isA(new String("text"), String));             // true: only the wrapper object is
wontCompile("'text' instanceof String", "TS2358: left-hand side of instanceof must be any, an object type or a type parameter");
note("typeof for primitives, instanceof for objects made by a constructor, Array.isArray for arrays (prototype chain: lesson 11)");

section("Symbol: a value that is unique");
syntax(
  ["Symbol(\"description\")", "a new unique value every call; the text is only a label"],
  ["Symbol.for(\"key\")", "one shared symbol per key, from a global registry"],
  ["const K: unique symbol = Symbol();", "a type that names exactly this one symbol"],
);
const id = Symbol("id");
show(() => id.description);                              // the label only: it does not make the symbol equal to another
show(() => Symbol("id") === Symbol("id"));               // false: every call makes a new one
show(() => Symbol.for("app") === Symbol.for("app"));     // true: the registry returns the same symbol
show(() => Symbol.keyFor(Symbol.for("app")));            // "app": the registry key
show(() => id.toString());
show(() => String(id));                                  // explicit conversion works
const asText = id as unknown as string;                  // tsc forbids the implicit one (TS2731), the cast hides it
show(() => thrown(() => "id: " + asText));              // TypeError: a symbol never converts to text by itself
const tagged: Tagged = { [KEY]: 7, name: "x" };
show(() => tagged[KEY]);                                 // a symbol key hides a property from accidental clashes
show(() => Object.keys(tagged));                         // [ 'name' ]: symbol keys are skipped by keys, for...in and JSON
show(() => JSON.stringify(tagged));
show(() => Object.getOwnPropertySymbols(tagged));        // the only way to list them
wontCompile("const B: typeof KEY = Symbol('key');", "TS2322: 'unique symbol' types accept only that one symbol");
note("Built-in symbols name protocols: Symbol.iterator makes an object work in for...of (lesson 2, iterators)");
