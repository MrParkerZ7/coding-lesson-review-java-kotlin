/**
 * 8c. EARLY RETURN, GUARD CLAUSES, LABELS AND never
 *     Run: npx tsx src/8-control-flow/early-return.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** Guard clauses: each bad case leaves at once, the main path stays flat. */
function shippingCost(weightKg: number, express: boolean): number {
  if (weightKg <= 0) return 0;
  if (weightKg > 30) return -1;                         // too heavy: refused
  const base = weightKg <= 5 ? 4 : 9;
  return express ? base * 2 : base;
}

/** The same rules with nested ifs: the real work sits three levels deep. */
function shippingNested(weightKg: number, express: boolean): number {
  if (weightKg > 0) {
    if (weightKg <= 30) {
      const base = weightKg <= 5 ? 4 : 9;
      return express ? base * 2 : base;
    } else {
      return -1;
    }
  } else {
    return 0;
  }
}

/** return leaves BOTH loops at once: no flag, no label. */
function findPair(nums: number[], target: number): [number, number] | undefined {
  for (let i = 0; i < nums.length; i++) {
    for (let j = i + 1; j < nums.length; j++) {
      if (nums[i] + nums[j] === target) return [nums[i], nums[j]];
    }
  }
  return undefined;
}

/** for...of: a return leaves the whole function. */
function firstNegativeIndex(xs: number[]): number {
  for (const [i, x] of xs.entries()) if (x < 0) return i;
  return -1;
}

/** TRAP: this return only leaves the callback, forEach goes on, and the function always returns -1. */
function firstNegativeIndexWrong(xs: number[]): number {
  xs.forEach((x, i) => { if (x < 0) return i; });
  return -1;
}

/** In forEach a return acts like continue (it skips the rest of that one callback). */
function sumPositives(xs: number[]): number {
  let total = 0;
  xs.forEach((x) => { if (x <= 0) return; total += x; });
  return total;
}

/** A label lets break leave an outer loop. */
function findCell(grid: number[][], target: number): [number, number] | undefined {
  let found: [number, number] | undefined;
  search: for (let r = 0; r < grid.length; r++) {
    for (let c = 0; c < grid[r].length; c++) {
      if (grid[r][c] === target) { found = [r, c]; break search; }
    }
  }
  return found;
}

/** A label lets continue go to the next round of an outer loop: skip n as soon as one divisor is found. */
function primesBelow(limit: number): number[] {
  const out: number[] = [];
  outer: for (let n = 2; n < limit; n++) {
    for (let d = 2; d * d <= n; d++) if (n % d === 0) continue outer;
    out.push(n);
  }
  return out;
}

const twice = (x: number): number => x * 2;
const abs = (n: number): number => (n < 0 ? -n : n);
const factorial = (n: number): number => (n <= 1 ? 1 : n * factorial(n - 1));   // a recursive arrow needs its return type written

/** never: this function does not return normally, so tsc knows the code after a call is unreachable. */
function fail(message: string): never {
  throw new Error(message);
}
const halve = (n: number): number => (n % 2 === 0 ? n / 2 : fail(`odd: ${n}`));   // never fits every type, so the else branch is still a number
function parseOrFail(s: string): number {
  const n = Number(s);
  return Number.isNaN(n) ? fail(`not a number: ${s}`) : n;
}

/** A path with no return gives undefined: fine for a return type that says so. */
function bigOrNothing(n: number): string | undefined {
  if (n > 5) return "big";
}

title("EARLY RETURN  -  leave as soon as the answer is known");

section("Guard clauses");
syntax(["if (bad case) return value;", "handle the odd cases first and leave; the main path stays flat"]);
show(() => shippingCost(3, false));
show(() => shippingCost(8, true));                      // 9 * 2 for express
show(() => shippingCost(0, true));                      // guard: nothing to ship
show(() => shippingCost(31, false));                    // guard: too heavy
show(() => shippingNested(8, true) === shippingCost(8, true));   // same result, but the nested version is harder to read

section("return, break and continue from loops");
syntax(
  ["return value", "leaves the function, from any depth of loops"],
  ["label: for (...) { for (...) { break label; | continue label; } }", "leave | continue an OUTER loop"],
  ["xs.forEach((x) => { return; })", "leaves only the callback: it acts like continue, never like break"],
);
show(() => findPair([2, 7, 11, 15], 18));               // found in the inner loop: both loops end at once
show(() => findPair([1, 2], 100));
show(() => firstNegativeIndex([3, 5, -1, 4]));          // the return inside for...of left the whole function
show(() => firstNegativeIndexWrong([3, 5, -1, 4]));     // TRAP: -1, the return inside forEach left only the callback
show(() => sumPositives([3, -1, 4, 0, 2]));             // return skipped -1 and 0 only
show(() => findCell([[1, 2], [3, 4]], 3));              // break search ended both loops
show(() => findCell([[1, 2], [3, 4]], 9));
show(() => primesBelow(20));                            // continue outer skipped to the next n
note("To stop early on an array use for...of, some, every, find or findIndex; forEach cannot break (lesson 2)");

section("Arrow functions and recursion");
syntax(
  ["const f = (x: T): R => expression;", "no braces and no return: the expression is the result"],
  ["function f(n: number): R { ... f(n - 1) ... }", "a recursive function needs its return type written"],
);
show(() => twice(21));
show(() => abs(-8));
show(() => factorial(5));
show(() => factorial(20));                              // 2432902008176640000: still exact, but not a safe integer
show(() => Number.isSafeInteger(factorial(20)));
show(() => factorial(25));                              // TRAP: 1.5511210043330986e+25, doubles lose digits above 2^53 (lesson 0 has bigint)
show(() => factorial(171));                             // Infinity: no overflow wrap-around and no exception
wontCompile("function fact(n: number) { return n <= 1 ? 1 : n * fact(n - 1); }", "TS7023: 'fact' implicitly has return type 'any' because it is referenced in its own return expression");
wontCompile("function grade(n: number): string { if (n > 5) return \"hi\"; }", "TS2366: Function lacks ending return statement and return type does not include 'undefined'");
show(() => bigOrNothing(9));
show(() => bigOrNothing(1));                            // the missing return path gives undefined

section("never: a function that does not return");
syntax(
  ["function fail(msg: string): never { throw ... }", "never is assignable to every type"],
  ["throw ... | process.exit(1)", "also never: they end the flow, so code after them is unreachable"],
);
show(() => halve(8));
show(() => thrown(() => halve(7)));                     // the else branch is fail(), so it throws
show(() => parseOrFail("12"));
show(() => thrown(() => parseOrFail("x")));
note("Code after a call to a never function is unreachable: an editor hint by default, an error (TS7027) with allowUnreachableCode: false");
