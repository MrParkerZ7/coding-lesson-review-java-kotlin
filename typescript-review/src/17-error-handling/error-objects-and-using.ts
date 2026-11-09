/**
 * 17b. ERROR OBJECTS AND CLEANUP: name, message, stack, cause, AggregateError, using
 *      Run: npx tsx src/17-error-handling/error-objects-and-using.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title } from "../output";

/** A resource whose [Symbol.dispose] runs when a `using` variable leaves its block. */
class Resource {
  constructor(
    readonly name: string,
    private readonly log: string[],
  ) {}
  [Symbol.dispose](): void {
    this.log.push(`close ${this.name}`);
  }
}
title("ERROR OBJECTS AND CLEANUP  -  what an Error holds, and using");

section("Error objects: name, message, stack, cause, AggregateError");
const err = new Error("boom");
show(() => err.name);
show(() => err.message);
show(() => String(err));                                // name: message
show(() => err.stack?.split("\n")[0]);                  // the stack text starts with the same line
show(() => Object.keys(err));                           // TRAP: name, message and stack are not enumerable (so JSON.stringify gives {})
const inner = new Error("disk full");
const outer = new Error("save failed", { cause: inner });   // cause (ES2022): wrap an error WITHOUT losing the original
show(() => outer.cause === inner);
show(() => (outer.cause as Error).message);
const many = new AggregateError([new Error("a"), new Error("b")], "two failed");
show(() => many.errors.map((e: Error) => e.message));   // AggregateError holds several errors (Promise.any rejects with one)
show(() => many instanceof Error);

section("using: cleanup that cannot be forgotten (try-with-resources)");
syntax(["using name = resource", "calls resource[Symbol.dispose]() when the block ends, in reverse order (TS 5.2+)"]);
const useLog: string[] = [];
function work(fail: boolean): void {
  using first = new Resource("first", useLog);
  using second = new Resource("second", useLog);
  useLog.push("work");
  if (fail) throw new Error("boom");
}
work(false);
show(() => useLog);                                     // work, then the resources close last-opened first
const failUseLog: string[] = [];
function failingWork(): void {
  using first = new Resource("first", failUseLog);
  failUseLog.push("work");
  throw new Error("boom");
}
show(() => thrown(failingWork));                        // the error still travels ...
show(() => failUseLog);                                 // ... after the resource was closed
note("Same idea as Java try-with-resources and Kotlin use { }; the manual form is try { } finally { r.close() }");
