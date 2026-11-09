/**
 * 15b. MAPPED AND CONDITIONAL TYPES: [K in keyof T], -readonly, -?, as, T extends U ? X : Y, infer,
 *      template literal types, recursive DeepReadonly
 *      Run: npx tsx src/15-utility-and-advanced-types/mapped-and-conditional.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

interface User {
  readonly id: number;
  name: string;
  email?: string;
}

// ---- mapped types: build a new object type from the keys of another one
type Flags<T> = { [K in keyof T]: boolean };
type Mutable<T> = { -readonly [K in keyof T]: T[K] };
type Concrete<T> = { [K in keyof T]-?: T[K] };
type Getters<T> = { [K in keyof T as `get${Capitalize<string & K>}`]: () => T[K] };
type OnlyStrings<T> = { [K in keyof T as T[K] extends string ? K : never]: T[K] };

// ---- conditional types: a type-level if
type IsString<T> = T extends string ? "yes" : "no";
type ToArray<T> = T extends unknown ? T[] : never;       // distributes over a union
type ToArrayAll<T> = [T] extends [unknown] ? T[] : never;   // the tuple wrapper switches distribution off
type ElementOf<T> = T extends readonly (infer E)[] ? E : never;
type Head<T extends unknown[]> = T extends [infer H, ...unknown[]] ? H : never;
type MyReturn<F> = F extends (...args: never[]) => infer R ? R : never;
type Unwrap<T> = T extends Promise<infer U> ? Unwrap<U> : T;   // recursive: unwraps any depth

// ---- template literal types
type EventName<T extends string> = `on${Capitalize<T>}`;
type Dir = `${"left" | "right"}-${"top" | "bottom"}`;
type Route = `/${string}`;

// ---- recursive mapped type
type DeepReadonly<T> = T extends (...args: never[]) => unknown
  ? T
  : T extends object
    ? { readonly [K in keyof T]: DeepReadonly<T[K]> }
    : T;

/** The run-time twin of DeepReadonly: the type promises it, this makes it true. */
function deepFreeze<T>(value: T): DeepReadonly<T> {
  if (typeof value === "object" && value !== null) {
    for (const child of Object.values(value)) deepFreeze(child);
    Object.freeze(value);
  }
  return value as DeepReadonly<T>;
}

/** Types only describe getters: this builds them at run time, then casts to the computed type. */
function makeGetters<T extends object>(source: T): Getters<T> {
  const result: Record<string, () => unknown> = {};
  for (const [key, value] of Object.entries(source)) {
    result[`get${key.charAt(0).toUpperCase()}${key.slice(1)}`] = () => value;
  }
  return result as Getters<T>;
}

title("ADVANCED TYPES  -  mapped, conditional, template literal");

section("Mapped types: { [K in keyof T]: ... }");
syntax(
  ["{ [K in keyof T]: V }", "same keys as T, new value type V (optional and readonly are copied from T)"],
  ["{ -readonly [K in keyof T]: T[K] } | { [K in keyof T]-?: T[K] }", "-readonly removes readonly | -? removes optional (+ adds them)"],
  ["{ [K in keyof T as NewKey]: T[K] }", "key remapping: rename a key, or map it to never to drop it"],
);
const flags: Flags<User> = { id: true, name: false };   // email stays optional: a mapped type over keyof T copies the modifiers
show(() => flags);
const loose: Mutable<User> = { id: 1, name: "Ann" };
loose.id = 2;                                           // fine: -readonly removed the readonly on id
show(() => loose);
const strict: Concrete<User> = { id: 1, name: "Ann", email: "ann@x.io" };   // -? made email mandatory
show(() => strict);
const getters = makeGetters({ name: "Ann", age: 30 });  // Getters<{ name: string; age: number }>
show(() => getters.getName());                          // keys renamed by the template: name -> getName
show(() => getters.getAge());
const texts: OnlyStrings<{ a: string; b: number; c: string }> = { a: "x", c: "y" };
show(() => texts);                                      // b was mapped to never, so it is dropped from the type
wontCompile("const f: Flags<User> = { id: true };", "TS2741: Property 'name' is missing in type '{ id: true; }' but required in type 'Flags<User>'");
wontCompile("const c: Concrete<User> = { id: 1, name: 'A' };", "TS2741: Property 'email' is missing in type ... but required in type 'Concrete<User>'");
wontCompile("getters.getEmail()", "TS2339: Property 'getEmail' does not exist on type 'Getters<{ name: string; age: number; }>'");
note("Partial, Required, Readonly, Pick and Record from the last file are exactly such mapped types");

