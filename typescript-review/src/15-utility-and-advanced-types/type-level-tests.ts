/**
 * 15c. TYPE-LEVEL TESTS: Equal / Expect, @ts-expect-error, satisfies, and the limit: types vanish at run time
 *      Run: npx tsx src/15-utility-and-advanced-types/type-level-tests.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

/** True only when A and B are IDENTICAL types (the trick compares how the compiler treats two generic functions). */
type Equal<A, B> = (<T>() => T extends A ? 1 : 2) extends (<T>() => T extends B ? 1 : 2) ? true : false;

/** Compiles only when its argument is the type true: a failed Equal becomes a compile error. */
type Expect<T extends true> = T;

/** The obvious version: "A and B are assignable to each other". It looks right and is not. */
type NaiveEqual<A, B> = A extends B ? (B extends A ? true : false) : false;

interface User {
  id: number;
  name: string;
  email?: string;
}

function add(a: number, b: number): number {
  return a + b;
}

/** Compiles only when the value fits T. At run time it is an empty function. */
function assertType<T>(_value: T): void {}

// ---- the tests: a failing line is a compile error, a passing line costs nothing at run time
type Test1 = Expect<Equal<Partial<User>["email"], string | undefined>>;
type Test2 = Expect<Equal<Pick<User, "id" | "name">, { id: number; name: string }>>;
type Test3 = Expect<Equal<ReturnType<typeof add>, number>>;
type Test4 = Expect<Equal<Omit<User, "email">, { id: number; name: string }>>;
type Test5 = Expect<Equal<NaiveEqual<any, number>, boolean>>;   // the naive helper cannot even answer yes or no for any

title("TYPE-LEVEL TESTS  -  check types at compile time");

section("Equal<A, B> and Expect<T>: a test that fails the build");
syntax(
  ["type Expect<T extends true> = T", "compiles only if T is exactly true"],
  ["type Test = Expect<Equal<Actual, Expected>>", "one line per check; no run-time code is produced"],
  ["const r: Equal<A, B> = true", "a value annotated with the computed type: it records what you expect, and tsc checks it"],
);
const sameString: Equal<string, string> = true;
show(() => sameString);                                 // identical types
const anyVsNumber: Equal<any, number> = false;
show(() => anyVsNumber);                                // any is NOT identical to number, even though it is assignable to it
const orderFree: Equal<1 | 2, 2 | 1> = true;
show(() => orderFree);                                  // union member order does not matter
const merged: Equal<{ a: 1 } & { b: 2 }, { a: 1; b: 2 }> = false;
show(() => merged);                                     // TRAP: an intersection and the merged object type are NOT identical
const readonlyArray: Equal<readonly string[], string[]> = false;
show(() => readonlyArray);                              // readonly counts
const naive: NaiveEqual<{ a: 1 } & { b: 2 }, { a: 1; b: 2 }> = true;
show(() => naive);                                      // the naive helper only asks "assignable both ways", so it says yes
wontCompile("type Bad = Expect<Equal<string, number>>;", "TS2344: Type 'false' does not satisfy the constraint 'true'");
wontCompile("const x: Equal<string, string> = false;", "TS2322: Type 'false' is not assignable to type 'true'");
note("The Test1 to Test5 lines above the title run nothing: change an expected type and tsc (npm run check) fails, tsx still runs");

section("@ts-expect-error: a test that something does NOT compile");
syntax(
  ["// @ts-expect-error", "the next line MUST be a compile error; if it compiles, tsc reports the directive itself"],
  ["// @ts-ignore", "silences the next line whatever happens (no check that an error exists): avoid it"],
);
// @ts-expect-error  the second argument is missing
add(1);
// @ts-expect-error  a string is not a number
const joined = add("1", 2);
show(() => joined);                                     // "12": tsx removed the types and ran the wrong call, so + concatenated
wontCompile("// @ts-expect-error  above  const ok: number = 1;", "TS2578: Unused '@ts-expect-error' directive (the line is valid, so the directive is the error)");
note("Use it to pin a rule down: 'this must keep being an error'. When a refactor makes the line valid, the test turns red");

section("satisfies: check a value against a type and keep its own type");
syntax(
  ["const x = value satisfies Type", "error if value does not fit Type, but x keeps its precise inferred type (TS 4.9+)"],
  ["const x: Type = value", "the annotation widens x to Type: the precise type is lost"],
);
const palette = { red: [255, 0, 0], green: "#0f0" } satisfies Record<string, string | number[]>;
show(() => palette.red.map((n) => n / 255));            // palette.red is still number[], so map exists
wontCompile("const p: Record<string, string | number[]> = {...}; p.red.map(f);", "TS2339: Property 'map' does not exist on type 'string | number[]'");
wontCompile("{ reed: [1] } satisfies Record<'red' | 'green', number[]>", "TS2561: Object literal may only specify known properties ... Did you mean to write 'red'?");
wontCompile("{ red: [1] } satisfies Record<'red' | 'green', number[]>", "TS1360: Type '{ red: number[]; }' does not satisfy the expected type (Property 'green' is missing)");

section("The limit: types vanish at run time");
syntax(["assertType<T>(value)", "compile-time check that value fits T; the call itself does nothing"]);
show(() => assertType<number>(5));                      // undefined: an empty function ran, the check was tsc's
wontCompile("assertType<number>('x');", "TS2345: Argument of type 'string' is not assignable to parameter of type 'number'");
const loose: unknown = JSON.parse('{"id":"7"}');
show(() => typeof (loose as User).id);                  // string: no test above can see this, the data only exists at run time
note("tsx and node strip the types WITHOUT checking them: only tsc (npm run check) or the editor reports a type error");
note("Type tests prove the TYPES are right; data from outside still needs a run-time check (lesson 18)");
note("Libraries do this at scale: tsd, expect-type and vitest's expectTypeOf use the same Equal idea");
