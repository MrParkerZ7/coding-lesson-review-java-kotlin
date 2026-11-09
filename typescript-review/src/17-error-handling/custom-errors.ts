/**
 * 17c. CUSTOM ERRORS: subclasses of Error, name, codes, cause chains, JSON.stringify
 *      Run: npx tsx src/17-error-handling/custom-errors.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title } from "../output";

/** Forgot to set the name: the class name is NOT used. */
class PlainError extends Error {}

/** The base of every error this app throws on purpose: a stable code, an optional cause, a JSON shape. */
class AppError extends Error {
  constructor(
    message: string,
    readonly code: string,
    options?: ErrorOptions,
  ) {
    super(message, options);
    this.name = new.target.name;                        // "AppError", "ValidationError", ... one line for every subclass
  }
  toJSON(): { name: string; code: string; message: string } {
    return { name: this.name, code: this.code, message: this.message };
  }
}

class ValidationError extends AppError {
  constructor(
    readonly field: string,
    message: string,
  ) {
    super(message, "E_VALIDATION");
  }
}

class NotFoundError extends AppError {
  constructor(what: string, options?: ErrorOptions) {
    super(`${what} not found`, "E_NOT_FOUND", options);
  }
}

/** An error with its own field but no toJSON. */
class LooseError extends Error {
  constructor(
    message: string,
    readonly code: string,
  ) {
    super(message);
  }
}

/** Most specific class first: an AppError check on top would catch its subclasses too. */
function describe(error: unknown): string {
  if (error instanceof ValidationError) return `fix field ${error.field}`;
  if (error instanceof NotFoundError) return `404: ${error.message}`;
  if (error instanceof AppError) return `app error ${error.code}`;
  if (error instanceof Error) return `unexpected: ${error.name}`;
  return "something that is not an Error was thrown";
}

function loadConfig(): never {
  try {
    JSON.parse("{");
  } catch (error) {
    throw new AppError("cannot load config", "E_CONFIG", { cause: error });
  }
  throw new Error("unreachable");
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

/** Follow the cause links from the outermost error to the root cause. */
function chainOf(error: unknown): string[] {
  const names: string[] = [];
  let current: unknown = error;
  while (current instanceof Error) {
    names.push(current.name);
    current = current.cause;
  }
  return names;
}

title("CUSTOM ERRORS  -  your own error types");

section("Extending Error");
syntax(
  ["class MyError extends Error { constructor(...) { super(message); this.name = ... } }", "call super(message); set name yourself, or it stays 'Error'"],
  ["new.target.name", "the name of the class being constructed: one line in the base class names every subclass"],
);
show(() => new PlainError("x").name);                   // TRAP: still "Error", the class name is not copied
show(() => String(new PlainError("x")));                // Error: x, which hides the real type in logs
show(() => new PlainError("x").constructor.name);       // the class is there, but name and String() do not use it
const invalid = new ValidationError("email", "email is required");
show(() => invalid.name);                               // new.target.name gave the subclass name
show(() => invalid instanceof ValidationError);
show(() => invalid instanceof AppError);                // a subclass is also its base class
show(() => invalid instanceof Error);
show(() => invalid.field);
show(() => invalid.code);
const lookalike: PlainError = new Error("x");           // compiles: a subclass with no new members has the same shape as Error
show(() => lookalike instanceof PlainError);            // TRAP: TypeScript compares shapes, instanceof compares prototypes: false
note("Compiling to ES5 breaks instanceof for Error subclasses (tested: false, and the subclass methods vanish): add Object.setPrototypeOf(this, new.target.prototype); modern targets do not need it");

section("Telling errors apart");
show(() => describe(new ValidationError("age", "too young")));
show(() => describe(new NotFoundError("user")));
show(() => describe(new AppError("generic", "E_GENERIC")));
show(() => describe(new TypeError("bad")));
show(() => describe("just a string"));                  // catch variables are unknown: anything can arrive
note("Prefer a code field (switch on error.code) over instanceof when errors cross package or realm boundaries: two copies of a class are two classes");

section("Wrapping with cause");
syntax(["new Error(message, { cause: original })", "keep the original error attached; read it back from error.cause"]);
const wrapped = new NotFoundError("config", { cause: new RangeError("inner") });
show(() => wrapped.cause instanceof RangeError);
show(() => chainOf(wrapped));                           // outermost first
show(() => thrown(loadConfig));                         // loadConfig throws an AppError that wraps the SyntaxError from JSON.parse
show(() => chainOf(caught(loadConfig)));                // the chain: AppError, then its cause SyntaxError

section("JSON.stringify and errors");
show(() => JSON.stringify(new Error("x")));             // TRAP: {} - name, message and stack are not enumerable
show(() => JSON.stringify(new LooseError("x", "E_LOOSE")));   // only the OWN enumerable field survives: the message is lost
show(() => JSON.stringify(invalid));                    // toJSON decides what to serialize
note("Log errors with their message and stack as text, and send clients a deliberate shape (code, message), never the raw object");
