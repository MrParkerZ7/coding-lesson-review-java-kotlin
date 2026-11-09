/**
 * 14b. CONSTRAINTS AND keyof: T extends ..., keyof T, T[K], constructor types
 *      Run: npx tsx src/14-generics/constraints-and-keyof.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

/** T must have a numeric length: strings, arrays and typed arrays qualify. Returns T, not just { length: number }. */
function longest<T extends { length: number }>(a: T, b: T): T {
  return a.length >= b.length ? a : b;
}

/** The return type is the constraint, so the caller loses the exact type of the argument. */
function longestLoose<T extends { length: number }>(a: T, b: T): { length: number } {
  return a.length >= b.length ? a : b;
}

/** K must be one of the keys of T, and the result is the type of THAT property. */
function getProperty<T, K extends keyof T>(obj: T, key: K): T[K] {
  return obj[key];
}

/** The value must match the type of the chosen key. */
function setProperty<T, K extends keyof T>(obj: T, key: K, value: T[K]): void {
  obj[key] = value;
}

/** Copy only the named keys: the result type is Pick<T, K>. */
function pick<T, K extends keyof T>(obj: T, ...keys: K[]): Pick<T, K> {
  const out = {} as Pick<T, K>;
  for (const key of keys) out[key] = obj[key];
  return out;
}

/** A constructor type: new () => T means "a class that can be built with no arguments". */
function create<T>(ctor: new () => T): T {
  return new ctor();
}

/** A constraint on a primitive keeps the literal type of the argument. */
function tag<T extends string>(value: T): T {
  return value;
}

/** One constraint can depend on another type parameter. */
function merge<T extends object, U extends object>(a: T, b: U): T & U {
  return { ...a, ...b };
}

class Counter {
  count = 0;
}

class Point {
  constructor(public x: number) {}
}

abstract class Shape {}

const user = { id: 1, name: "Ann", active: true };

title("GENERICS  -  constraints and keyof");

section("Constraint: T extends Type");
syntax(
  ["function f<T extends Bound>(x: T)", "T must be assignable to Bound (it may be a subtype of it)"],
  ["function f<T extends A & B>(x: T)", "several bounds: intersect them"],
);
show(() => longest("kiwi", "fig"));                     // a string has a length
show(() => longest([1, 2, 3], [4, 5]));                 // so has an array: T = number[]
show(() => longest({ length: 2 }, { length: 9 }));      // any object with a numeric length qualifies
show(() => longestLoose("ab", "c"));                    // same value, but the static type is only { length: number }
wontCompile("longest(10, 20)", "TS2345: Argument of type 'number' is not assignable to parameter of type '{ length: number; }'");
wontCompile("function f<T>(x: T) { return x.length; }", "TS2339: Property 'length' does not exist on type 'T'");
note("Without a constraint T is opaque: you can pass it around but not touch its members");
note("Return T, not the constraint: longest keeps the caller's exact type, longestLoose throws it away");

section("keyof and indexed access T[K]");
syntax(
  ["keyof T", "the union of the key names of T: keyof typeof user is 'id' | 'name' | 'active'"],
  ["T[K]", "the type of the property K of T (an indexed access type)"],
  ["function get<T, K extends keyof T>(o: T, k: K): T[K]", "the key is checked against the object, the result is typed per key"],
);
show(() => getProperty(user, "name"));                  // the result type is string
show(() => getProperty(user, "id"));                    // the result type is number: T[K] follows the key
setProperty(user, "active", false);
show(() => user.active);                                // the value had to be a boolean
show(() => pick(user, "id", "name"));                   // Pick<T, K>: { id: number; name: string }
wontCompile("getProperty(user, 'age')", "TS2345: Argument of type '\"age\"' is not assignable to parameter of type '\"id\" | \"name\" | \"active\"'");
wontCompile("setProperty(user, 'id', 'x')", "TS2345: Argument of type 'string' is not assignable to parameter of type 'number'");
note("A typo in a key is a compile error instead of a silent undefined at run time");

section("Trap: Object.keys returns string[], not (keyof T)[]");
const keys = Object.keys(user);                         // string[]: an object may have more keys than its type says
show(() => keys);
wontCompile("for (const k of Object.keys(user)) user[k];", "TS7053: Element implicitly has an 'any' type because expression of type 'string' can't be used to index");
const typedKeys = Object.keys(user) as (keyof typeof user)[];
show(() => typedKeys.map((k) => user[k]));              // the cast is safe only if you know the object has no extra keys
note("Structural typing allows extra keys at run time, so TS does not narrow Object.keys: cast it, or use a keys array you wrote");

section("Constructor types: new () => T");
syntax(
  ["function create<T>(ctor: new () => T): T", "pass the CLASS itself; T is the instance type"],
  ["new (...args: any[]) => T", "a class with any constructor arguments (the usual mixin and factory shape)"],
  ["abstract new () => T", "also accepts an abstract class (TS 4.2+), which cannot be built"],
);
const counter = create(Counter);                        // T = Counter, inferred from the class
show(() => counter);
show(() => counter instanceof Counter);                 // a real object: the class value is what exists at run time
wontCompile("create(Point)", "TS2345: Argument of type 'typeof Point' is not assignable to parameter of type 'new () => Point'");
wontCompile("create(Shape)", "TS2345: 'typeof Shape' is not assignable to 'new () => Shape' (an abstract constructor type)");
note("T alone cannot build anything (T is erased): hand over the constructor, as with Java's Class<T> token");

section("More constraint tricks");
const literal = tag("a");                               // T extends string: the type stays the literal "a", not string
show(() => literal);
show(() => merge({ a: 1 }, { b: "x" }));               // T & U: { a: number } & { b: string }
interface Config {
  port: number;
}
type ConfigAlias = { port: number };
function size<T extends Record<string, unknown>>(obj: T): number {
  return Object.keys(obj).length;
}
const asAlias: ConfigAlias = { port: 80 };
show(() => size(asAlias));                              // a type alias of an object type is assignable to Record<string, unknown>
const asInterface: Config = { port: 80 };
wontCompile("size(asInterface)", "TS2345: Argument of type 'Config' is not assignable to parameter of type 'Record<string, unknown>' (index signature missing)");
note("An interface has no implicit index signature, a type alias does: constrain with `object` when you only need an object");
