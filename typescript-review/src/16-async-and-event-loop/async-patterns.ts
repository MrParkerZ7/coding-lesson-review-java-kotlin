/**
 * 16d. ASYNC PATTERNS: retry with backoff, timeout with race, a concurrency-limited pool
 *      Run: npx tsx src/16-async-and-event-loop/async-patterns.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

const sleep = <T>(ms: number, value: T): Promise<T> => new Promise((resolve) => setTimeout(resolve, ms, value));

/** Retry fn up to attempts times; wait baseMs, 2 x baseMs, 4 x baseMs ... between tries (exponential backoff). */
async function retry<T>(fn: (attempt: number) => Promise<T>, attempts: number, baseMs: number, log: string[]): Promise<T> {
  for (let attempt = 1; ; attempt++) {
    try {
      return await fn(attempt);                         // await INSIDE the try, or the catch would never see the failure
    } catch (error) {
      log.push(`attempt ${attempt} failed`);
      if (attempt >= attempts) throw error;             // out of tries: let the last error through
      const wait = baseMs * 2 ** (attempt - 1);
      log.push(`wait ${wait}`);
      await sleep(wait, undefined);
    }
  }
}

/** Race the work against a timer. The loser is NOT cancelled; the timer is cleared so it cannot keep node alive. */
function withTimeout<T>(work: Promise<T>, ms: number): Promise<T> {
  let timer: ReturnType<typeof setTimeout> | undefined;
  const timeout = new Promise<never>((_, reject) => {
    timer = setTimeout(() => reject(new Error(`timed out after ${ms} ms`)), ms);
  });
  return Promise.race([work, timeout]).finally(() => clearTimeout(timer));
}

/** Run fn over items with at most `limit` running at once; results keep the INPUT order. */
async function mapPool<T, R>(items: T[], limit: number, fn: (item: T) => Promise<R>): Promise<R[]> {
  const results: R[] = new Array(items.length);
  let next = 0;
  async function worker(): Promise<void> {
    while (next < items.length) {
      const index = next++;                             // no await between the check and the increment: no two workers take the same item
      results[index] = await fn(items[index]);
    }
  }
  await Promise.all(Array.from({ length: Math.min(limit, items.length) }, worker));
  return results;
}
title("ASYNC PATTERNS  -  the shapes interviewers ask for");

section("Retry with exponential backoff");
syntax(["for (attempt = 1; ; attempt++) { try { return await fn() } catch { wait; } }", "loop, await inside the try, wait longer each time, rethrow at the end"]);
const flaky = async (attempt: number): Promise<string> => {
  if (attempt < 3) throw new Error("flaky");
  return `ok on attempt ${attempt}`;
};
const retryLog: string[] = [];
const retried = await retry(flaky, 5, 10, retryLog);
show(() => retried);                                    // the third try succeeded
show(() => retryLog);                                   // two failures with waits of 10 and 20 ms (doubling); the third attempt succeeded and logs nothing
const giveUpLog: string[] = [];
const gaveUp = await retry(flaky, 2, 10, giveUpLog).catch((e: Error) => `gave up: ${e.message}`);
show(() => gaveUp);                                     // only 2 attempts allowed: the last error comes out
note("Real code adds jitter (a random extra wait) so many clients do not retry at the same instant; retry only errors worth retrying");

section("Timeout with Promise.race");
syntax(["Promise.race([work, timeout])", "whichever settles first decides; the other one is only ignored"]);
const quick = await withTimeout(sleep(40, "done in time"), 120);
show(() => quick);
const timedOut = await withTimeout(sleep(120, "too late"), 40).catch((e: Error) => e.message);
show(() => timedOut);                                   // the timer won
const lateWork: string[] = [];
const slowWork = sleep(80, "x").then(() => lateWork.push("slow work still finished"));
await withTimeout(slowWork, 40).catch(() => undefined);
await sleep(200, undefined);
show(() => lateWork);                                   // TRAP: race does not cancel the loser; the slow work ran to the end (see AbortController)

section("A pool: at most N tasks at a time");
syntax(["worker loop: while (next < items.length) { const i = next++; results[i] = await fn(items[i]) }", "N workers share one counter; Promise.all waits for all of them"]);
let running = 0;
let peak = 0;
const square = async (n: number): Promise<number> => {
  running++;
  peak = Math.max(peak, running);
  await sleep(40, undefined);
  running--;
  return n * n;
};
const pooled = await mapPool([1, 2, 3, 4, 5, 6], 2, square);
show(() => pooled);                                     // results are in input order although tasks finish in waves
show(() => peak);                                       // never more than 2 at once
peak = 0;
await Promise.all([1, 2, 3, 4, 5, 6].map(square));    // no limit: all 6 start together
const peakUnlimited = peak;
show(() => peakUnlimited);                              // 6 at once
note("Promise.all(items.map(fn)) is fine for 10 requests and a bug for 10 000: bound it, or the server, the sockets and the memory pay");
