/**
 * 16f. ASYNC LOOPS AND CANCELLATION: forEach vs for...of vs map + Promise.all, for await + async generators, AbortController
 *      Run: npx tsx src/16-async-and-event-loop/async-loops-and-cancel.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

const later = <T>(ms: number, value: T): Promise<T> => new Promise((resolve) => setTimeout(resolve, ms, value));
const sleep = (ms: number): Promise<void> => later(ms, undefined);

/** An async generator: yields a number every 40 ms; the finally block is its cleanup. */
async function* ticker(count: number, log: string[]): AsyncGenerator<number, void, undefined> {
  try {
    for (let i = 1; i <= count; i++) {
      await sleep(40);
      yield i;
    }
  } finally {
    log.push("ticker cleanup");
  }
}

/** A sleep that can be cancelled: it rejects with the signal's reason and clears its timer. */
function cancellableSleep(ms: number, signal: AbortSignal): Promise<string> {
  return new Promise((resolve, reject) => {
    if (signal.aborted) {
      reject(signal.reason);
      return;
    }
    const timer = setTimeout(() => {
      signal.removeEventListener("abort", onAbort);
      resolve("finished");
    }, ms);
    function onAbort(): void {
      clearTimeout(timer);
      reject(signal.reason);
    }
    signal.addEventListener("abort", onAbort, { once: true });
  });
}

title("ASYNC LOOPS AND CANCELLATION");

section("Looping with await: forEach does NOT wait");
syntax(
  ["for (const x of xs) { await work(x) }", "sequential: one at a time, in order; break and return work"],
  ["await Promise.all(xs.map(async (x) => work(x)))", "parallel: all started together, results in input order"],
  ["xs.forEach(async (x) => ...)", "BUG: forEach ignores the returned promises, so nothing waits and errors are lost"],
);
const forEachLog: string[] = [];
[1, 2, 3].forEach(async (n) => {
  await sleep(40 * n);
  forEachLog.push(`item ${n}`);
});
forEachLog.push("after forEach");
const rightAfter = [...forEachLog];                     // a copy taken immediately: the callbacks have not finished
show(() => rightAfter);                                 // forEach returned at once; the work is still pending
await sleep(200);
show(() => forEachLog);                                 // the items arrive later, after the code that thought it was done
const sequentialLog: string[] = [];
for (const n of [1, 2, 3]) {
  await sleep(40);
  sequentialLog.push(`item ${n}`);
}
sequentialLog.push("after loop");
show(() => sequentialLog);                              // for...of waits: "after loop" really is after
const parallelLog: string[] = [];
const squares = await Promise.all(
  [1, 2, 3].map(async (n) => {
    await sleep(40);
    parallelLog.push(`item ${n}`);
    return n * n;
  }),
);
show(() => squares);                                    // the three waits overlapped, and the results are in input order
show(() => parallelLog);
const promises = [1, 2, 3].map(async (n) => n * 2);
show(() => promises);                                   // TRAP: map with an async callback gives an array of PROMISES, not numbers
const values = await Promise.all(promises);
show(() => values);                                     // Promise.all turns Promise<number>[] into number[]
note("Reading the rule: need the results or the ordering, use for...of or Promise.all; forEach is for sync callbacks only");

section("Async generators and for await...of");
syntax(
  ["async function* name() { yield value }", "an async generator: each yield may come after an await"],
  ["for await (const v of asyncIterable) { ... }", "awaits each item in turn (also works on an array of promises)"],
);
const seen: number[] = [];
const fullLog: string[] = [];
for await (const n of ticker(3, fullLog)) seen.push(n);
show(() => seen);                                       // 1, 2, 3, one every 40 ms
show(() => fullLog);                                    // the generator's finally ran when it finished
const early: number[] = [];
const breakLog: string[] = [];
for await (const n of ticker(5, breakLog)) {
  early.push(n);
  if (n === 2) break;                                   // leaving the loop early calls the generator's return()
}
show(() => early);
show(() => breakLog);                                   // cleanup ran although the generator was not exhausted
const arrivals: string[] = [];
for await (const v of [later(80, "slow"), later(40, "fast")]) arrivals.push(v);
show(() => arrivals);                                   // array order, not completion order: for await takes them one by one
wontCompile("function f() { for await (const x of gen()) { } }", "TS1103: 'for await' loops are only allowed within async functions and at the top levels of modules");
wontCompile("for (const x of gen()) { }", "TS2488: Type 'AsyncGenerator<number, any, any>' must have a '[Symbol.iterator]()' method that returns an iterator");
note("Streams, pagination and message queues are async iterables; Array.fromAsync(asyncIterable) collects one into an array (lesson 19)");

section("Cancellation with AbortController");
syntax(
  ["const controller = new AbortController()", "controller.signal goes to the work; controller.abort() cancels it"],
  ["signal.addEventListener('abort', ...)  |  signal.aborted  |  signal.reason", "how the work learns it was cancelled, and why"],
  ["AbortSignal.timeout(ms)", "a signal that aborts itself after ms: a built-in timeout"],
);
const controller = new AbortController();
const work = cancellableSleep(200, controller.signal);
setTimeout(() => controller.abort(), 40);               // someone cancels while the work is pending
const cancelled = await work.catch((e: Error) => e.name);
show(() => cancelled);                                  // the default reason is a DOMException named AbortError
show(() => controller.signal.aborted);
show(() => thrown(() => controller.signal.throwIfAborted()));   // the one-liner check inside long work: throws the reason
const custom = new AbortController();
const stopped = cancellableSleep(200, custom.signal);
custom.abort(new Error("user clicked cancel"));         // any value can be the reason
const reason = await stopped.catch((e: Error) => e.message);
show(() => reason);                                     // the reason arrives as the rejection value
const timedOut = await cancellableSleep(200, AbortSignal.timeout(40)).catch((e: Error) => e.name);
show(() => timedOut);                                   // TimeoutError: AbortSignal.timeout uses its own reason
const completed = await cancellableSleep(40, new AbortController().signal);
show(() => completed);                                  // never aborted: the work finishes normally
note("fetch(url, { signal }), fs promises and timers/promises accept a signal; the work must LISTEN for it, abort() cancels nothing by itself");
note("Promise.race with a timer gives up WAITING (async-patterns); AbortController stops the WORK");
