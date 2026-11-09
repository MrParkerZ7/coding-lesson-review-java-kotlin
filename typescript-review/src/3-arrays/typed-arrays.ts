/**
 * 3f. TYPED ARRAYS: fixed-size arrays of ONE numeric type, backed by raw bytes
 *     Run: npx tsx src/3-arrays/typed-arrays.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title, wontCompile } from "../output";

/** Sieve of Eratosthenes: all primes up to n. A Uint8Array of flags uses 1 byte per number. O(n log log n). */
function primesUpTo(n: number): number[] {
  const composite = new Uint8Array(n + 1);              // zero-filled: 0 = "still might be prime"
  const primes: number[] = [];
  for (let i = 2; i <= n; i++) {
    if (composite[i] === 1) continue;
    primes.push(i);
    for (let j = i * i; j <= n; j += i) composite[j] = 1;
  }
  return primes;
}

title("TYPED ARRAYS  -  Int32Array, Uint8Array, Float64Array ...");

section("What they are");
syntax(
  ["new Int32Array(n) | Int32Array.of(1, 2) | Int32Array.from(xs)", "n zeros | from values | from any iterable"],
  ["Int8 Uint8 Uint8Clamped Int16 Uint16 Int32 Uint32 Float32 Float64", "the element types (each + Array); BigInt64Array and BigUint64Array hold bigint"],
  ["ta.buffer | ta.byteLength | ta.BYTES_PER_ELEMENT", "the raw ArrayBuffer | its size in bytes | 4 for Int32Array"],
);
const ints = Int32Array.of(5, 1, 4);  show(() => ints);
show(() => new Int32Array(3));                          // zero-filled: no holes, no undefined
show(() => ints.byteLength);                            // 3 elements * 4 bytes
show(() => Int32Array.BYTES_PER_ELEMENT);
show(() => Array.isArray(ints));                        // TRAP: false, a typed array is NOT an Array
show(() => ints instanceof Int32Array);
show(() => ArrayBuffer.isView(ints));                   // true for every typed array (and DataView)
show(() => [...ints]);                                  // spread (or Array.from) gives a normal array
wontCompile("ints.push(4)", "TS2339: property 'push' does not exist on type 'Int32Array<ArrayBuffer>'");
wontCompile("const a: number[] = ints", "TS2740: 'Int32Array<ArrayBuffer>' is missing pop, push, concat, shift ... of number[]");
note("The length is fixed. There is no push, pop, splice, no holes, and a write past the end is ignored");
const small = new Int32Array(2);
small[10] = 5;  show(() => small);                      // ignored: no exception, no growth
show(() => small[10]);                                  // undefined

section("Wrapping and clamping: values are converted on every write");
const i32 = new Int32Array(1);
i32[0] = 2 ** 31;  show(() => i32[0]);                  // TRAP: wraps to -2147483648 (the same as 2 ** 31 | 0)
const u8 = new Uint8Array(1);
u8[0] = 256;  show(() => u8[0]);                        // 256 wraps to 0
u8[0] = -1;  show(() => u8[0]);                         // -1 wraps to 255
u8[0] = 3.9;  show(() => u8[0]);                        // the fraction is cut: 3
const clamped = new Uint8ClampedArray(1);
clamped[0] = 300;  show(() => clamped[0]);              // Uint8Clamped: saturates at 255 (pixels, canvas)
clamped[0] = -5;  show(() => clamped[0]);               // ... and at 0
clamped[0] = 1.5;  show(() => clamped[0]);              // rounds (half to even): 2
const f32 = new Float32Array(1);
f32[0] = 0.1;  show(() => f32[0]);                      // Float32 keeps about 7 digits
const f64 = new Float64Array(1);
f64[0] = 0.1;  show(() => f64[0]);                      // Float64 is the same double as a normal number
const big = new BigInt64Array(1);
big[0] = 2n ** 63n;  show(() => big[0]);                // 64-bit signed bigint wraps too
note("There is no error on overflow: the value is silently converted (like a C cast)");

section("Views on one buffer");
const buffer = new ArrayBuffer(8);
const bytes = new Uint8Array(buffer);
const words = new Int32Array(buffer);                   // the same 8 bytes seen as two 32-bit integers
words[0] = 0x01020304;
show(() => bytes);                                      // little-endian CPU (x86, ARM): the lowest byte comes first
new DataView(buffer).setInt32(4, 0x01020304, false);    // DataView lets you choose the byte order (false = big-endian)
show(() => bytes);                                      // bytes 4..7 are now 1, 2, 3, 4
const u = Uint8Array.of(1, 2, 3, 4, 5);
const view = u.subarray(1, 3);                          // a VIEW: shares memory with u
view[0] = 99;
show(() => u);                                          // u changed: the view writes into the same memory
const copy = u.slice(1, 3);                             // slice COPIES, as on a plain Array
copy[0] = 7;
show(() => u);                                          // u did not change
note("subarray = a view (shared memory), slice = a copy. A plain Array has only slice, which copies");

section("Methods: like Array, with surprises");
show(() => Int32Array.of(10, 9, 1).sort());             // TRAP the other way: typed arrays sort NUMERICALLY by default
show(() => [10, 9, 1].sort());                          // while a plain Array sorts as text: [1, 10, 9]
show(() => Int32Array.of(1, 2, 3).map((x) => x * 1e10));   // map returns the SAME type, so every result wraps again
show(() => Int32Array.of(1, 2, 3).map((x) => x * 2));
show(() => Int32Array.of(1, 2, 3).includes(2));
show(() => JSON.stringify(Int32Array.of(1, 2)));        // TRAP: an object with numeric keys, not [1,2] (lesson 18)
show(() => JSON.stringify(Array.from(Int32Array.of(1, 2))));   // convert to an Array first
const counts = new Int32Array(26);
for (const ch of "banana") counts[ch.charCodeAt(0) - 97]++;   // frequency table of a..z (lesson 6)
show(() => [counts[0], counts[1], counts[13]]);         // a: 3, b: 1, n: 2

section("A real use: the sieve of Eratosthenes");
show(() => primesUpTo(30));
show(() => primesUpTo(1));                              // no primes below 2
show(() => primesUpTo(100).length);                     // 25 primes up to 100

section("Cost");
bigO(
  ["ta[i] read | write", "O(1): the numbers sit side by side in memory"],
  ["new Int32Array(n)", "O(n) time, 4n bytes (the element type fixes the size)"],
  ["subarray", "O(1): a view, nothing is copied"],
  ["slice | Array.from | spread", "O(n): a copy"],
  ["sort", "O(n log n); numeric by default, no comparator needed"],
);
