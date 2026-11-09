/**
 * 7c. NARROWING AND TYPE GUARDS: let the compiler see that a value is not null
 *     Run: npx tsx src/7-null-and-undefined/narrowing-and-guards.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

type Fish = { swim: () => string };
type Bird = { fly: () => string };
type Person = { name: string; age: number };

/** Every check below narrows the type inside the branch that follows it. */
function describe(x: number | string | string[] | Date | null | undefined): string {
  if (x == null) return "nothing";                      // == null is true for null AND undefined: the one accepted use of ==
  if (typeof x === "number") return `number ${x.toFixed(1)}`;
  if (typeof x === "string") return `text of ${x.length}`;
  if (Array.isArray(x)) return `list of ${x.length}`;
  return `date ${x.toISOString()}`;                     // only Date is left
}

const move = (p: Fish | Bird): string => ("swim" in p ? p.swim() : p.fly());   // in narrows by property name

/** TRAP: a count of 0 is falsy, so the first branch is skipped for a perfectly good value. */
function labelWrong(count: number | undefined): string {
  if (count) return `count ${count}`;
  return "no count";
}
function labelRight(count: number | undefined): string {
  if (count !== undefined) return `count ${count}`;
  return "no count";
}

/** A user-defined type guard: the return type says "when true, x is a Person". tsc trusts the body. */
function isPerson(x: unknown): x is Person {
  return typeof x === "object" && x !== null && "name" in x && typeof x.name === "string" && "age" in x && typeof x.age === "number";
}
const isNumberWrong = (x: unknown): x is number => true;   // compiles: a guard that lies is the programmer's bug
function useWrongGuard(v: unknown): string | undefined {
  return isNumberWrong(v) ? v.toFixed() : undefined;   // v is a number here, according to the guard
}

/** An assertion function: it returns normally only when the condition holds, and narrows what follows. */
function assertDefined<T>(x: T | null | undefined, name: string): asserts x is T {
  if (x == null) throw new Error(`${name} is missing`);
}
function emailDomain(p: { email: string | null }): string {
  assertDefined(p.email, "email");
  return p.email.split("@")[1] ?? "";                   // here p.email is a string
}

/** The narrowing of p is lost inside a callback when p is assigned again later: the callback sees the declared type. */
function lengthsFixed(p: string | undefined): number[] {
  if (p === undefined) return [];
  const name = p;                                       // a const keeps its narrowed type in every callback
  const lengths = ["ab", "c"].map((s) => s.length + name.length);
  p = undefined;
  return lengths;
}

title("NARROWING AND GUARDS  -  the check tells the compiler what the type is");

section("Checks that narrow");
syntax(
  ["if (x) | if (x != null) | if (x !== undefined)", "truthy | not null and not undefined | not undefined"],
  ["typeof x === \"string\" | Array.isArray(x) | x instanceof Date", "by kind of value"],
  ["\"swim\" in x | x.kind === \"a\"", "by property name | by a discriminant property (lesson 12)"],
);
show(() => describe(null));
show(() => describe(undefined));                        // x == null caught this one as well
show(() => describe(3));
show(() => describe("abc"));
show(() => describe(["a", "b"]));
show(() => describe(new Date(Date.UTC(2026, 10, 9))));  // months count from 0, so 10 is November
show(() => move({ swim: () => "swims" }));
show(() => move({ fly: () => "flies" }));

section("Truthiness pitfalls");
show(() => labelWrong(5));
show(() => labelWrong(0));                              // TRAP: "no count", a valid 0 fell into the falsy branch
show(() => labelRight(0));                              // test for undefined, not for truthiness, when 0 or "" are real values
show(() => Boolean([]));                                // an empty array is truthy: test .length instead
show(() => Boolean(""));
note("if (x) is fine for objects and for 'any non-empty text'; never for numbers, booleans or empty strings that carry meaning");

section("The non-null assertion !");
syntax(["x!", "tells tsc 'trust me, it is not null or undefined'; it is erased and checks NOTHING at run time"]);
const stock = new Map<string, number>([["apple", 3]]);
show(() => stock.get("apple")! + 1);                    // 4: the promise held
show(() => thrown(() => stock.get("pear")!.toFixed()));   // TRAP: TypeError, the ! only silenced tsc
wontCompile("stock.get(\"pear\").toFixed()", "TS2532: Object is possibly 'undefined' (Map.get returns T | undefined)");
show(() => stock.get("pear")?.toFixed() ?? "none");     // the safe forms: ?. with ?? ...
show(() => (stock.get("pear") ?? 0) + 1);               // ... or a default

section("User-defined guards and assertion functions");
syntax(
  ["function isT(x: unknown): x is T", "when it returns true, x is a T in that branch"],
  ["function assertT(x: unknown): asserts x is T", "returns normally only when x is a T; narrows the code after the call"],
);
const good: unknown = JSON.parse("{\"name\":\"Ann\",\"age\":31}");
const bad: unknown = JSON.parse("{\"name\":\"Ann\"}");
show(() => isPerson(good));                             // JSON.parse gives any: validate it, never trust it (lesson 18)
show(() => isPerson(bad));
show(() => isPerson(null));
show(() => [1, null, 3, undefined].filter((x) => x != null));   // since TS 5.5 the callback is inferred to be a guard, so the result is number[]
show(() => thrown(() => useWrongGuard("text")));       // a lying guard: tsc believed it, TypeError at run time
show(() => emailDomain({ email: "ann@mail.com" }));
show(() => thrown(() => emailDomain({ email: null })));
wontCompile("const assertOk = (x: unknown): asserts x => { }; assertOk(v)", "TS2775: Assertions require every name in the call target to be declared with an explicit type annotation");

section("Narrowing is lost in a callback when the variable changes later");
wontCompile("if (p !== undefined) { xs.map(() => p.length); p = undefined; }", "TS18048: 'p' is possibly 'undefined' inside the callback");
show(() => lengthsFixed("xyz"));                        // the fix: copy into a const first
note("TS 5.4+ keeps the narrowing in a callback when the variable is never assigned after it (function scope); a let at the top level of a module still loses it");
note("Prefer const, early returns and small functions: a variable that never changes needs no tricks");
