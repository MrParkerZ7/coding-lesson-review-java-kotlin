/**
 * 17e. COMMON ERRORS: TypeError, ReferenceError, RangeError, SyntaxError, URIError, system errors, assert, and what does NOT throw
 *      Run: npx tsx src/17-error-handling/common-errors.ts        (add --plain for ASCII output)
 */
import assert from "node:assert/strict";
import { readFileSync } from "node:fs";
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

// ---- values the compiler cannot see through, so the triggers compile
const nothing = (): { x: number } => undefined as unknown as { x: number };
const notAFunction = 5 as unknown as () => void;
const frozen = Object.freeze({ a: 1 }) as { a: number };
const writeFrozen = (): void => {
  frozen.a = 2;
};
const destructureNull = (): number => {
  const { a } = null as unknown as { a: number };
  return a;
};
class Point {}
const callClass = Point as unknown as () => void;

declare const ghost: number;                            // declared for tsc, but no such variable exists at run time

/** Reads `later` before the line that initialises it has run (the temporal dead zone). */
function useBeforeInit(): number {
  return later;
}
const beforeInit = thrown(useBeforeInit);
const later = 1;

function endless(n: number): number {
  return endless(n + 1) + 1;                            // no base case: every call waits for the next one
}

/** Returns the thrown value itself, or undefined when nothing was thrown. */
function caught(fn: () => unknown): unknown {
  try {
    fn();
  } catch (error) {
    return error;
  }
  return undefined;
}

title("COMMON ERRORS  -  the trigger and the class name");

section("The family");
syntax(
  ["Error", "the base class; TypeError, RangeError, ReferenceError, SyntaxError, URIError, EvalError and AggregateError extend it"],
  ["DOMException", "browser-style errors node also throws (structuredClone, AbortSignal): it is an Error too, with a name and a code"],
);
show(() => new RangeError("x") instanceof Error);       // catching Error also catches all of them
show(() => RangeError.prototype instanceof Error);
show(() => Object.keys(new RangeError("x")));           // no enumerable fields: branch on instanceof or name

section("TypeError: the wrong kind of value was used");
show(() => thrown(() => nothing().x));                  // reading a property of undefined (or null)
show(() => thrown(notAFunction));                       // calling something that is not a function
show(() => thrown(callClass));                          // a class called without new
show(() => thrown(writeFrozen));                        // writing to a frozen object (modules are strict, so it throws)
show(() => thrown(destructureNull));                    // destructuring null or undefined
show(() => thrown(() => ([] as number[]).reduce((a, b) => a + b)));   // reduce on an empty array with no initial value
show(() => thrown(() => 1n + (1 as unknown as bigint)));   // mixing bigint and number
wontCompile("const n = 5; n();", "TS2349: This expression is not callable. Type 'Number' has no call signatures");
wontCompile("const u = undefined; u.x;", "TS18048: 'u' is possibly 'undefined'");
note("'Cannot read properties of undefined' is the most common error in JavaScript: strict mode and optional chaining (?.) are the cure");

section("ReferenceError: the name does not exist (or is not initialised yet)");
show(() => thrown(() => ghost));                        // no variable called ghost was ever created
show(() => beforeInit);                                 // the const existed but was still in its dead zone when the function ran
show(() => typeof ghost);                                // typeof is the one safe question about an undeclared name: no throw
wontCompile("ghost2;", "TS2304: Cannot find name 'ghost2'");
note("typeof does not help inside the dead zone: typeof of a let or const that is not initialised yet still throws");

section("RangeError: the value is out of the allowed range");
show(() => thrown(() => new Array(-1)));                // invalid array length
show(() => thrown(() => "x".repeat(-1)));
show(() => thrown(() => BigInt(1.5)));                  // a bigint must be an integer
show(() => thrown(() => new Date(NaN).toISOString()));  // an invalid date cannot be formatted
show(() => thrown(() => endless(0)));                   // stack overflow is a RangeError in JavaScript (an Error subclass in Java)
note("Deep recursion over big input is the interview version: use a loop or an explicit stack (array) instead");

section("SyntaxError, URIError and DOMException");
show(() => thrown(() => JSON.parse("{")));              // invalid JSON text
show(() => thrown(() => JSON.parse("")));               // empty text is invalid too
show(() => thrown(() => new RegExp("(")));              // an invalid regular expression
show(() => thrown(() => decodeURIComponent("%")));      // malformed percent-encoding
show(() => thrown(() => structuredClone(() => 1)));     // functions cannot be cloned: a DOMException (code 25, DataCloneError)

section("Node system errors and assert");
const missing = caught(() => readFileSync("definitely-missing-file.txt"));
show(() => (missing as NodeJS.ErrnoException).code);    // system errors are plain Error objects with a string code
show(() => (missing as NodeJS.ErrnoException).syscall);
show(() => missing instanceof Error);
show(() => thrown(() => assert.strictEqual(1, 2)));     // assert throws an AssertionError when the check fails
show(() => (caught(() => assert(0 > 1, "math broke")) as { code: string }).code);
show(() => thrown(() => assert.deepStrictEqual({ a: [1] }, { a: [1] })));   // equal by value: nothing happens
show(() => thrown(() => assert.throws(() => JSON.parse("{"), SyntaxError)));   // asserts that it DOES throw a SyntaxError
note("assert(cond) also narrows the type (asserts cond). Use it for invariants and tests; console.assert only prints and never throws");

section("Quiet failures: what does NOT throw");
syntax(["NaN | Infinity | undefined | Invalid Date", "JavaScript prefers a special value to an exception in these places"]);
show(() => 1 / 0);                                      // Infinity, unlike Java's ArithmeticException
show(() => 0 / 0);
show(() => Number("12px"));                             // NaN, no NumberFormatException
show(() => [1, 2, 3][10]);                              // undefined, no IndexOutOfBounds
show(() => "abc"[10]);
show(() => new Map<string, number>().get("k"));         // undefined
show(() => new Date("garbage").getTime());              // NaN: an invalid Date is a value, only toISOString throws
show(() => Math.sqrt(-1));
note("These bugs travel silently: NaN spreads through every calculation. Check Number.isNaN, array bounds and undefined yourself");
