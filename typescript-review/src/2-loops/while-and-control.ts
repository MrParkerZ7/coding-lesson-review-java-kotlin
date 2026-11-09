/**
 * 2b. WHILE, DO-WHILE, for(;;), BREAK, CONTINUE, LABELS, and some/every as a break substitute
 *     Run: npx tsx src/2-loops/while-and-control.ts        (add --plain for ASCII output)
 *     Each demo collects what the loop visits with collect((add) => { loop calling add(x) }), so the result is printed.
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** Runs a loop body that calls add(x) for everything it visits, and returns what was added. */
function collect(body: (add: (x: unknown) => void) => void): unknown[] {
  const out: unknown[] = [];
  body((x) => out.push(x));
  return out;
}

title("WHILE LOOPS AND LOOP CONTROL");

section("while, do-while and for (;;)");
syntax(
  ["while (condition) { }", "checks BEFORE each round: may run zero times"],
  ["do { } while (condition);", "runs the body first: at least once"],
  ["while (true) { ... break; } | for (;;) { ... break; }", "loop until something inside says stop"],
);
show(() => collect((add) => { let n = 3; while (n > 0) add(n--); }));                   // n-- uses the value, THEN decrements
show(() => collect((add) => { let k = 10; while (k < 5) add(k); }));                    // zero rounds: the condition is false at once
show(() => collect((add) => { let k = 10; do { add(k); } while (k < 5); }));            // one round: the body ran before the check
show(() => collect((add) => { let i = 0; while (true) { if (i === 3) break; add(i++); } }));
show(() => collect((add) => { let i = 0; for (;;) { if (i === 3) break; add(i++); } }));  // for (;;) is the same infinite loop
show(() => collect((add) => { const queue = [1, 2, 3]; while (queue.length) add(queue.shift()); }));   // the shape of BFS (lesson 5)
note("The condition is any value, not just a boolean: while (queue.length) stops at 0, the falsy number. while (1) compiles too");
note("A counter that jumps over the stop value never ends: while (n !== 0) n -= 2 from 5 is an endless loop; test with > or <");

section("break, continue and labels");
syntax(
  ["break | continue", "leave the loop | skip to the next round"],
  ["label: for (...) { for (...) { continue label; } }", "continue (or break) the OUTER loop"],
  ["label: { ... break label; ... }", "a label on a plain block leaves just that block"],
);
show(() => collect((add) => { for (let i = 1; i <= 6; i++) { if (i % 2 === 1) continue; add(i); } }));   // skip the odd numbers
show(() => collect((add) => { for (let i = 1; i <= 9; i++) { if (i === 4) break; add(i); } }));         // stop at 4
const primes: number[] = [];
next: for (let n = 2; n <= 20; n++) {
  for (let d = 2; d * d <= n; d++) {
    if (n % d === 0) continue next;                      // n has a divisor: go to the next n, skip the push
  }
  primes.push(n);
}
show(() => primes);                                      // the classic labeled continue: primes up to 20
const grid = [[1, 2], [3, 4]];
let result = "not found";
search: {
  for (const row of grid) {
    for (const cell of row) {
      if (cell === 3) {
        result = "found";
        break search;                                    // leaves the block, so both loops end at once
      }
    }
  }
}
show(() => result);
wontCompile("for (let i = 0; i < 3; i++) { } console.log(i);", "TS2304: Cannot find name 'i' (a let counter ends with the loop)");
const seen: string[] = [];
for (let i = 1; i <= 3; i++) {
  switch (i) {
    case 2:
      break;                                             // leaves only the switch, not the for
    default:
      seen.push(`d${i}`);
  }
  seen.push(`end${i}`);                                  // so this still runs for i = 2
}
show(() => seen);                                        // TRAP: [ 'd1', 'end1', 'end2', 'd3', 'end3' ]
note("break inside a switch inside a loop does not stop the loop: use a label (break outer) or return");

section("some and every as a break substitute");
syntax(
  ["xs.some((x) => test)", "stops at the FIRST true: returns true, or false for an empty array"],
  ["xs.every((x) => test)", "stops at the FIRST false: returns false, or true for an empty array"],
  ["xs.find((x) => test) | xs.findIndex((x) => test)", "stops at the first match: the element | its index (-1 when none)"],
);
show(() => collect((add) => { [1, 2, 3, 4].some((x) => { add(x); return x === 3; }); }));    // visits 1, 2, 3 and stops
show(() => collect((add) => { [1, 2, 3, 4].every((x) => { add(x); return x < 3; }); }));    // returning false is the "break"
show(() => [].every(() => false));                       // TRAP: true, nothing contradicts an empty array
show(() => [].some(() => true));                         // false
show(() => [5, 8, 12].find((x) => x > 6));               // 8: stops at the first match
show(() => [5, 8, 12].findIndex((x) => x > 100));        // -1: not found
note("forEach cannot break (previous file); when you need break, continue or await in order, a plain for...of is the clearest");

section("Cost");
let halvings = 0;
for (let n = 1024; n > 1; n = Math.floor(n / 2)) halvings++;
show(() => halvings);                                    // 10: halving 1024 takes 10 rounds, log2(1024)
bigO(
  ["while (n > 0) { ...; n-- }", "O(n) rounds"],
  ["while (n > 1) { ...; n = Math.floor(n / 2) }", "O(log n) rounds"],
  ["break / some / find on the first match", "best case O(1), worst case O(n)"],
);
