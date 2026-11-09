/**
 * 16b. THE EVENT LOOP: who runs first? sync code, process.nextTick, promise microtasks, timers
 *      Run: npx tsx src/16-async-and-event-loop/event-loop-order.ts        (add --plain for ASCII output)
 *      Every demo records events into an array and shows the final ORDER (never a time).
 */
import { note, section, show, syntax, title } from "../output";

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

const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

title("EVENT LOOP  -  the order of everything that is not plain code");

section("The queues");
syntax(
  ["call stack", "synchronous code: runs to the end first, nothing can interrupt it"],
  ["process.nextTick queue", "Node only: callbacks run right after the current code, before any promise callback"],
  ["microtask queue", "promise reactions (then / catch / finally / the code after an await) and queueMicrotask"],
  ["timer queue (macrotasks)", "setTimeout / setInterval: ONE callback per turn, ordered by due time, then by registration"],
  ["the loop", "run code, then drain nextTick, then drain microtasks (repeat until both are empty), then the next macrotask"],
);
note("JavaScript has one thread: nothing runs in parallel with your code; waiting is just a callback queued for later");

section("The textbook order, inside a timer callback");
const basic = await inMacrotask((log) => {
  log.push("sync 1");
  setTimeout(() => log.push("timeout 0"), 0);
  process.nextTick(() => log.push("nextTick"));
  Promise.resolve().then(() => log.push("promise.then"));
  queueMicrotask(() => log.push("queueMicrotask"));
  log.push("sync 2");
});
show(() => basic);                                      // sync, nextTick, promises and microtasks in queue order, the timer last
const interleave = await inMacrotask((log) => {
  Promise.resolve().then(() => log.push("p1")).then(() => log.push("p2"));
  queueMicrotask(() => log.push("q1"));
  Promise.resolve().then(() => log.push("p3"));
});
show(() => interleave);                                 // p2 joins the END of the queue only after p1 has run: q1 and p3 go first
const tickInside = await inMacrotask((log) => {
  Promise.resolve().then(() => { log.push("p1"); process.nextTick(() => log.push("tick inside p1")); }).then(() => log.push("p2"));
});
show(() => tickInside);                                 // a nextTick queued by a microtask waits until the microtask queue is empty
const microInside = await inMacrotask((log) => {
  process.nextTick(() => { log.push("t1"); Promise.resolve().then(() => log.push("promise from t1")); });
  process.nextTick(() => log.push("t2"));
});
show(() => microInside);                                // the whole nextTick queue is drained before the promise it created can run
const starve = await inMacrotask((log) => {
  setTimeout(() => log.push("timer 0"), 0);
  let n = 0;
  const loop = (): void => {
    log.push(`micro ${++n}`);
    if (n < 3) queueMicrotask(loop);
  };
  queueMicrotask(loop);
});
show(() => starve);                                     // microtasks that queue microtasks run to the end first: with no limit they would starve the timer
note("Rule of thumb: sync, then nextTick, then promises, then timers. The queue is drained completely before the loop moves on");

section("The main-module surprise: an ES module already runs inside a promise job");
const mainLog: string[] = [];
mainLog.push("sync 1");
process.nextTick(() => mainLog.push("nextTick"));
Promise.resolve().then(() => mainLog.push("promise.then"));
queueMicrotask(() => mainLog.push("queueMicrotask"));
mainLog.push("sync 2");
await sleep(60);
show(() => mainLog);                                    // here the promise callbacks come BEFORE nextTick: the reverse of the previous demo
note("An ES module body (and any code after an await) runs inside a microtask, so the queue being drained is the promise one");
note("A plain CommonJS script run with node (node file.cjs) prints nextTick first: the textbook order needs a macrotask start");
note("In an interview say the textbook order, then add: 'inside a promise job, microtasks win'");
