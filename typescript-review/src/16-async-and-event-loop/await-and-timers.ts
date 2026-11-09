/**
 * 16c. await AND TIMERS: what await does to a function, and how timers are ordered
 *      Run: npx tsx src/16-async-and-event-loop/await-and-timers.ts        (add --plain for ASCII output)
 *      Every demo records events into an array and shows the final ORDER (never a time).
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

/**
 * Runs body inside a timer callback (a fresh macrotask: the textbook starting point), then waits flushMs
 * so every callback it queued (the longest timer in a demo is shorter than that) has run. Returns the recorded events.
 */
function inMacrotask(body: (log: string[]) => void, flushMs = 120): Promise<string[]> {
  const log: string[] = [];
  return new Promise((resolve) => {
    setTimeout(() => {
      body(log);
      setTimeout(() => resolve(log), flushMs);
    }, 0);
  });
}

title("await AND TIMERS  -  splitting a function, ordering by due time");

section("await splits a function in two");
syntax(["await x", "everything after it becomes a microtask: the function returns to its caller at the await"]);
const split = await inMacrotask((log) => {
  async function a(): Promise<void> {
    log.push("a before await");
    await null;                                         // even a non-promise value pauses the function for one microtask tick
    log.push("a after await");
  }
  log.push("start");
  void a();
  log.push("end");
});
show(() => split);                                      // the caller's "end" comes before "a after await"
const nested = await inMacrotask((log) => {
  async function one(): Promise<void> {
    log.push("one start");
    await two();
    log.push("one end");
  }
  async function two(): Promise<void> {
    log.push("two");                                    // the body of an async function runs synchronously up to its first await
  }
  void one();
  log.push("sync after");
});
show(() => nested);                                     // two runs inside one's first line, but one's second half waits
const classic = await inMacrotask((log) => {
  async function async1(): Promise<void> {
    log.push("async1 start");
    await async2();
    log.push("async1 end");
  }
  async function async2(): Promise<void> {
    log.push("async2");
  }
  log.push("script start");
  setTimeout(() => log.push("setTimeout"), 0);
  void async1();
  new Promise<void>((resolve) => {
    log.push("promise1");
    resolve();
  }).then(() => log.push("promise2"));
  log.push("script end");
});
show(() => classic);                                    // THE interview question: 'async1 end' before 'promise2' on modern V8
const chains = await inMacrotask((log) => {
  async function f(): Promise<void> {
    log.push("f1");
    await Promise.resolve();
    log.push("f2");
    await Promise.resolve();
    log.push("f3");
  }
  void f();
  Promise.resolve().then(() => log.push("p1")).then(() => log.push("p2")).then(() => log.push("p3"));
});
show(() => chains);                                     // two chains of one-tick steps alternate: f2 p1 f3 p2 p3
wontCompile("function f() { await g(); }", "TS1308: 'await' expressions are only allowed within async functions and at the top levels of modules");

section("Timers: ordered by due time, not by registration");
const byDelay = await inMacrotask((log) => {
  setTimeout(() => log.push("80 ms"), 80);
  setTimeout(() => log.push("40 ms"), 40);
  setTimeout(() => log.push("0 ms"), 0);
});
show(() => byDelay);                                    // registered 80, 40, 0: they run 0, 40, 80
const nestedTimers = await inMacrotask((log) => {
  setTimeout(() => {
    log.push("outer 40");
    setTimeout(() => log.push("inner 0"), 0);           // a new timer, due about 1 ms from now
  }, 40);
  setTimeout(() => log.push("second 140"), 140);
}, 200);
show(() => nestedTimers);                               // the inner 0 ms timer is due long before the 140 ms one
const sameTime = await inMacrotask((log) => {
  setTimeout(() => {
    log.push("t1");
    Promise.resolve().then(() => log.push("t1 microtask"));
  }, 40);
  setTimeout(() => log.push("t2"), 40);
});
show(() => sameTime);                                   // two timers due together: the microtask of t1 still runs between them
note("setTimeout(fn, 0) means 'as soon as possible after the current code and its microtasks', never 'now'");
note("A timer is a MINIMUM delay: a busy main thread delays it");
note("Do not rely on the order of setTimeout(fn, 0) versus setImmediate(fn) in the main module: it is not guaranteed");