section("Conditional types: T extends U ? X : Y");
syntax(
  ["T extends U ? X : Y", "X if T is assignable to U, else Y (a type-level if)"],
  ["T extends Promise<infer U> ? U : T", "infer introduces a type variable that TS fills in from the match"],
  ["[T] extends [U] ? X : Y", "wrapping in a tuple switches OFF distribution over unions"],
);
const no: IsString<number> = "no";
const yes: IsString<"x"> = "yes";
show(() => [no, yes]);                                  // values of the computed types: the answer is a type, not a runtime value
const one: ToArray<string | number> = ["x"];            // distributive: (string -> string[]) | (number -> number[])
show(() => one);
const mix: ToArrayAll<string | number> = ["x", 1];      // not distributive: one array of the whole union
show(() => mix);
const first: Head<[string, number]> = "head";           // infer H takes the first tuple element
show(() => first);
const ret: MyReturn<() => number> = 42;                 // ReturnType rebuilt in one line
show(() => ret);
const flat: Unwrap<Promise<Promise<number>>> = 7;       // recursion unwraps every level
show(() => flat);
const item: ElementOf<string[]> = "item";               // infer E from string[]
show(() => item);
wontCompile("const a: IsString<number> = 'yes';", "TS2322: Type '\"yes\"' is not assignable to type '\"no\"'");
wontCompile("const b: ToArray<string | number> = ['x', 1];", "TS2322: Type '(string | number)[]' is not assignable to type 'string[] | number[]'");
note("TRAP: a distributive conditional over never is never (ToArray<never> is never); ToArrayAll<never> is never[]");

section("Template literal types: string types built from pieces");
syntax(
  ["`on${Capitalize<T>}`", "a string type computed from another: 'click' becomes 'onClick'"],
  ["`${A}-${B}` with unions", "every combination: 2 x 2 gives 4 strings"],
  ["`/${string}`", "a pattern: any string that starts with a slash"],
);
const handler: EventName<"click" | "focus"> = "onFocus";   // the union maps member by member: "onClick" | "onFocus"
show(() => handler);
const dirs: Dir[] = ["left-top", "left-bottom", "right-top", "right-bottom"];   // exactly these four are legal
show(() => dirs.length);
const route: Route = "/users/1";
show(() => route);
wontCompile("const e: EventName<'click'> = 'onclick';", "TS2322: Type '\"onclick\"' is not assignable to type '\"onClick\"'");
wontCompile("const d: Dir = 'left-middle';", "TS2322: Type '\"left-middle\"' is not assignable to type '\"left-top\" | \"left-bottom\" | \"right-top\" | \"right-bottom\"'");
wontCompile("const r: Route = 'users';", "TS2322: Type '\"users\"' is not assignable to type '`/${string}`'");

section("Recursive type: DeepReadonly<T>");
syntax(["{ readonly [K in keyof T]: DeepReadonly<T[K]> }", "the mapped type calls itself on every property: readonly all the way down (arrays too)"]);
const config = deepFreeze({ name: "app", db: { port: 5432, hosts: ["a", "b"] } });
show(() => config.db.port);
const changePort = (): void => {
  (config.db as { port: number }).port = 1;
};
show(() => thrown(changePort));                         // the type check was bypassed with a cast, but deepFreeze made it a run-time TypeError
const addHost = (): void => {
  (config.db.hosts as string[]).push("c");
};
show(() => thrown(addHost));                            // a frozen array refuses push as well
wontCompile("config.db.port = 1;", "TS2540: Cannot assign to 'port' because it is a read-only property");
wontCompile("config.db.hosts.push('c');", "TS2339: Property 'push' does not exist on type 'readonly string[]'");
note("Readonly<T> is shallow and compile-time only; DeepReadonly + deepFreeze is the deep pair (types vanish, freeze does not)");
note("Deep recursion costs compile time: a too-deep recursion fails with TS2589 (Type instantiation is excessively deep and possibly infinite)");
