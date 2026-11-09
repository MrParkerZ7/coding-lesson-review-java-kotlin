/**
 * 13c. OBJECT FEATURES: literals, spread, freeze, structuredClone, destructuring, symbol keys
 *      Run: npx tsx src/13-classes-and-objects/object-features.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("OBJECT FEATURES  -  shorthand, spread, freeze, clone, destructuring, symbols");

section("Literal shorthand and computed keys");
syntax(
  ["{ name, age }", "property shorthand: same as { name: name, age: age }"],
  ["{ [expr]: value }", "computed key: the key is the VALUE of expr"],
  ["{ greet() { } }", "method shorthand (this is the object the method is called on)"],
);
const name = "Ann";
const age = 30;
const field = "role";
const person = { name, age, [field]: "admin", ["is" + "Adult"]: age >= 18, greet() { return `hi ${this.name}`; } };
show(() => person);
show(() => person.greet());

section("Spread vs Object.assign, and the merge order");
syntax(
  ["{ ...a, ...b }", "a NEW object: own enumerable properties of a, then b (the LATER one wins)"],
  ["Object.assign(target, a, b)", "copies into target and MUTATES it; returns the same target"],
);
const defaults = { host: "localhost", port: 80, debug: false };
const overrides = { port: 8080, debug: true };
show(() => ({ ...defaults, ...overrides }));            // the later object wins: port 8080
show(() => ({ ...overrides, ...defaults }));            // TRAP: swap the order and the defaults win
const target = { a: 1 };
const merged = Object.assign(target, { b: 2 });
show(() => merged === target);                          // true: assign returned the object it mutated
show(() => target);
show(() => Object.assign({}, defaults, overrides));     // the non-mutating form: assign into a fresh {}
const absent = undefined as { a: number } | undefined;
show(() => ({ ...absent, z: 1 }));                      // spreading null or undefined adds nothing, no error
class Point {
  constructor(public x: number, public y: number) {}
  len(): number { return Math.hypot(this.x, this.y); }
}
const point = new Point(3, 4);
const spread = { ...point };
show(() => spread);                                     // a plain Object: spread copies OWN properties only
show(() => "len" in spread);                            // the method lives on the prototype, so it is gone
show(() => spread instanceof Point);
const nested = { inner: { v: 1 } };
const shallow = { ...nested };
shallow.inner.v = 2;
show(() => nested.inner.v);                             // TRAP: spread is shallow, both objects share inner

section("Object.freeze, Readonly<T>, structuredClone");
syntax(
  ["Object.freeze(obj)", "run time: no add, delete or change; SHALLOW; throws in strict code (ES modules)"],
  ["Readonly<T>  |  as const", "compile time only: nothing is enforced when the program runs"],
  ["structuredClone(v)", "a DEEP copy of plain data, Date, Map, Set; keeps no prototype and refuses functions"],
);
const frozen = Object.freeze({ a: 1, inner: { v: 1 } });
wontCompile("frozen.a = 2", "TS2540: Cannot assign to 'a' because it is a read-only property");
const unlocked = frozen as { a: number };               // a cast gets past tsc ...
show(() => thrown(() => { unlocked.a = 2; }));          // ... but the engine refuses: TypeError, the object is frozen
frozen.inner.v = 2;                                     // compiles AND runs: the freeze stops at the first level
show(() => frozen.inner.v);
show(() => Object.isFrozen(frozen.inner));
const view: Readonly<{ n: number }> = { n: 1 };
(view as { n: number }).n = 2;
show(() => view.n);                                     // TRAP: Readonly<T> protected nothing at run time
const original = { when: new Date(Date.UTC(2026, 10, 9)), tags: new Set(["a"]), nested: { v: 1 } };
const deep = structuredClone(original);
deep.nested.v = 99;
show(() => original.nested.v);                          // a real deep copy: the original is untouched
show(() => deep.when instanceof Date);                  // Date, Set and Map survive (JSON would flatten them, lesson 18)
const clonedPoint = structuredClone(point);
show(() => clonedPoint instanceof Point);               // false: back as a plain object, the class is gone
const withFunction = { f: () => 1 };
show(() => thrown(() => structuredClone(withFunction)));   // functions cannot be cloned: a DOMException (DataCloneError)

section("Destructuring: defaults, rename, rest, nesting");
syntax(
  ["const { a, b: renamed = d, ...rest } = obj", "take a; take b under the name renamed (default d); gather the others"],
  ["const [x, , z = d, ...tail] = arr", "by position; skip with a comma; default; rest"],
);
interface Options { theme?: string; size?: number; lang?: string | null; debug?: boolean; nested: { depth: number } }
const options: Options = { theme: "dark", lang: null, debug: true, nested: { depth: 3 } };
const { theme, size: fontSize = 10, lang = "en", nested: { depth }, ...rest } = options;
show(() => [theme, fontSize, lang, depth]);             // size was missing: its default applied. lang is null: the default did NOT
show(() => rest);                                       // whatever was not named
const [first, , third = "c", ...tail] = ["a", "b", undefined, "d", "e"];
show(() => [first, third, tail]);                       // a default only replaces undefined
let left = 1;
let right = 2;
[left, right] = [right, left];                          // swap without a temporary variable
show(() => [left, right]);

section("Symbols as keys");
syntax(["{ [sym]: value }", "a symbol key is unique and hidden from Object.keys, for...in and JSON"]);
const id = Symbol("id");
const user = { name: "Ann", [id]: 7 };
show(() => user);
show(() => user[id]);
show(() => Object.keys(user));                          // the symbol key is skipped
show(() => JSON.stringify(user));                       // ... and never serialised
show(() => Reflect.ownKeys(user));                      // every own key, strings and symbols
show(() => Symbol("id") === id);                        // false: two symbols with the same description are different keys
note("Well-known symbols (Symbol.iterator, Symbol.toPrimitive, Symbol.toStringTag) hook objects into the language: lesson 2 and 11");
