/**
 * 10d. HIGHER-ORDER FUNCTIONS: functions as values, callbacks, compose, wrappers
 *      Run: npx tsx src/10-functions-and-closures/higher-order-functions.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

title("HIGHER-ORDER FUNCTIONS  -  take a function, return a function");

section("Functions are values");
syntax(
  ["type Op = (a: number, b: number) => number", "a function TYPE: (parameters) => result"],
  ["const ops: Record<string, Op> = { '+': add }", "store functions in objects and arrays, pass them, return them"],
  ["add  vs  add(1, 2)", "add hands the function over; add(1, 2) runs it and hands over the result"],
);
type BinaryOp = (a: number, b: number) => number;
const add: BinaryOp = (a, b) => a + b;                  // a and b are typed by the context
const ops: Record<string, BinaryOp> = { "+": add, "*": (a, b) => a * b };
show(() => typeof add);                                 // a function is a value of its own typeof
show(() => add instanceof Object);                      // ... and an object: it can carry properties
show(() => ops["*"](6, 7));                             // the strategy pattern: behaviour picked by data
show(() => Object.keys(ops));
function applyTwice<T>(fn: (x: T) => T, x: T): T { return fn(fn(x)); }
show(() => applyTwice((n: number) => n * 3, 2));        // fn(fn(2)) = 18
function keepIf<T>(items: T[], test: (item: T) => boolean): T[] { return items.filter((item) => test(item)); }
show(() => keepIf([1, 2, 3, 4], (n) => n % 2 === 0));   // n is inferred as number from the array
wontCompile("keepIf([1, 2], (s: string) => true)", "TS2322: the array makes T a number, a (s: string) callback does not fit");
wontCompile("cb: (a: number) => void = (a: number, b: number) => {}", "TS2322: a callback may take FEWER parameters than offered, never more");
note("Q: What is a higher-order function?  A: one that takes a function, returns one, or both (map, filter, reduce, compose)");

section("Returning functions: factory, compose, pipe");
syntax(
  ["compose(f, g)(x)", "f(g(x)): the LAST function runs first (math order)"],
  ["pipe(f, g)(x)", "g(f(x)): the FIRST function runs first (reading order)"],
);
const multiplier = (factor: number) => (n: number): number => n * factor;
const triple = multiplier(3);                           // the returned arrow remembers factor (closures)
show(() => triple(5));
const compose = <T>(...fns: Array<(x: T) => T>) => (x: T): T => fns.reduceRight((acc, fn) => fn(acc), x);
const pipe = <T>(...fns: Array<(x: T) => T>) => (x: T): T => fns.reduce((acc, fn) => fn(acc), x);
const inc = (n: number): number => n + 1;
const double = (n: number): number => n * 2;
show(() => compose(double, inc)(5));                    // double(inc(5)): inc first, 12
show(() => pipe(double, inc)(5));                       // inc(double(5)): double first, 11
const pipe2 = <A, B, C>(f: (a: A) => B, g: (b: B) => C) => (a: A): C => g(f(a));
const lengthOfTrimmed = pipe2((s: string) => s.trim(), (s) => s.length);   // the types may change between steps
show(() => lengthOfTrimmed("  hello  "));

section("Trap: map passes (value, index, array), not just the value");
syntax(["array.map(fn)", "calls fn(value, index, array): a function with optional extra parameters sees them"]);
show(() => ["1", "2", "3"].map(parseInt));              // TRAP: parseInt(value, index) uses the index as the radix: [1, NaN, NaN]
show(() => ["10", "10", "10"].map(parseInt));           // radix 0 (= 10), 1 (invalid), 2 (binary): [10, NaN, 2]
show(() => ["1", "2", "3"].map(Number));                // Number takes one argument and ignores the rest
show(() => ["1", "2", "3"].map((s) => parseInt(s, 10))); // the safe habit: wrap it and say the radix
note("Pass a function by name only when you know ALL its parameters; otherwise write the arrow");

section("fn.length and fn.name");
syntax(
  ["fn.length", "the number of parameters before the first default or rest parameter"],
  ["fn.name", "the declared name; an arrow takes the name of the variable it is assigned to"],
);
function three(a: number, b: number, c: number): number { return a + b + c; }
const withDefault = (a: number, b = 2, c = 3): number => a + b + c;
const withRest = (a: number, ...others: number[]): number => a + others.length;
show(() => three.length);
show(() => withDefault.length);                         // stops at the first default: only a counts
show(() => withRest.length);                            // a rest parameter is not counted
show(() => three.name);
show(() => withDefault.name);                           // taken from the variable
show(() => [() => 0][0].name);                          // an arrow created inside an array literal has no name: ""
note("Frameworks read fn.length: Express treats a 4-parameter function as an error handler");

section("Wrappers with generics: add behaviour, keep the signature");
syntax(["wrap<A extends unknown[], R>(fn: (...args: A) => R)", "A = the parameter list, R = the result: the returned wrapper has the SAME type as fn"]);
function withLog<A extends unknown[], R>(fn: (...args: A) => R, log: string[]): (...args: A) => R {
  return (...args: A): R => {
    const result = fn(...args);
    log.push(`${fn.name}(${args.join(", ")}) -> ${String(result)}`);
    return result;
  };
}
const log: string[] = [];
const loggedAdd = withLog(add, log);
show(() => loggedAdd(2, 3));                            // behaves exactly like add
show(() => log);                                        // ... and left a trace
wontCompile("loggedAdd('x', 2)", "TS2345: the wrapper keeps add's parameter types");
const negate = <A extends unknown[]>(test: (...args: A) => boolean) => (...args: A): boolean => !test(...args);
const isEven = (n: number): boolean => n % 2 === 0;
show(() => [1, 2, 3, 4].filter(negate(isEven)));        // a predicate turned inside out
note("The same shape is behind memoize, once, debounce, retry and middleware (closures, async-patterns)");
