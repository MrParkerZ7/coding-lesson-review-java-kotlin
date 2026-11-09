/**
 * 17a. TRY / CATCH / FINALLY, throw, catch what you can handle
 *      Run: npx tsx src/17-error-handling/try-catch-finally.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** Returns the thrown value itself (anything can be thrown), or undefined when nothing was thrown. */
function caught(fn: () => unknown): unknown {
  try {
    fn();
  } catch (error) {
    return error;
  }
  return undefined;
}

function flow(fail: boolean, log: string[]): string {
  try {
    log.push("try");
    if (fail) throw new Error("boom");
    log.push("try end");
    return "returned";
  } catch {
    log.push("catch");
    return "recovered";
  } finally {
    log.push("finally");
  }
}

function returnInFinally(): string {
  try {
    return "from try";
  } finally {
    return "from finally";                              // a return in finally REPLACES the earlier return (and swallows a throw)
  }
}

function capturedBeforeFinally(log: string[]): number {
  let x = 1;
  try {
    return x;                                           // the value 1 is taken here, before finally runs
  } finally {
    x = 2;
    log.push(`finally sees x = ${x}`);
  }
}

function finallyThrows(): never {
  try {
    throw new Error("first");
  } finally {
    throw new Error("second");                          // a throw in finally replaces the error that was on its way
  }
}

const throwString = (): never => {
  throw "just a string";
};
const throwObject = (): never => {
  throw { code: 7 };
};

/** instanceof is the safe way to look into an unknown catch value. */
const messageOf = (error: unknown): string => (error instanceof Error ? error.message : String(error));

function readKind(kind: "syntax" | "range" | "type"): void {
  if (kind === "syntax") JSON.parse("{");
  else if (kind === "range") new Array(-1);
  else (null as unknown as { x: number }).x;
}

/** Handle one kind of error, let every other kind keep travelling. */
function recoverFromSyntax(kind: "syntax" | "range" | "type"): string {
  try {
    readKind(kind);
    return "ok";
  } catch (error) {
    if (error instanceof SyntaxError) return "recovered from SyntaxError";
    throw error;                                        // not ours: rethrow, never swallow what you cannot handle
  }
}

title("ERROR HANDLING  -  try, catch, finally, throw");

section("Flow: the order of try, catch and finally");
syntax(
  ["try { ... } catch (e) { ... } finally { ... }", "finally ALWAYS runs: after try, after catch, even after a return"],
  ["try { ... } catch { ... }", "optional catch binding (ES2019): when you do not need the error"],
);
const okLog: string[] = [];
show(() => flow(false, okLog));                         // no error: try returns, finally still runs
show(() => okLog);
const failLog: string[] = [];
show(() => flow(true, failLog));                        // the catch result is returned, finally runs last
show(() => failLog);

section("finally traps");
show(() => returnInFinally());                          // TRAP: the return in finally wins
const captureLog: string[] = [];
show(() => capturedBeforeFinally(captureLog));          // 1, not 2: the return value was fixed before finally changed x
show(() => captureLog);
show(() => messageOf(caught(finallyThrows)));           // TRAP: the second error replaces the first, which is lost
note("Never return or throw inside finally: use it only for cleanup (closing, releasing, logging)");

section("throw accepts ANY value; the catch variable is unknown");
syntax(
  ["throw new Error('message')", "always throw an Error: only an Error object carries a stack trace"],
  ["catch (error)", "error is unknown under strict (useUnknownInCatchVariables): narrow it before use"],
);
show(() => caught(throwString));                        // a string was thrown: nothing forbids it
show(() => caught(throwObject));                        // so was a plain object
show(() => thrown(throwString));                        // the helper tells it is not an Error
show(() => messageOf(new Error("boom")));
show(() => messageOf("plain text"));                    // the same function copes with a thrown string
wontCompile("try { } catch (e) { console.log(e.message); }", "TS18046: 'e' is of type 'unknown'");
wontCompile("try { } catch (e: string) { }", "TS1196: Catch clause variable type annotation must be 'any' or 'unknown' if specified");
note("catch (e: any) is allowed and turns the check off: prefer unknown plus instanceof");

section("Catch what you can handle, rethrow the rest");
show(() => recoverFromSyntax("syntax"));                // JSON.parse threw a SyntaxError: handled
show(() => thrown(() => recoverFromSyntax("range")));   // a RangeError is not handled here, so it travels on
show(() => thrown(() => recoverFromSyntax("type")));    // same for a TypeError
note("An empty catch { } hides bugs; catching everything and returning a default makes failures look like data");

