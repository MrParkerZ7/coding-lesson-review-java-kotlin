/**
 * 16g. CALLBACKS TO PROMISES: error-first callbacks, util.promisify, wrapping with new Promise, callback hell, "Zalgo"
 *      Run: npx tsx src/16-async-and-event-loop/callbacks-to-promises.ts        (add --plain for ASCII output)
 */
import { readFile } from "node:fs";
import { EventEmitter, once } from "node:events";
import { promisify } from "node:util";
import { note, section, show, syntax, title } from "../output";

const sleep = (ms: number): Promise<void> => new Promise((resolve) => setTimeout(resolve, ms));

/** Node's callback convention: error first (null when fine), then the result. */
type Callback<T> = (error: Error | null, value?: T) => void;

interface User {
  id: number;
  name: string;
}

function loadUser(id: number, cb: Callback<User>): void {
  setTimeout(() => (id > 0 ? cb(null, { id, name: `user${id}` }) : cb(new Error(`no user ${id}`))), 10);
}
function loadOrders(userId: number, cb: Callback<number[]>): void {
  setTimeout(() => cb(null, [userId * 10, userId * 5]), 10);
}
function sumOrders(orders: number[], cb: Callback<number>): void {
  setTimeout(() => cb(null, orders.reduce((a, b) => a + b, 0)), 10);
}

/** The hand-made util.promisify for a one-argument callback API: settle the promise from inside the callback. */
function promisify1<A, T>(fn: (arg: A, cb: Callback<T>) => void): (arg: A) => Promise<T> {
  return (arg) =>
    new Promise<T>((resolve, reject) => {
      fn(arg, (error, value) => (error ? reject(error) : resolve(value as T)));
    });
}

const loadUserP = promisify1(loadUser);
const loadOrdersP = promisify1(loadOrders);
const sumOrdersP = promisify1(sumOrders);

/** The same three steps as the pyramid below, flat: one try / catch covers every step. */
async function owed(id: number): Promise<string> {
  const user = await loadUserP(id);
  const orders = await loadOrdersP(user.id);
  return `${user.name} owes ${await sumOrdersP(orders)}`;
}

/** Sometimes synchronous (cache hit), sometimes asynchronous (miss): the callback order depends on the data. */
const cache = new Map<string, string>();
function getCached(key: string, cb: (value: string) => void): void {
  const hit = cache.get(key);
  if (hit !== undefined) {
    cb(hit);                                            // synchronous: runs before getCached returns
    return;
  }
  setTimeout(() => {
    cache.set(key, `value of ${key}`);
    cb(`value of ${key}`);                              // asynchronous: runs later
  }, 10);
}

/** A promise API is ALWAYS asynchronous: then callbacks wait for the microtask queue, hit or miss. */
function getCachedPromise(key: string): Promise<string> {
  return Promise.resolve(cache.get(key) ?? `value of ${key}`);
}

title("CALLBACKS TO PROMISES  -  old APIs in new code");

section("Callback hell: the pyramid");
syntax(["fn(args, (err, value) => { if (err) ...; next(value, (err2, v2) => { ... }) })", "every step nests one level deeper and repeats the error check"]);
const pyramid: string[] = [];
const { promise: finished, resolve: done } = Promise.withResolvers<void>();
const report = (text: string): void => {
  pyramid.push(text);
  done();
};
loadUser(1, (error1, user) => {
  if (error1 || !user) return report(`error: ${error1?.message}`);
  loadOrders(user.id, (error2, orders) => {
    if (error2 || !orders) return report(`error: ${error2?.message}`);
    sumOrders(orders, (error3, total) => {
      if (error3 || total === undefined) return report(`error: ${error3?.message}`);
      report(`${user.name} owes ${total}`);
    });
  });
});
await finished;                                         // a callback chain has no promise to await: this one is made by hand
show(() => pyramid);
note("Three problems: the code walks to the right, each level repeats its error check, and nothing can simply await the result");

section("Wrapping a callback API with new Promise");
syntax(
  ["new Promise<T>((resolve, reject) => fn(args, (err, v) => err ? reject(err) : resolve(v)))", "settle the promise from inside the callback"],
  ["const p = promisify(fn)", "node:util does the same for any error-first (err, value) callback API"],
);
const flat = await owed(1);
show(() => flat);                                       // same answer as the pyramid, read top to bottom
const failed = await owed(-1).catch((e: Error) => `caught once: ${e.message}`);
show(() => failed);                                     // one catch for the whole chain
const twice = await new Promise<number>((resolve) => {
  resolve(1);
  resolve(2);                                           // ignored: a promise settles only once
});
show(() => twice);
const executorThrow = await new Promise<number>(() => {
  throw new Error("thrown in the executor");
}).catch((e: Error) => e.message);
show(() => executorThrow);                              // a throw inside the executor becomes a rejection
note("In a wrapper call reject on error and return: forgetting the reject leaves the promise pending forever");

section("util.promisify and the promise versions in node");
const waitFor = promisify(setTimeout);                  // setTimeout has a special promisify: (ms, value) => Promise<value>
const waited = await waitFor(10, "value");            // resolves with the second argument after 10 ms
show(() => waited);
const readFileP = promisify(readFile);                  // fs.readFile is a plain error-first API
const code = await readFileP("definitely-missing-file.txt").catch((e: NodeJS.ErrnoException) => e.code);
show(() => code);                                       // ENOENT: the callback's error became the rejection
const emitter = new EventEmitter();
setTimeout(() => emitter.emit("ready", 42), 10);
const [payload] = await once(emitter, "ready");         // an event as a promise: resolves with the argument list
show(() => payload);
note("Modern node ships promise versions: node:fs/promises, node:timers/promises, node:dns/promises: prefer them over promisify");
note("promisify on a callback typed with an optional value gives Promise<T | undefined>: type your own wrapper (promisify1) when that matters");

section("Zalgo: a callback that is sometimes sync and sometimes async");
syntax(["cb(value)  vs  setTimeout(() => cb(value))", "the same function must pick ONE: always sync or always async"]);
const miss: string[] = [];
miss.push("before");
getCached("a", () => miss.push("callback"));
miss.push("after");
await sleep(50);
show(() => miss);                                       // first call (cache miss): the callback is async, after "after"
const hit: string[] = [];
hit.push("before");
getCached("a", () => hit.push("callback"));
hit.push("after");
show(() => hit);                                        // second call (cache hit): the callback runs BEFORE "after"
const promised: string[] = [];
promised.push("before");
const pending = getCachedPromise("a").then(() => promised.push("then"));
promised.push("after");
await pending;
show(() => promised);                                   // a promise-returning API is always async: one predictable order
note("Fix for callbacks: queueMicrotask(() => cb(hit)) or process.nextTick; better, return a promise so the problem cannot exist");
