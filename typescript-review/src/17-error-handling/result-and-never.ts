/**
 * 17d. RESULT AND never: expected failures as values, fail(), assertion functions
 *      Run: npx tsx src/17-error-handling/result-and-never.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** A minimal custom error, enough for the Result below (custom-errors builds the full hierarchy). */
class ValidationError extends Error {
  constructor(
    readonly field: string,
    message: string,
  ) {
    super(message);
    this.name = "ValidationError";
  }
}

type Result<T, E = Error> = { ok: true; value: T } | { ok: false; error: E };

function parsePort(text: string): Result<number, ValidationError> {
  const n = Number(text);
  if (!Number.isInteger(n) || n < 1 || n > 65535) return { ok: false, error: new ValidationError("port", `bad port: ${text}`) };
  return { ok: true, value: n };
}

/** Turn a throwing function into a Result: the failure is a value you must look at. */
function attempt<T>(fn: () => T): Result<T, unknown> {
  try {
    return { ok: true, value: fn() };
  } catch (error) {
    return { ok: false, error };
  }
}

/** Return type never: this function cannot return, so it fits wherever a value is expected. */
function fail(message: string): never {
  throw new Error(message);
}

/** An assertion function: after it returns, TypeScript knows the value is not undefined. */
function assertDefined<T>(value: T, name: string): asserts value is NonNullable<T> {
  if (value === undefined || value === null) throw new Error(`${name} is missing`);
}

const maybe = (text: string): string | undefined => (text === "" ? undefined : text);
title("RESULT AND never  -  failures as values, fail(), assertions");

section("Result: expected failures as values");
syntax(["type Result<T, E> = { ok: true; value: T } | { ok: false; error: E }", "the caller must check ok before it can read value"]);
const good = parsePort("8080");
const bad = parsePort("abc");
show(() => (good.ok ? good.value : good.error.message));
show(() => (bad.ok ? bad.value : bad.error.message));
const attempted = attempt(() => JSON.parse("{"));
show(() => attempted.ok);                               // the SyntaxError became data instead of an exception
show(() => attempt(() => 42));
wontCompile("console.log(good.value);", "TS2339: Property 'value' does not exist on type 'Result<number, ValidationError>'");
note("Exceptions for the unexpected (bugs, I/O), Result for expected failures at one call site (parsing input); do not mix them in one API");

section("never and assertion functions");
syntax(
  ["function fail(message: string): never", "never: it throws, so `x ?? fail(...)` has the type of x without undefined"],
  ["function assertDefined<T>(v: T): asserts v is NonNullable<T>", "after the call the value is narrowed (declare it with function: an untyped const arrow gives TS2775)"],
);
const settings: { port?: number } = { port: 8080 };
const port = settings.port ?? fail("port is required");   // type number, not number | undefined
show(() => port);
const empty: { port?: number } = {};
show(() => thrown(() => empty.port ?? fail("port is required")));   // the right side runs only when the left one is missing
const text = maybe("hi");
assertDefined(text, "text");
show(() => text.length);                                // narrowed to string by the assertion
show(() => thrown(() => assertDefined(maybe(""), "text")));
wontCompile("const len = maybe('hi').length;", "TS2532: Object is possibly 'undefined'");
wontCompile("const p: number = settings.port;", "TS2322: Type 'number | undefined' is not assignable to type 'number'");
