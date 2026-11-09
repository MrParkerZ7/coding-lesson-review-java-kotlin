/**
 * 12b. UNIONS AND NARROWING: |, &, literal unions, discriminated unions, never, unknown
 *      Run: npx tsx src/12-types-and-interfaces/unions-and-narrowing.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("UNIONS AND NARROWING  -  one of several types, and how TS tells which");

section("Unions: only what ALL members share is allowed until you narrow");
syntax(
  ["A | B", "a value that is an A or a B"],
  ["A & B", "a value that is both: all the members of each"],
  ["'up' | 'down'", "a literal union: a closed set of values, the lightweight enum"],
);
function describe(x: string | number): string {
  return typeof x === "string" ? `text of ${x.length}` : `number ${x.toFixed(1)}`;   // typeof narrows each branch
}
show(() => describe("hey"));
show(() => describe(3));
wontCompile("function f(x: string | number) { x.toFixed(); }", "TS2339: Property 'toFixed' does not exist on type 'string | number'");
type Direction = "up" | "down" | "left" | "right";
const opposite = (d: Direction): Direction => (d === "up" ? "down" : d === "down" ? "up" : d === "left" ? "right" : "left");
show(() => opposite("left"));
wontCompile("opposite('north')", "TS2345: Argument of type '\"north\"' is not assignable to parameter of type 'Direction'");

section("Narrowing tools: typeof, in, instanceof, equality, discriminant");
syntax(
  ["typeof x === 'string'", "primitives"],
  ["'swim' in pet", "which of several object shapes has this property"],
  ["e instanceof Error", "class instances"],
  ["s.kind === 'circle'", "a discriminant: a literal property every member has (the best tool)"],
);
type Fish = { swim(): string };
type Bird = { fly(): string };
const move = (pet: Fish | Bird): string => ("swim" in pet ? pet.swim() : pet.fly());
show(() => move({ fly: () => "flap" }));
const message = (e: unknown): string => (e instanceof Error ? e.message : String(e));
show(() => message(new RangeError("too big")));
show(() => message("plain text"));                      // anything can be thrown in JS: always narrow before reading
type Shape = { kind: "circle"; radius: number } | { kind: "rect"; width: number; height: number };
const assertNever = (x: never): never => { throw new Error(`unexpected ${JSON.stringify(x)}`); };
function area(s: Shape): number {
  switch (s.kind) {
    case "circle": return Math.round(Math.PI * s.radius ** 2);   // here s is the circle member
    case "rect": return s.width * s.height;
    default: return assertNever(s);                     // s is never here: every case was handled
  }
}
show(() => area({ kind: "rect", width: 3, height: 4 }));
show(() => area({ kind: "circle", radius: 2 }));
const bogus = { kind: "hex" } as unknown as Shape;     // data from outside (JSON) can break the type's promise
show(() => thrown(() => area(bogus)));                  // the never branch catches it at run time
wontCompile("assertNever(s) with a triangle member", "TS2345: the new member is not assignable to 'never': add its case");
note("Exhaustive checking turns a forgotten case into a compile error: add a member, and every switch that misses it fails to compile");

section("unknown, any, never");
syntax(
  ["unknown", "any value, but you must narrow before using it: the safe top type"],
  ["any", "switches the checker off for everything it touches: avoid"],
  ["never", "no value at all: a function that never returns, or a case that cannot happen"],
);
const u: unknown = "text";
const lengthOf = (v: unknown): number => (typeof v === "string" ? v.length : -1);
show(() => lengthOf(u));                                // narrowing is the only way in
wontCompile("u.length", "TS18046: 'u' is of type 'unknown'");
const loose: any = 5;                                   // any is the topic here
show(() => thrown(() => loose.foo.bar));                // TypeError at run time: tsc said nothing
wontCompile("const n: never = 1", "TS2322: Type 'number' is not assignable to type 'never'");
const fail = (text: string): never => { throw new Error(text); };
show(() => thrown(() => fail("boom")));                 // a never function never returns normally
const count = Number("5") > 3 ? 1 : fail("small");      // never fits anywhere: this is just a number
show(() => count);

section("as and satisfies");
syntax(
  ["value as T", "tell the checker you know better: NO run-time check, so it can lie"],
  ["value satisfies T", "check the value against T, but keep its own narrower type"],
);
wontCompile("'5' as number", "TS2352: Conversion of type 'string' to type 'number' may be a mistake");
const lie = "5" as unknown as number;                   // the double cast silences the checker
show(() => lie + 1);                                    // TRAP: the type says number, the value is text: "51"
const palette = { red: [255, 0, 0], green: "#00ff00" } satisfies Record<string, string | number[]>;
show(() => palette.red.map((c) => c / 255));            // red is still number[], not string | number[]
const widened: Record<string, string | number[]> = { red: [255, 0, 0] };
show(() => widened.red);                                // annotation instead of satisfies: the specific type is lost
wontCompile("widened.red.map(...)", "TS2339: Property 'map' does not exist on type 'string | number[]'");
wontCompile("{ x: true } satisfies Record<string, string>", "TS2322: Type 'boolean' is not assignable to type 'string'");
