/**
 * 8a. IF, ELSE IF AND THE TERNARY OPERATOR (if is a statement; ?: is the expression)
 *     Run: npx tsx src/8-control-flow/if-and-ternary.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

/** A chain of else if: the first true condition decides. */
function sizeWord(n: number): string {
  if (n < 0) return "negative";
  else if (n === 0) return "zero";
  else if (n < 10) return "small";
  else return "large";
}

const signWord = (n: number): string => (n < 0 ? "negative" : n === 0 ? "zero" : "positive");   // a chained ternary: fine when it stays this flat

/** A boolean | undefined that is true, false or missing. */
const maybeFlag = (kind: number): boolean | undefined => (kind === 0 ? true : kind === 1 ? false : undefined);

/** TRAP: = assigns, == and === compare. The assignment is the condition, and 5 is truthy. tsc does not complain (a linter would). */
function isFiveWrong(x: number): string {
  if (x = 5) return "yes";
  return "no";
}

/** TRAP: without braces only the NEXT statement belongs to the if; the indentation lies. */
function braceless(flag: boolean): string[] {
  const out: string[] = [];
  if (flag)
    out.push("inside");
    out.push("outside");
  return out;
}

/** switch (true): every case is a Boolean condition, the first true one runs. Order matters. */
function temperatureWord(t: number): string {
  switch (true) {
    case t < 0: return "freezing";
    case t < 15: return "cold";
    case t < 25: return "mild";
    default: return "hot";
  }
}

/** TRAP: the second branch can never run, because n > 0 already matched every n > 100. */
function wrongOrder(n: number): string {
  if (n > 0) return "positive";
  else if (n > 100) return "huge";
  return "not positive";
}

title("IF AND TERNARY  -  choose between paths");

section("if gives a statement, ?: gives a value");
syntax(
  ["if (cond) { ... } else if (cond2) { ... } else { ... }", "a STATEMENT: it runs code, it has no value"],
  ["cond ? a : b", "an EXPRESSION: it has a value, so it can sit inside a const, an argument or a template"],
  ["a ? b : c ? d : e", "chained: reads left to right, the first true condition wins"],
);
const n = 7;
show(() => (n > 5 ? "big" : "small"));
const parity = n % 2 === 0 ? "even" : "odd";  show(() => parity);
show(() => `${n} is ${n > 5 ? "big" : "small"}`);       // an expression, so it fits inside a template literal
show(() => (n > 5 ? 1 : "one"));                        // the branches may have different types: number | string
wontCompile("const s = if (n > 5) 1 else 2;", "TS1109: Expression expected (an if cannot give a value: use ?:)");
show(() => sizeWord(-4));
show(() => sizeWord(0));
show(() => sizeWord(9));
show(() => sizeWord(10));                               // boundary: n < 10 is false, so the last branch
show(() => signWord(-3));
show(() => signWord(0));
note("Nested ternaries beyond two levels read badly: use if / else if, a lookup table or a function with early returns");

section("Conditions: ranges, &&, || and precedence");
syntax(
  ["a <= x && x <= b", "a range test: JavaScript has no chained comparison and no `in a..b`"],
  ["a || b && c", "&& binds tighter than ||: it means a || (b && c)"],
);
show(() => n >= 1 && n <= 10);
show(() => n >= 1 && n < 7);                            // false: 7 is not below 7
wontCompile("const inRange = 0 < n < 10;", "TS2365: Operator '<' cannot be applied to types 'boolean' and 'number'");
show(() => ((0 < 50) as unknown as number) < 10);       // true for EVERY n: (0 < 50) is true, true becomes 1, and 1 < 10 (a cast hides tsc's error)
show(() => true || false && false);                     // TRAP: true, read as true || (false && false)
show(() => (true || false) && false);                   // false: parentheses give the other meaning
show(() => maybeFlag(0) === true);
show(() => maybeFlag(2) === true);                      // undefined === true is false: compare a boolean | undefined like this
show(() => maybeFlag(2) ?? false);                      // or give it a default with ??

section("&& and || as control flow (the right side runs only if needed)");
const calls: string[] = [];
const log = (message: string): boolean => { calls.push(message); return true; };
const on = Boolean(1);
const off = Boolean(0);
off && log("a");                                        // the left side is false: log never runs
on && log("b");                                         // the left side is true: log runs
on || log("c");                                         // the left side is already true: log never runs
off || log("d");                                        // the left side is false: log runs
show(() => calls);                                      // only b and d ran
note("a && b() as a statement is legal but unclear; write if (a) b(). Keep && and || for values: user && user.name, count || 10 (careful: 0, see 7b)");

section("Traps, and switch (true)");
syntax(["switch (true) { case t < 0: ...; case t < 15: ...; default: ... }", "every case is a Boolean condition: the first true one runs (next file: switch in general)"]);
show(() => isFiveWrong(3));                             // TRAP: "yes": (x = 5) assigns 5 and the condition is the value 5 (truthy)
show(() => braceless(false));                           // TRAP: [ 'outside' ]: the second push is not part of the if
show(() => braceless(true));
show(() => wrongOrder(500));                            // TRAP: positive, the huge branch is unreachable
show(() => temperatureWord(-5));
show(() => temperatureWord(15));                        // boundary: t < 15 is false, so mild
show(() => temperatureWord(25));
show(() => temperatureWord(NaN));                       // "hot": every comparison with NaN is false, so default (lesson 0)
note("Always use braces, even for one statement; a switch (true) lists conditions top to bottom just like else if");
