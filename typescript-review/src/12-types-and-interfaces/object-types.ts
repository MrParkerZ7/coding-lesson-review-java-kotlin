/**
 * 12a. OBJECT TYPES: type vs interface, optional, readonly, index signatures, tuples
 *      Run: npx tsx src/12-types-and-interfaces/object-types.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

title("OBJECT TYPES  -  the shape of an object, checked at compile time only");

section("interface, type, optional and readonly");
syntax(
  ["interface Name { a: T; b?: U; readonly c: V }", "an object shape: open (can merge), best for objects and classes"],
  ["type Name = { a: T; b?: U }", "an alias: works for ANY type (union, tuple, function); cannot be merged"],
  ["b?: U  |  readonly c: V", "b is U | undefined and may be missing; c cannot be assigned (compile time only)"],
);
interface User { id: number; name: string; email?: string; readonly createdAt: string }
const ann: User = { id: 1, name: "Ann", createdAt: "2026-11-09" };
show(() => ann);                                        // plain object at run time: the types are gone
show(() => ann.email);                                  // missing optional property: undefined
wontCompile("ann.createdAt = 'x'", "TS2540: Cannot assign to 'createdAt' because it is a read-only property");
const writable = ann as { createdAt: string };          // readonly does not change assignability: this cast is allowed
writable.createdAt = "changed";
show(() => ann.createdAt);                              // TRAP: readonly is erased; only Object.freeze protects at run time

section("extends vs &, merging, implements");
syntax(
  ["interface B extends A { }", "B has everything A has; a clash is an error at the declaration"],
  ["type B = A & { }", "intersection: both shapes at once; a clash becomes never, silently"],
  ["interface A { } interface A { }", "declaration merging: same name twice = ONE interface (a type alias cannot)"],
);
interface Animal { name: string }
interface Dog extends Animal { breed: string }
type Cat = Animal & { lives: number };
const rex: Dog = { name: "Rex", breed: "lab" };
const tom: Cat = { name: "Tom", lives: 9 };
show(() => [rex.name, tom.lives]);
interface Config { host: string }
interface Config { port: number }                       // merged with the line above
const cfg: Config = { host: "localhost", port: 8080 };
show(() => cfg);
wontCompile("type T = { a: string }; type T = { b: number }", "TS2300: Duplicate identifier 'T'");
wontCompile("interface Bad extends Animal { name: number }", "TS2430: Interface 'Bad' incorrectly extends interface 'Animal'");
type Clash = { a: string } & { a: number };             // compiles: the property a is now string & number = never
wontCompile("const c: Clash = { a: 'x' }", "TS2322: Type 'string' is not assignable to type 'never'");
interface Printable { print(): string }
class Doc implements Printable { constructor(private text: string) {} print(): string { return this.text; } }
show(() => new Doc("hello").print());                  // implements is only a compile-time check, erased at run time (lesson 13)

section("Excess property check vs structural typing");
syntax(["const p: Point = { x: 1, y: 2, z: 3 }", "a FRESH object literal may not have extra properties"]);
interface Point { x: number; y: number }
wontCompile("const p: Point = { x: 1, y: 2, z: 3 }", "TS2353: Object literal may only specify known properties");
const wide = { x: 1, y: 2, z: 3 };
const viaVariable: Point = wide;                        // compiles: any value with x and y fits (structural typing)
show(() => viaVariable);                                // the extra z is still there at run time

section("Index signatures, Record, call signatures");
syntax(
  ["{ [key: string]: T }", "any number of string keys, every value a T"],
  ["Record<K, T>", "the same idea with a known key set: Record<'a' | 'b', T> needs both keys"],
  ["{ (a: A): R; prop: P }  |  { new (a: A): R }", "call signature (callable with properties) | construct signature"],
);
interface Scores { [name: string]: number }
const scores: Scores = { ann: 90, bob: 75 };
show(() => scores["ann"]);
show(() => scores["zed"]);                              // TRAP: the type says number, the value is undefined (noUncheckedIndexedAccess fixes it)
wontCompile("interface Bad { [k: string]: number; name: string }", "TS2411: 'name' (string) is not assignable to the string index type number");
const weights: Record<"low" | "high", number> = { low: 1, high: 10 };
show(() => weights);
wontCompile("const w: Record<'low' | 'high', number> = { low: 1 }", "TS2741: Property 'high' is missing");
interface Formatter { (value: number): string; locale: string }
const fmt: Formatter = Object.assign((value: number) => value.toFixed(2), { locale: "en-US" });
show(() => fmt(3.14159));                               // a function that also has a property
show(() => fmt.locale);

section("Tuples");
syntax(
  ["type Pair = [first: string, second: number]", "fixed length, a type per position; the labels are documentation only"],
  ["[string, number?]  |  [string, ...number[]]", "optional element | rest element"],
);
type Pair = [first: string, second: number];
const pair: Pair = ["a", 1];
show(() => pair);                                       // an ordinary array at run time
show(() => Array.isArray(pair));
wontCompile("const bad: Pair = ['a']", "TS2322: Type '[string]' is not assignable to type 'Pair' (1 element, 2 required)");
pair.push(99);                                          // compiles! push is allowed on a plain tuple
show(() => pair);                                       // TRAP: the tuple grew to 3 at run time; use readonly [string, number] to forbid it
wontCompile("const ro: readonly [string, number] = pair; ro.push(1)", "TS2339: Property 'push' does not exist on type");
note("Tuples are how a function returns several values: const [value, setValue] = useState(0)");
