/**
 * 14a. GENERIC BASICS: type parameters on functions, interfaces, type aliases and classes
 *      Run: npx tsx src/14-generics/generic-basics.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

/** A generic function: T is filled in from the argument. */
function identity<T>(x: T): T {
  return x;
}

/** T[] in, T | undefined out: an empty array has no first element. */
function first<T>(items: T[]): T | undefined {
  return items[0];
}

/** Two type parameters: the pair keeps both types. */
function pair<A, B>(a: A, b: B): [A, B] {
  return [a, b];
}

/** T is inferred from the array, R from what the callback returns. */
function mapAll<T, R>(items: T[], fn: (item: T) => R): R[] {
  const result: R[] = [];
  for (const item of items) result.push(fn(item));
  return result;
}

/** Both arguments must have the same T. */
function same<T>(a: T, b: T): boolean {
  return a === b;
}

interface Box<T> {
  value: T;
}

type Pair<A, B = A> = { left: A; right: B };      // B defaults to A when it is left out

/** A generic class: the type argument is chosen where the class is used. */
class Wrapper<T> {
  constructor(readonly value: T) {}
  map<R>(fn: (v: T) => R): Wrapper<R> {
    return new Wrapper(fn(this.value));
  }
}

/** A default type parameter is used when nothing can be inferred. */
class Store<T = string> {
  items: T[] = [];
  add(item: T): void {
    this.items.push(item);
  }
}

/** No default and nothing to infer from: T becomes unknown. */
class Bag<T> {
  items: T[] = [];
  add(item: T): void {
    this.items.push(item);
  }
}

title("GENERICS  -  basics: one definition, many types");

section("Generic function: function name<T>(x: T): T");
syntax(
  ["function name<T>(x: T): T { ... }", "T is declared in angle brackets AFTER the function name"],
  ["name(x)  |  name<Type>(x)", "T inferred from the argument | T written out"],
  ["function name<A, B>(a: A, b: B)", "several type parameters, separated by commas"],
);
show(() => identity(42));                               // T is inferred as 42 (a literal) here, the value comes back unchanged
show(() => identity<string>("kotlin"));                 // explicit type argument: usually not needed
show(() => first([10, 20, 30]));                        // T = number, the result type is number | undefined
show(() => first([]));                                  // an empty array has no first element: undefined
show(() => pair("id", 7));                              // [string, number]: both types are kept
show(() => mapAll([1, 2, 3], (n) => `n=${n}`));         // T = number, R = string: R comes from the callback
wontCompile("identity<string>(5)", "TS2345: Argument of type 'number' is not assignable to parameter of type 'string'");
wontCompile("same(1, 'a')", "TS2345: Argument of type '\"a\"' is not assignable to parameter of type '1'");
note("A type parameter means 'the SAME type everywhere it appears', not 'any type': same(1, 2) is fine, same(1, 'a') is not");

section("Inference widens differently for const and let");
const kept = identity("hi");                            // type of kept: the literal "hi"
let widened = identity("hi");                           // type of widened: string (a let may change later)
widened = "other";
show(() => kept);                                       // the const keeps the literal type "hi"
show(() => widened);                                    // the let was widened to string, so a new string is allowed
note("The type is gone at run time (see erasure-and-variance); only the values are printed here");

section("Generic interface, type alias and class");
syntax(
  ["interface Box<T> { value: T }", "generic interface: use it as Box<number>"],
  ["type Pair<A, B = A> = { left: A; right: B }", "generic type alias; B = A is a default type parameter"],
  ["class Wrapper<T> { constructor(readonly value: T) {} }", "generic class: the type argument is chosen per use"],
);
const box: Box<number> = { value: 5 };                  // Box<number> is just the shape { value: number }
show(() => box);
const both: Pair<string> = { left: "a", right: "b" };   // B was left out, so it is the default: string
show(() => both);
const wrapped = new Wrapper(42);                        // T inferred from the constructor argument
show(() => wrapped);
const mapped = wrapped.map((n) => `n=${n}`);            // Wrapper<number> -> Wrapper<string>: map changes T
show(() => mapped);
wontCompile("const b: Box<string> = { value: 1 };", "TS2322: Type 'number' is not assignable to type 'string'");
wontCompile("const b: Box = { value: 1 };", "TS2314: Generic type 'Box<T>' requires 1 type argument(s)");

section("Default type parameters and explicit type arguments");
syntax(
  ["class Store<T = string>", "T falls back to string when it cannot be inferred"],
  ["new Set<number>() | new Map<string, number>()", "write the type argument when there is nothing to infer from"],
);
const store = new Store();                              // nothing to infer from: T is the default, string
store.add("ok");
show(() => store.items);
wontCompile("store.add(1);", "TS2345: Argument of type 'number' is not assignable to parameter of type 'string'");
const bag = new Bag();                                  // no default: T is unknown, so anything goes in
bag.add(1);
bag.add("two");
show(() => bag.items);                                  // unknown[]: you must narrow each item before you use it
const ids = new Set<number>();                          // without <number> this is a Set<unknown>
ids.add(1);
ids.add(1);
show(() => ids);                                        // a Set keeps one copy; the type argument fixed the element type
note("new Map() with no arguments is Map<any, any> (no checking at all): always write new Map<K, V>()");
note("Generics cost nothing at run time: the type parameters are erased, one function serves every T");
