/**
 * 4c. GROUPING AND COUNTING: groupBy, frequency maps, partition, zip, chunk, windowed, unique
 *     Run: npx tsx src/4-array-methods/grouping-and-counting.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** Split in two with ONE pass: [matching, not matching]. O(n). */
function partition<T>(xs: readonly T[], test: (x: T) => boolean): [T[], T[]] {
  const yes: T[] = [];
  const no: T[] = [];
  for (const x of xs) (test(x) ? yes : no).push(x);
  return [yes, no];
}

/** Pair up two arrays; stops at the SHORTER one, like Kotlin's zip. O(n). */
function zip<A, B>(xs: readonly A[], ys: readonly B[]): [A, B][] {
  const n = Math.min(xs.length, ys.length);
  return Array.from({ length: n }, (_, i): [A, B] => [xs[i]!, ys[i]!]);
}

/** Blocks of `size` that do not overlap; the last may be shorter. O(n). */
function chunk<T>(xs: readonly T[], size: number): T[][] {
  if (!(size >= 1)) throw new RangeError("size must be at least 1");   // size 0 would make Math.ceil(n / 0) = Infinity
  return Array.from({ length: Math.ceil(xs.length / size) }, (_, i) => xs.slice(i * size, i * size + size));
}

/** Sliding windows of `size`: n - size + 1 of them, none when the array is shorter. O(n * size). */
function windowed<T>(xs: readonly T[], size: number): T[][] {
  return Array.from({ length: Math.max(0, xs.length - size + 1) }, (_, i) => xs.slice(i, i + size));
}

/** Keep the FIRST element of each key, order kept. O(n). */
function uniqueBy<T, K>(xs: readonly T[], key: (x: T) => K): T[] {
  const seen = new Set<K>();
  return xs.filter((x) => {
    const k = key(x);
    if (seen.has(k)) return false;
    seen.add(k);
    return true;
  });
}

title("GROUPING AND COUNTING  -  turn an array into a Map, split it, slice it");

const words = ["bat", "ant", "ape", "bee", "cow", "bee"];
const nums = [1, 2, 3, 4, 5, 6];

section("groupBy: one key, many elements (ES2024)");
syntax(
  ["Object.groupBy(xs, (x, i) => key)", "a null-prototype OBJECT: keys become strings (integer-like keys sort first)"],
  ["Map.groupBy(xs, (x, i) => key)", "a MAP: keys keep their type, first-seen order"],
);
show(() => Object.groupBy(words, (w) => w[0]));         // keys in first-seen order: b, a, c
show(() => Map.groupBy(words, (w) => w[0]));            // the same as a Map
show(() => Object.groupBy(nums, (x) => (x % 2 === 0 ? "even" : "odd")));
show(() => Object.groupBy(nums, (x) => x % 3));         // TRAP: keys '0' '1' '2' are strings, ordered ascending, not first-seen
show(() => Map.groupBy(nums, (x) => x % 3));            // number keys kept, first-seen order: 1, 2, 0
const groups = Object.groupBy(words, (w) => w[0]);
show(() => Object.getPrototypeOf(groups));              // null: no toString, no hasOwnProperty on the result
show(() => groups.b?.length);                           // TS types every group as possibly missing: use ?.
show(() => groups.z?.length);                           // undefined: no such group
wontCompile("groups.b.length", "TS18048: 'groups.b' is possibly 'undefined' (the result is a Partial<Record<...>>)");
show(() => [...Map.groupBy(words, (w) => w[0])].map(([k, v]) => k + v.length));   // key and group size: b3 a2 c1

section("Counting: a frequency map");
syntax(
  ["const counts = new Map<K, number>();", "for (const x of xs) counts.set(x, (counts.get(x) ?? 0) + 1);"],
  ["[...counts].sort(([, a], [, b]) => b - a)", "entries by count, biggest first (a stable sort keeps ties in first-seen order)"],
);
const counts = new Map<string, number>();
for (const w of words) counts.set(w, (counts.get(w) ?? 0) + 1);
show(() => counts);                                     // 'bee' => 2, the rest => 1
const byCount = [...counts].sort(([, a], [, b]) => b - a);
show(() => byCount[0]);                                 // the most frequent word: ['bee', 2]
show(() => Object.fromEntries(counts));                 // Map to a plain object
show(() => new Map(Object.entries({ a: 1, b: 2 })));    // and back
show(() => Object.entries({ a: 3, b: 1 }).sort(([, x], [, y]) => y - x).map(([k]) => k));   // keys by value, descending
show(() => Map.groupBy(words, (w) => w).size);          // distinct words: 5
show(() => new Set(words).size);                        // the same, cheaper: nothing is grouped

section("Split in two and pair up");
show(() => partition(nums, (x) => x % 2 === 0));        // [[2, 4, 6], [1, 3, 5]] in one pass
show(() => zip([1, 2, 3], ["a", "b", "c"]));
show(() => zip([1, 2, 3], ["a", "b"]));                 // TRAP: the unmatched 3 is silently dropped
show(() => Object.fromEntries(zip(["a", "b"], [1, 2])));   // two arrays to an object
const unzipped = [[1, "a"], [2, "b"]] as const;
show(() => [unzipped.map(([n]) => n), unzipped.map(([, s]) => s)]);   // unzip: two maps, one per side

section("Chunk, windowed, unique");
show(() => chunk(nums, 4));                             // the last block may be shorter
show(() => chunk([], 3));                               // empty in, empty out
show(() => thrown(() => chunk(nums, 0)));               // guarded: without the check, Math.ceil(6 / 0) is Infinity
show(() => windowed(nums, 3));                          // 4 windows: n - 3 + 1
show(() => windowed(nums, 3).map((w) => w[0] + w[1] + w[2]));   // sums of 3 in a row (lesson 2: O(n) by updating a total)
show(() => windowed(nums, 9));                          // TRAP: a window longer than the array gives NO windows
show(() => [...new Set(words)]);                        // unique values, first-seen order
show(() => uniqueBy(words, (w) => w[0]));               // the FIRST word of each first letter
show(() => nums.slice(1, -1));                          // all but the first and last

section("Cost");
bigO(
  ["groupBy | counting | partition | zip | unique", "O(n) time, O(n) space: a hash map or one pass"],
  ["chunk | slice-based windows", "O(n): each element copied once"],
  ["windowed(k)", "O(n * k): k elements copied per window"],
  ["sorting the counts", "O(d log d) for d distinct keys"],
  ["group, then .size", "builds every list: count with a Map of numbers when only sizes matter"],
);
note("Object.groupBy and Map.groupBy need Node 21+ (this repo runs Node 24); older code used reduce (reduce.ts)");
