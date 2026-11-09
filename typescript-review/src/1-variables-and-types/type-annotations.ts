/**
 * 1b. TYPE ANNOTATIONS: inference, any vs unknown, literal types, readonly, tuples, optional
 *     Run: npx tsx src/1-variables-and-types/type-annotations.ts        (add --plain for ASCII output)
 *     Types exist only for tsc and are ERASED at run time: the show lines print values, the wontCompile lines print the compiler's verdict.
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

type Direction = "left" | "right";
type User = { id: number; nick?: string };

/** The parameter has an optional type: inside, name is string | undefined. */
function greet(name?: string): string {
  return `Hello ${name ?? "guest"}`;
}

/** {} accepts every value except null and undefined, primitives included. */
function typeOfAnything(x: {}): string {
  return typeof x;
}

title("TYPE ANNOTATIONS  -  what tsc knows, and what is gone at run time");

section("Inference and explicit annotations");
syntax(
  ["let x: Type = value;", "explicit type (needed for parameters, empty collections, values from outside)"],
  ["let x = value;", "inferred from the value, WIDENED: let x = 5 is a number"],
  ["const x = value;", "inferred as the exact literal for a primitive: const x = 5 has the type 5"],
);
let widened = 5;  show(() => widened);                   // inferred number: a later widened = "five" is rejected
const exact = "left";  show(() => exact);                // inferred as the literal type "left", not string
wontCompile("let n = 5; n = 'five';", "TS2322: Type 'string' is not assignable to type 'number' (inferred, never declared)");
wontCompile("function add(a, b) { }", "TS7006: Parameter 'a' implicitly has an 'any' type (parameters are never inferred)");
const names: string[] = [];                              // an empty [] gives tsc nothing to infer from: annotate it so the element type is fixed
names.push("Ann");  show(() => names);
wontCompile("const xs: number[] = []; xs.push('x');", "TS2345: Argument of type 'string' is not assignable to parameter of type 'number'");

section("any vs unknown");
syntax(
  ["let x: any", "switches checking OFF: every use compiles, mistakes appear at run time"],
  ["let x: unknown", "ANY value may be assigned, but nothing may be done until it is narrowed"],
);
const loose: any = "text";                               // any is allowed in this lesson only because it is the topic
show(() => thrown(() => loose.missing.deep));            // TypeError at run time, tsc stayed silent
const wrong: number = loose;                             // compiles: any is assignable to every type
show(() => typeof wrong);                                // "string": a number-typed variable holding text
const safe: unknown = "text";
wontCompile("const u: unknown = 's'; u.length;", "TS18046: 'u' is of type 'unknown'");
show(() => (typeof safe === "string" ? safe.length : 0));   // narrowing with typeof makes it usable: 4
note("Prefer unknown for data of an unknown shape (JSON, catch variables); any silently disables the checker");

section("Literal types, as const, readonly");
syntax(
  ["type T = \"a\" | \"b\";", "a union of literal values: only those exact values fit (the enum replacement)"],
  ["value as const", "freeze the TYPE: readonly properties and literal types"],
  ["readonly T[] | { readonly p: T }", "an array or property tsc will not let you change"],
);
const dir: Direction = "left";  show(() => dir);
wontCompile("let d: Direction = 'left'; d = 'up';", `TS2322: Type '"up"' is not assignable to type 'Direction'`);
const point = { x: 1, y: 2 } as const;                   // type: { readonly x: 1; readonly y: 2 }
const modes = ["fast", "safe"] as const;                 // type: readonly ["fast", "safe"]
show(() => point);
show(() => modes);
wontCompile("point.x = 3;", "TS2540: Cannot assign to 'x' because it is a read-only property");
const editable = point as unknown as { x: number };      // the cast removes the protection for tsc, not for the runtime
editable.x = 99;
show(() => point.x);                                     // TRAP: 99, as const and readonly are compile-time only (no freeze)
const fixed: readonly number[] = [1, 2];
wontCompile("fixed.push(3);", "TS2339: Property 'push' does not exist on type 'readonly number[]'");
show(() => fixed.map((n) => n * 2));                     // reading methods stay available

section("Tuples and optional members");
syntax(
  ["let t: [string, number] = [\"a\", 1];", "a fixed-length array with one type per position"],
  ["[name: string, age?: number]", "named elements, ? makes the last one optional"],
  ["name?: string  |  (x?: string)", "an optional property or parameter has the type string | undefined"],
);
const pair: [string, number] = ["Ann", 30];
const [who, age] = pair;                                 // destructuring keeps the position types
show(() => who);
show(() => age);
show(() => Array.isArray(pair));                         // true: a tuple IS an array at run time
wontCompile("const p: [string, number] = ['a', 1]; p[2];", "TS2493: Tuple type '[string, number]' of length '2' has no element at index '2'");
show(() => greet());                                     // the missing argument is undefined
show(() => greet("Ann"));
const u1: User = { id: 1 };
show(() => u1.nick);                                     // undefined: an optional property that was not set
wontCompile("const u: User = {};", "TS2741: Property 'id' is missing in type '{}' but required in type 'User'");

section("object vs {}");
syntax(
  ["let o: object", "any non-primitive: objects, arrays, functions (not number, string, boolean, null)"],
  ["let e: {}", "any value except null and undefined, primitives included (avoid; the capital Object type is the same trap)"],
);
const fn: object = () => 1;
show(() => typeof fn);                                   // "function": a function is an object too
show(() => typeOfAnything(5));                           // "number": {} accepts primitives
wontCompile("const o: object = 5;", "TS2322: Type 'number' is not assignable to type 'object'");
wontCompile("const e: {} = null;", "TS2322: Type 'null' is not assignable to type '{}'");
note("Assertions (as, !), satisfies and typeof in a type position: the next file");
