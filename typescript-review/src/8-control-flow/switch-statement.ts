/**
 * 8b. SWITCH: === matching, fall-through, exhaustiveness, and the lookup-table alternative
 *     Run: npx tsx src/8-control-flow/switch-statement.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

enum Light { Red = "RED", Yellow = "YELLOW", Green = "GREEN" }
type Shape = { kind: "circle"; r: number } | { kind: "rect"; w: number; h: number } | { kind: "dot" };

/** For a value that can never happen: its parameter type is never, so a forgotten case is a COMPILE error. */
function assertNever(x: never): never {
  throw new Error(`unexpected value: ${JSON.stringify(x)}`);
}

/** switch compares with ===, so 1 and "1" are different cases (x is unknown, so tsc allows both). */
function kindOf(x: unknown): string {
  switch (x) {
    case 1: return "number 1";
    case "1": return "string 1";
    case true: return "true";
    default: return "other";
  }
}

/** Grouped cases: several values share one body. */
function dayKind(d: number): string {
  switch (d) {
    case 6:
    case 7:
      return "weekend";
    case 1: case 2: case 3: case 4: case 5:
      return "weekday";
    default:
      return "invalid";
  }
}

/** TRAP: no break after case 1, so it runs on into case 2. Fall-through is the default behaviour. */
function fallThrough(n: number): string[] {
  const out: string[] = [];
  switch (n) {
    case 1: out.push("one");
    case 2: out.push("two"); break;
    case 3: out.push("three");
  }
  return out;
}

/** TRAP: default may sit anywhere; it runs only when nothing matches, but then it falls through into the next case. */
function defaultInMiddle(n: number): string[] {
  const out: string[] = [];
  switch (n) {
    case 1: out.push("one"); break;
    default: out.push("default");
    case 2: out.push("two");
  }
  return out;
}

/** Braces give each case its own block, so both can declare base. Without them the second const is an error. */
function priceOf(code: string): number {
  switch (code) {
    case "A": { const base = 10; return base * 2; }
    case "B": { const base = 5; return base; }
    default: return 0;
  }
}

function nanSwitch(x: number): string {
  switch (x) {
    case NaN: return "nan";                             // never matches: NaN === NaN is false
    default: return "default";
  }
}

/** An enum subject with a never check in default: add a Light and this stops compiling until you handle it. */
function nextLight(l: Light): Light {
  switch (l) {
    case Light.Red: return Light.Green;
    case Light.Green: return Light.Yellow;
    case Light.Yellow: return Light.Red;
    default: return assertNever(l);
  }
}

/** A discriminated union: switch (s.kind) narrows s inside each case. */
function area(s: Shape): number {
  switch (s.kind) {
    case "circle": return 3 * s.r * s.r;
    case "rect": return s.w * s.h;
    case "dot": return 0;
    default: return assertNever(s);
  }
}

title("SWITCH  -  match one value against many cases");

section("switch compares with ===");
syntax(
  ["switch (x) { case a: ...; break; case b: case c: ...; break; default: ... }", "case values are compared with === (no type conversion)"],
  ["break | return | throw", "end a case; without one the code FALLS THROUGH into the next case"],
);
show(() => kindOf(1));
show(() => kindOf("1"));                                // a different case: "1" !== 1
show(() => kindOf(true));
show(() => kindOf(NaN));                                // other: no case matches
show(() => nanSwitch(NaN));                             // TRAP: "default", case NaN can never match because NaN !== NaN (use Number.isNaN first)
wontCompile("switch (s) { case 1: ... }   (s: string)", "TS2678: Type 'number' is not comparable to type 'string'");
show(() => dayKind(7));                                 // case 6: case 7: share one body
show(() => dayKind(3));
show(() => dayKind(9));

section("Fall-through and default in the middle");
show(() => fallThrough(1));                             // TRAP: [ 'one', 'two' ]: no break after case 1
show(() => fallThrough(2));
show(() => fallThrough(3));
show(() => defaultInMiddle(1));
show(() => defaultInMiddle(2));
show(() => defaultInMiddle(9));                         // TRAP: [ 'default', 'two' ]: default ran, then fell into case 2
note("Fall-through is almost always a bug: end every case with break, return or throw, and comment the rare deliberate one");

section("case blocks need braces for let and const");
show(() => priceOf("A"));                               // 20
show(() => priceOf("B"));
wontCompile("case 1: const x = 10; ... case 2: const x = 20;", "TS2451: Cannot redeclare block-scoped variable 'x' (the whole switch is ONE block: add braces)");

section("Exhaustive switch: a missing case is a compile error");
syntax(
  ["default: return assertNever(x);", "x has type never only when every case is handled; assertNever(x: never): never throws otherwise"],
);
show(() => nextLight(Light.Red));
show(() => nextLight(Light.Yellow));
show(() => area({ kind: "rect", w: 2, h: 3 }));
show(() => area({ kind: "dot" }));
show(() => thrown(() => nextLight("PURPLE" as Light)));   // the default branch is also the run-time guard for bad data from outside
wontCompile("case Light.Yellow removed, default: assertNever(l)", "TS2345: Argument of type 'Light.Yellow' is not assignable to parameter of type 'never'");
wontCompile("function f(l: Light): Light { switch (l) { ...2 of 3 cases } }", "TS2366: Function lacks ending return statement and return type does not include 'undefined'");
note("Without the default branch the TS2366 form still protects a function that returns a value; with assertNever it also protects a plain statement switch");

section("A lookup table instead of a switch");
const dayNames: Record<number, string> = { 1: "Mon", 2: "Tue", 3: "Wed", 4: "Thu", 5: "Fri", 6: "Sat", 7: "Sun" };
const table: Record<string, string> = { RED: "stop", GREEN: "go" };
show(() => dayNames[3] ?? "?");                         // data instead of code: easy to extend, easy to test
show(() => dayNames[9] ?? "?");                         // a missing key gives undefined, so ?? supplies the default
show(() => table["GREEN"]);
show(() => typeof table["toString"]);                   // TRAP: "function": a plain object also has inherited keys
show(() => Object.hasOwn(table, "toString"));           // false: check own keys, or use a Map (lesson 5)
show(() => new Map(Object.entries(table)).get("toString"));   // a Map has no inherited keys
note("switch for a few branches with logic, a lookup table for value-to-value mappings, polymorphism or a union for behaviour per type");
