/**
 * 16e. ERRORS AROUND await: try / catch / finally, "return await", and the missing-await bug
 *      Run: npx tsx src/16-async-and-event-loop/async-error-handling.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

const sleep = <T>(ms: number, value: T): Promise<T> => new Promise((resolve) => setTimeout(resolve, ms, value));

/** Rejects with an Error after ms milliseconds. */
const failLater = (ms: number, message: string): Promise<string> =>
  new Promise((_, reject) => setTimeout(reject, ms, new Error(message)));

async function withoutAwait(): Promise<string> {
  try {
    return failLater(40, "boom");                       // returns the promise itself: the function is already out of the try when it rejects
  } catch {
    return "caught inside";
  }
}

async function withAwait(): Promise<string> {
  try {
    return await failLater(40, "boom");                 // await rethrows inside the try, so the catch runs
  } catch {
    return "caught inside";
  }
}

async function load(log: string[], fail: boolean): Promise<string> {
  log.push("open");
  try {
    await sleep(40, undefined);
    if (fail) throw new Error("bad");
    log.push("read");
    return "data";
  } catch {
    log.push("catch");
    return "fallback";
  } finally {
    log.push("close");                                  // runs on success, on failure and after the return
  }
}
title("ERRORS AROUND await  -  try, catch, finally and the missing await");

section("try / catch / finally around await");
syntax(["try { await x } catch (e) { ... } finally { ... }", "an awaited rejection is thrown at the await, so ordinary try / catch works"]);
const okLog: string[] = [];
await load(okLog, false);
show(() => okLog);
const failLog: string[] = [];
await load(failLog, true);
show(() => failLog);                                    // the catch gave the fallback, finally still closed
const escaped = await withoutAwait().catch((e: Error) => `escaped the catch: ${e.message}`);
show(() => escaped);                                    // TRAP: return promise (no await) inside try skips the catch
const handled = await withAwait();
show(() => handled);                                    // return await inside try: the catch runs
const unhandled: string[] = [];
const listener = (reason: unknown): void => {
  unhandled.push(reason instanceof Error ? reason.message : String(reason));
};
process.on("unhandledRejection", listener);
try {
  failLater(40, "forgot to await");                     // no await, no .catch: nobody is listening when it rejects
} catch {
  unhandled.push("sync catch ran");                     // never runs: the failure happens later, outside this try
}
await sleep(100, undefined);
process.off("unhandledRejection", listener);
show(() => unhandled);                                  // only the unhandledRejection event saw it (without a listener node exits with an error)
note("A missing await is the #1 async bug: the work is started, its failure is lost, and the next line runs too early");
