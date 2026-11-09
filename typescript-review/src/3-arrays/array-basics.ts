/**
 * 3a. ARRAY BASICS: create, read, holes, length, types
 *     Run: npx tsx src/3-arrays/array-basics.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

title("ARRAYS  -  create and read");

section("Creating");
syntax(
  ["const xs: number[] = [1, 2, 3];", "array literal; number[] and Array<number> are the same type"],
  ["Array.from({ length: n }, (_, i) => f(i))", "built by a function of the index"],
  ["new Array<T>(n).fill(v)", "n slots, all set to v (a primitive or ONE shared object)"],
  ["Array.of(x) | [...iterable]", "an array holding x | a copy from any iterable"],
);
const xs = [10, 20, 30, 40, 50];  show(() => xs);
show(() => Array.from({ length: 5 }, (_, i) => i * i));   // built from the index: 0, 1, 4, 9, 16
const zeros = new Array<number>(3).fill(0);  show(() => zeros);
show(() => Array.of(3));                                // an array holding the number 3
show(() => new Array<number>(3, 4));                    // TRAP: two arguments make [3, 4] ...
show(() => new Array<number>(3));                       // ... but ONE number makes 3 empty slots, not [3]
show(() => [..."abc"]);                                 // spread any iterable (a string gives characters)

section("Holes: empty slots are not undefined");
const holes = new Array<number>(3);  show(() => holes); // 3 empty slots, no values at all
show(() => holes.length);
show(() => holes.map(() => 1));                         // TRAP: map and forEach SKIP holes, so the result is still empty
show(() => Array.from(holes).map(() => 1));             // Array.from turns holes into undefined, then map runs
const sparse = [1, 2];
sparse[5] = 9;  show(() => sparse);                     // writing past the end leaves a gap: length is now 6
show(() => 3 in sparse);                                // false: index 3 does not exist as a key
note("A hole is a missing index: sparse[3] reads as undefined, but `3 in sparse` is false (no such key)");

section("Trap: fill with an object shares ONE object");
const rows = new Array<number[]>(3).fill([]);
rows[0].push(1);  show(() => rows);                     // TRAP: all three slots hold the SAME inner array
show(() => rows[0] === rows[2]);                        // true: one object, three references
const separate = Array.from({ length: 3 }, () => [] as number[]);
separate[0].push(1);  show(() => separate);             // the fix: a function builds a NEW inner array per slot
note("fill(x) evaluates x once. A number or string is copied, an array or object is shared (see two-dimensional.ts)");

section("Reading: no exception for a bad index");
syntax(
  ["xs[i]", "element at index i (from 0); out of range gives undefined"],
  ["xs.at(i)", "like xs[i] but a negative i counts from the end"],
  ["xs.length", "number of slots; also the index after the last element"],
);
show(() => xs[0]);
show(() => xs[xs.length - 1]);                          // the classic way to get the last element
show(() => xs.at(-1));                                  // the modern way
show(() => xs.at(-2));
show(() => xs[99]);                                     // TRAP: undefined, NO exception (Java would throw)
show(() => xs[-1]);                                     // TRAP: undefined, "-1" is just a missing property name
show(() => xs.at(99));
show(() => [].at(-1));                                  // undefined on an empty array
note("tsc types xs[99] as number, a lie: see the last section for noUncheckedIndexedAccess");
const [first, second] = xs;  show(() => first + second);   // destructuring takes the first slots
const [head, ...tail] = xs;  show(() => tail);          // rest collects the others (head is 10)
show(() => head);

section("length is writable");
const w = [1, 2, 3, 4, 5];
w.length = 2;  show(() => w);                           // truncates in place: the 3 elements are gone
w.length = 4;  show(() => w);                           // growing makes holes, not zeros
show(() => typeof xs);                                  // an array is an object ...
show(() => Array.isArray(xs));                          // ... and Array.isArray is the reliable test

section("The array types");
syntax(
  ["number[]  |  Array<number>", "the same type; use [] for simple elements, Array<> for long ones"],
  ["readonly number[]  |  ReadonlyArray<number>", "no push, pop, sort, index assignment: compile-time only"],
  ["[string, number]  |  [name: string, age: number]", "tuple: fixed length, a type per position"],
  ["(string | number)[]", "mixed elements; the parentheses matter: string | number[] is different"],
);
const ro: readonly number[] = [1, 2, 3];  show(() => ro.length);
wontCompile("ro.push(4)", "TS2339: property 'push' does not exist on type 'readonly number[]'");
wontCompile("ro[0] = 9", "TS2542: index signature in type 'readonly number[]' only permits reading");
const pair: [string, number] = ["ann", 30];  show(() => pair);   // at run time a tuple is an ordinary array
wontCompile("pair[2]", "TS2493: tuple type '[string, number]' of length '2' has no element at index '2'");
wontCompile("const t: [string, number] = ['a']", "TS2322: source has 1 element(s) but target requires 2");
(ro as number[]).push(4);  show(() => ro);              // readonly is erased: a cast and the push works at run time
note("readonly is a compile-time promise only; it does not freeze the array (Object.freeze does, shallowly)");

section("noUncheckedIndexedAccess: make xs[i] honest");
wontCompile("const n: number = xs[0]", "only with --noUncheckedIndexedAccess: xs[0] is number | undefined (TS2322)");
note("Not part of strict. When it is on, check first (if (x !== undefined)), use ?? , or the ! assertion");
