/**
 * 16a. PROMISES: states, then / catch / finally, async / await, all / allSettled / race / any, withResolvers
 *      Run: npx tsx src/16-async-and-event-loop/promises.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** Resolves with value after ms milliseconds (the delays below are 40 ms apart, so the ORDER is stable). */
const sleep = <T>(ms: number, value: T): Promise<T> => new Promise((resolve) => setTimeout(resolve, ms, value));

/** Rejects with an Error after ms milliseconds. */
const failAfter = (ms: number, message: string): Promise<never> =>
  new Promise((_, reject) => setTimeout(reject, ms, new Error(message)));

/** An async function never throws to its caller: the error becomes a rejection. */
async function boom(): Promise<number> {
  throw new Error("boom");
}

/** Logs when it starts and when it ends, so the order of events can be shown. */
async function task(name: string, ms: number, log: string[]): Promise<string> {
  log.push(`start ${name}`);
  await sleep(ms, undefined);
  log.push(`end ${name}`);
  return name;
}

title("PROMISES  -  a value that arrives later");

section("A promise has three states, and the executor runs at once");
syntax(
  ["new Promise<T>((resolve, reject) => { ... })", "the executor runs SYNCHRONOUSLY inside the constructor"],
  ["pending -> fulfilled(value) | rejected(reason)", "settled once; it never changes again"],
);
const log: string[] = [];
const started = new Promise<number>((resolve) => {
  log.push("executor");
  resolve(1);
});
log.push("after constructor");
await started.then((v) => log.push(`then ${v}`));       // the callback runs later: after the code that follows the constructor
show(() => log);                                        // executor first, then the line after new Promise, then the callback
show(() => new Promise(() => {}));                      // never settled: Promise { <pending> }
show(() => Promise.resolve(42));                        // Promise { 42 }: fulfilled with a value
const rejected = Promise.reject("nope");
rejected.catch(() => {});                               // a rejection nobody handles crashes node: attach a handler first
show(() => rejected);                                   // Promise { <rejected> 'nope' }
wontCompile("const n: number = Promise.resolve(1);", "TS2322: Type 'Promise<number>' is not assignable to type 'number'");
note("The promise is eager: the work starts when you create it, not when you call then or await");

section("then / catch / finally: every call returns a NEW promise");
syntax(
  ["p.then(onFulfilled, onRejected?)", "the return value of the callback becomes the value of the next promise"],
  ["p.catch(onRejected)", "same as then(undefined, onRejected): a returned value RECOVERS the chain"],
  ["p.finally(onSettled)", "no argument, and its return value is ignored (a throw still replaces the result)"],
);
const chained = await Promise.resolve(1).then((x) => x + 1).then((x) => x * 10);
show(() => chained);                                    // 20: each then receives the previous return value
const flattened = await Promise.resolve(1).then((x) => sleep(40, x + 100));
show(() => flattened);                                  // 101: returning a promise makes the chain wait for it
const lost = await Promise.resolve(1).then((x) => {
  x + 1;
});
show(() => lost);                                       // undefined: TRAP, a block body without return passes undefined on
const recovered = await Promise.reject(new Error("bad")).catch((e: Error) => `recovered from ${e.message}`);
show(() => recovered);                                  // catch returned a value, so the chain is fulfilled again
const kept = await Promise.resolve("kept").finally(() => "ignored");
show(() => kept);                                       // finally does not change the value
const thrownInThen = await Promise.resolve(1)
  .then(() => {
    throw new Error("in then");
  }, () => "never runs")
  .catch((e: Error) => `caught: ${e.message}`);
show(() => thrownInThen);                               // TRAP: the second argument of then does not catch what the first one throws

section("async / await: the same promises with synchronous-looking code");
syntax(
  ["async function f(): Promise<T> { ... }", "always returns a promise; a returned value is wrapped, a throw becomes a rejection"],
  ["const v = await promise", "pause THIS function, give the thread back, resume with the value (or throw the reason)"],
);
show(() => thrown(() => boom().catch(() => {})));       // calling it does not throw: it returns a promise that is already rejected
const outcome = await boom().then(() => "ok", (e: unknown) => (e instanceof Error ? e.message : "?"));
show(() => outcome);                                    // the error arrives as the rejection reason
note("await is only legal inside an async function or at the top of a module (this file is a module)");

section("Combinators: all, allSettled, race, any");
syntax(
  ["Promise.all([...])", "all fulfilled -> array in INPUT order; the first rejection rejects the whole thing"],
  ["Promise.allSettled([...])", "never rejects: an array of { status, value | reason }"],
  ["Promise.race([...]) | Promise.any([...])", "first to SETTLE | first to FULFIL (rejects with AggregateError if all fail)"],
);
const all = await Promise.all([sleep(120, "a"), sleep(40, "b"), sleep(80, "c")]);
show(() => all);                                        // a, b, c: the order of the input, not of completion
const allFailed = await Promise.all([sleep(120, "slow"), failAfter(40, "boom")]).catch((e: Error) => e.message);
show(() => allFailed);                                  // boom: all rejects as soon as one rejects (the other result is dropped)
const settled = await Promise.allSettled([sleep(40, "a"), failAfter(80, "b failed")]);
show(() => settled.map((r) => r.status));               // both outcomes are kept
show(() => settled.map((r) => (r.status === "fulfilled" ? r.value : (r.reason as Error).message)));
const raced = await Promise.race([sleep(120, "slow"), sleep(40, "fast")]);
show(() => raced);                                      // the first to settle wins; the loser keeps running but nobody listens
const anyOk = await Promise.any([failAfter(40, "x"), sleep(80, "ok")]);
show(() => anyOk);                                      // any skips rejections and takes the first success
const noneOk = await Promise.any([failAfter(40, "x"), failAfter(80, "y")]).catch((e: unknown) => e);
show(() => noneOk instanceof AggregateError);           // every promise failed: an AggregateError with all the reasons
show(() => (noneOk as AggregateError).errors.length);

section("Sequential vs parallel: the recorded order shows the difference");
const sequential: string[] = [];
await task("A", 80, sequential);
await task("B", 40, sequential);                        // B starts only after A has finished
show(() => sequential);
const parallel: string[] = [];
await Promise.all([task("A", 80, parallel), task("B", 40, parallel)]);   // both started first, then awaited together
show(() => parallel);                                   // B ends before A: the waits overlap
note("Independent awaits in a row waste time: start them first, await them together");

section("Promise.withResolvers (ES2024, Node 22+)");
syntax(["const { promise, resolve, reject } = Promise.withResolvers<T>()", "a promise plus its settle functions, no executor needed"]);
const { promise, resolve } = Promise.withResolvers<string>();
setTimeout(resolve, 40, "from outside");                // settle it from any other place: a timer, an event handler
const external = await promise;
show(() => external);
