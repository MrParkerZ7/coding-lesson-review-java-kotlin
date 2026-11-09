/**
 * 10a. FUNCTION FORMS: declaration, expression, arrow, parameters, overloads
 *      Run: npx tsx src/10-functions-and-closures/function-forms.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("FUNCTION FORMS  -  three ways to write one, and the parameter rules");

section("Declaration, expression, arrow: hoisting is the difference that matters");
syntax(
  ["function name(a: T): R { ... }", "declaration: hoisted WITH its body, callable above its own line"],
  ["const name = function (a: T): R { ... };", "expression: only a variable, so it is in the TDZ until the line runs"],
  ["const name = (a: T): R => expr;", "arrow: same TDZ; no own this, no own arguments (next file)"],
);
show(() => declared(3));                                // works: the declaration below was hoisted with its body
function declared(n: number): number { return n * 2; }
show(() => thrown(() => laterArrow(3)));                // ReferenceError: the const below is still in the TDZ
const laterArrow = (n: number): number => n * 2;
show(() => laterArrow(3));                              // after its line has run it works
const viaExpression = function (n: number): number { return n + 1; };
show(() => viaExpression(1));                           // a function expression is also just a value in a variable
show(() => declared.name);                              // the name property comes from the declaration
show(() => laterArrow.name);                            // an arrow takes its name from the variable it is assigned to
note("Use declarations for top-level helpers, arrows for callbacks. TDZ in depth: lesson 1 (let-const-var)");

section("Default parameters run at CALL time, and only for undefined");
syntax(
  ["function f(a: T = expr)", "expr is evaluated on every call that leaves a out (or passes undefined)"],
  ["function f(a: T, b = a)", "a default may read the parameters before it"],
);
let calls = 0;
const nextId = (): number => ++calls;
function tag(label: string, id = nextId()): string { return `${label}#${id}`; }
show(() => tag("a"));                                   // the default ran: calls is 1
show(() => tag("b"));                                   // evaluated again: a NEW value on every call (nothing is shared between calls)
show(() => tag("c", 99));                               // an argument was given: the default expression is NOT evaluated
show(() => calls);                                      // 2 evaluations for 3 calls
function box(w: number, h = w): number[] { return [w, h]; }
show(() => box(4));                                     // h defaults to w
show(() => box(4, undefined));                          // undefined counts as "missing"
const nullable = (x: number | null = 5): number | null => x;
show(() => nullable(null));                             // TRAP: null is a real value, it does NOT trigger the default (lesson 7)

section("Optional, rest and spread");
syntax(
  ["function f(a: T, b?: U)", "b is U | undefined; optional parameters must come last"],
  ["function f(...rest: T[])", "rest parameter: a real array of the remaining arguments"],
  ["f(...array)", "spread at the call: each element becomes one argument"],
);
function greet(name: string, title?: string): string { return title === undefined ? name : `${title} ${name}`; }
show(() => greet("Ann"));
show(() => greet("Ann", "Dr"));
wontCompile("greet()", "TS2554: Expected 1-2 arguments, but got 0 (JS would allow it)");
wontCompile("greet('a', 'b', 'c')", "TS2554: Expected 1-2 arguments, but got 3 (JS would ignore the extra one)");
wontCompile("function f(a?: number, b: string) {}", "TS1016: A required parameter cannot follow an optional parameter");
function sum(...nums: number[]): number { return nums.reduce((acc, n) => acc + n, 0); }
show(() => sum());                                      // rest is an empty array, not undefined
show(() => sum(1, 2, 3));
const parts = [4, 5, 6];
show(() => sum(...parts));                              // spread an array into arguments
show(() => sum(0, ...parts, 7));                        // spread can sit anywhere in the call
const bigArray = Array.from({ length: 1_000_000 }, () => 1);
show(() => thrown(() => Math.max(...bigArray)));        // TRAP: millions of arguments blow the call stack; use reduce instead

section("Destructured parameters");
syntax(["function f({ a, b = 1 }: { a: T; b?: U })", "unpack an options object right in the signature (named arguments, the JS way)"]);
function area({ w, h = w }: { w: number; h?: number }): number { return w * h; }
show(() => area({ w: 3 }));                             // h falls back to w
show(() => area({ w: 3, h: 5 }));
function firstTwo([a, b]: number[]): number[] { return [b ?? 0, a ?? 0]; }
show(() => firstTwo([1, 2, 3]));                        // array destructuring in the parameter list

section("Return types, void, overloads");
syntax(
  ["function f(): void", "returns nothing: the result at run time is undefined"],
  ["type Cb = () => void", "a function TYPE returning void accepts functions that return something (the value is ignored)"],
  ["function f(a: A): X;  function f(a: B): Y;", "overload signatures, then ONE implementation signature (not callable from outside)"],
);
function log1(): void { /* nothing */ }
show(() => log1());                                     // no return statement: undefined
wontCompile("function f(): void { return 42; }", "TS2322: Type 'number' is not assignable to type 'void'");
const ignore: () => void = () => 42;                    // allowed: a void callback type may return a value
show(() => ignore());                                   // it still returns 42 at run time; the TYPE says void
function convert(x: string): number;
function convert(x: number): string;
function convert(x: string | number): number | string { return typeof x === "string" ? Number(x) : String(x); }
show(() => convert("42"));                              // the overload says number
show(() => convert(42));                                // the overload says string
wontCompile("convert(true)", "TS2769: No overload matches this call");
wontCompile("convert(maybe)", "TS2769 for a string | number argument: only the overload signatures count");

section("Arguments object, IIFE, recursion");
function outer(_first?: number): number | undefined {
  const inner = (): number | undefined => (arguments[0] as number | undefined);   // an arrow has no own arguments: it sees outer's
  return inner();
}
show(() => outer(7));                                   // arguments is the outer function's list
note("arguments is old: array-like (no map), and an arrow has none of its own. Use rest parameters (...args) instead");
show(() => (() => 6 * 7)());                            // IIFE: define and call at once, to get a private scope (pre-modules idiom)
const factorial = (n: number): number => (n <= 1 ? 1 : n * factorial(n - 1));
show(() => factorial(5));                               // a recursive arrow may call its own const name
const sumTo = (n: number, acc = 0): number => (n === 0 ? acc : sumTo(n - 1, acc + n));
show(() => sumTo(100));                                 // a tail call, but V8 does NOT optimise tail calls...
show(() => thrown(() => sumTo(1_000_000)));             // ...so deep recursion is a RangeError (maximum call stack): use a loop
