/**
 * 7a. NULL VERSUS UNDEFINED: two "empty" values, and what strictNullChecks adds
 *     Run: npx tsx src/7-null-and-undefined/null-vs-undefined.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("NULL VERSUS UNDEFINED  -  two empty values, two meanings");

section("The two values");
syntax(
  ["undefined", "\"nothing was given\": a variable not yet set, a missing property or argument, no return"],
  ["null", "\"deliberately empty\": a value somebody set to mean no value"],
  ["T | undefined | null", "the type says which of them may appear (strictNullChecks, part of strict)"],
);
show(() => typeof undefined);
show(() => typeof null);                                // TRAP: "object", a bug from 1995 that can never be fixed (lesson 0)
show(() => null == undefined);                          // true: loose equality treats the two as the same "nothing"
show(() => null === undefined);                         // false: strict equality tells them apart
show(() => null == 0);                                  // false: null is only loosely equal to undefined, not to 0 or ""
show(() => null == "");                                 // false
show(() => (null as unknown as number) >= 0);           // TRAP: true! relational operators convert null to 0 (a cast: tsc refuses the plain form)
show(() => Number(null));                               // 0
show(() => Number(undefined));                          // NaN: see lesson 0 type-conversions
wontCompile("const gt = null >= 0;", "TS18050: The value 'null' cannot be used here");
wontCompile("const plus = null + 1;", "TS18050: The value 'null' cannot be used here");

section("Where each one comes from");
let declared: string | undefined;
show(() => declared);                                   // a variable that was never assigned
const emptyObject: { name?: string } = {};
show(() => emptyObject.name);                           // a missing property
const noReturn = (): void => { /* nothing returned */ };
show(() => noReturn());                                 // a function without return gives undefined
const optionalArg = (x?: number): number | undefined => x;
show(() => optionalArg());                              // a missing argument
show(() => [1, 2, 3][10]);                              // an index out of range (typed number by tsc: lesson 7d)
show(() => new Map([["a", 1]]).get("b"));               // a missing Map key
show(() => "abc".match(/\d/));                          // null: an API that says "no match" on purpose
show(() => JSON.parse("null"));                         // null is a valid JSON value, undefined is not
show(() => void 0);                                     // the void operator always gives undefined (old way to write it)

section("A missing property is not the same as a property holding undefined");
const missing: { a?: number } = {};
const holding: { a?: number } = { a: undefined };
show(() => missing.a);                                  // undefined
show(() => holding.a);                                  // undefined: reading cannot tell the two apart
show(() => "a" in missing);                             // false
show(() => "a" in holding);                             // true: the key exists, its value is undefined
show(() => Object.keys(holding));                       // [ 'a' ]
show(() => JSON.stringify({ a: undefined, b: null }));  // undefined is dropped, null is kept (lesson 18)
show(() => Object.hasOwn(holding, "a"));                // the safe test for "is this key present"

section("Defaults apply to undefined only, never to null");
syntax(
  ["function f(x = 5) { }", "a parameter default: used when the argument is undefined or missing"],
  ["const { a = 5 } = obj;", "a destructuring default: used when obj.a is undefined"],
  ["x ?? 5", "the same rule as an expression: undefined AND null both give 5 (lesson 7b)"],
);
const withDefault = (x: number | null = 5): number | null => x;
show(() => withDefault());                              // 5: missing argument
show(() => withDefault(undefined));                     // 5: undefined counts as missing
show(() => withDefault(null));                          // TRAP: null, the default is NOT used
const cfg: { port?: number | null } = { port: null };
const { port = 8080 } = cfg;  show(() => port);         // null: destructuring defaults skip null as well
const extra: { host?: string } = {};
const { host = "localhost" } = extra;  show(() => host);   // the property is missing, so the default applies
show(() => cfg.port ?? 8080);                           // 8080: ?? is the version that replaces both

section("strictNullChecks: null and undefined are their own types");
syntax(
  ["let s: string = value;", "never null or undefined: tsc enforces it"],
  ["let s: string | null = null;", "may be null (an explicit empty value)"],
  ["let n: number | undefined;  function f(n?: number)", "may be undefined; a ? parameter is T | undefined"],
);
wontCompile("const s: string = null;", "TS2322: Type 'null' is not assignable to type 'string'");
wontCompile("const n: number = undefined;", "TS2322: Type 'undefined' is not assignable to type 'number'");
wontCompile("let x: string; x.length", "TS2454: Variable 'x' is used before being assigned");
show(() => thrown(() => (undefined as unknown as string).length));   // TypeError: the crash tsc is protecting you from (cast to hide it)
show(() => thrown(() => (null as unknown as string[]).length));      // the same crash for null
note("Without strict (strictNullChecks off) every type silently accepts null and undefined: this is what the NullPointerException world looks like");
note("Convention: use undefined for 'not provided' and let null come only from APIs that return it (match, JSON, DOM)");
