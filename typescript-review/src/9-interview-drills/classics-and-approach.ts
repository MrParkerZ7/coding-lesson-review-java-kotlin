/**
 * 9e. CLASSICS AND THE INTERVIEW ROUTINE: digits, gcd, primes, Fibonacci, and how to approach a problem
 *     (FizzBuzz, leap year and grades are lesson 8d)
 *     Run: npx tsx src/9-interview-drills/classics-and-approach.ts        (add --plain for ASCII output)
 */
import { bigO, note, section, show, syntax, title } from "../output";

/**
 * PALINDROME NUMBER. Problem: does a whole number read the same backwards? No String allowed.
 * Idea: reverse only the second half of the digits and compare it with the first half.
 * Negatives are never palindromes; a positive number ending in 0 cannot be one either; a decimal is not a whole number. Time O(log10 x), space O(1).
 */
function isPalindromeNumber(x: number): boolean {
  if (!Number.isInteger(x) || x < 0 || (x % 10 === 0 && x !== 0)) return false;
  let rest = x;
  let reversedHalf = 0;
  while (rest > reversedHalf) {
    reversedHalf = reversedHalf * 10 + (rest % 10);
    rest = Math.floor(rest / 10);                        // "/" gives a fraction: floor it to drop the last digit
  }
  return rest === reversedHalf || rest === Math.floor(reversedHalf / 10);   // the second test drops the middle digit of an odd length
}

/**
 * REVERSE INTEGER. Problem: reverse the digits of a 32-bit integer; return 0 when the result does not fit in 32 bits.
 * Idea: pop the last digit with % 10 and push it with * 10. In JavaScript the arithmetic cannot overflow (a double holds 10 digits exactly),
 * so the range is checked AFTER the loop; in Java or Kotlin it must be checked before the multiplication. Time O(log10 x), space O(1).
 */
function reverseInteger(x: number): number {
  const sign = x < 0 ? -1 : 1;
  let rest = Math.abs(x);
  let reversed = 0;
  while (rest > 0) {
    reversed = reversed * 10 + (rest % 10);
    rest = Math.floor(rest / 10);
  }
  const result = sign * reversed;
  return result < -(2 ** 31) || result > 2 ** 31 - 1 ? 0 : result;
}

/** Digits of a number as numbers. TRAP version: map(parseInt) passes the INDEX as the radix. */
const digitsWrong = (n: number): number[] => String(n).split("").map(parseInt);
const digits = (n: number): number[] => String(n).split("").map(Number);
const digitSum = (n: number): number => digits(n).reduce((sum, d) => sum + d, 0);

/**
 * GREATEST COMMON DIVISOR. Problem: the largest number that divides both a and b (a, b >= 0).
 * Idea: Euclid. gcd(a, b) = gcd(b, a % b), and gcd(a, 0) = a. Time O(log min(a, b)), space O(1) for a loop (the recursion uses O(log n) stack).
 */
function gcd(a: number, b: number): number {
  while (b !== 0) [a, b] = [b, a % b];
  return a;
}

/**
 * IS PRIME. Problem: is n a prime (greater than 1, divisible only by 1 and itself)?
 * Idea: trial division up to the square root, odd divisors only. i * i stays exact for every safe integer divisor.
 * Time O(sqrt n), space O(1).
 */
function isPrime(n: number): boolean {
  if (!Number.isInteger(n) || n < 2) return false;
  if (n % 2 === 0) return n === 2;
  for (let i = 3; i * i <= n; i += 2) if (n % i === 0) return false;
  return true;
}

/** FIBONACCI, iterative: two variables. O(n) time, O(1) space. A number is exact only up to fib(78). */
function fib(n: number): number {
  let a = 0;
  let b = 1;
  for (let i = 0; i < n; i++) [a, b] = [b, a + b];
  return a;
}

/** FIBONACCI with bigint: exact for any n. O(n) time (the numbers grow, so each addition costs more). */
function fibBig(n: number): bigint {
  let a = 0n;
  let b = 1n;
  for (let i = 0; i < n; i++) [a, b] = [b, a + b];
  return a;
}

/** FIBONACCI, memoized recursion: each n is computed once and cached in a Map. O(n) time, O(n) space (and O(n) call stack). */
function fibMemo(n: number, memo: Map<number, number> = new Map()): number {
  if (n <= 1) return n;
  const cached = memo.get(n);
  if (cached !== undefined) return cached;
  const value = fibMemo(n - 1, memo) + fibMemo(n - 2, memo);
  memo.set(n, value);
  return value;
}

/** How many calls the plain recursion fib(n) = fib(n - 1) + fib(n - 2) makes: the cost of NOT caching. O(2^n). */
const fibNaiveCalls = (n: number): number => (n <= 1 ? 1 : 1 + fibNaiveCalls(n - 1) + fibNaiveCalls(n - 2));

title("CLASSICS  -  the small problems every interview starts with");

section("Palindrome number (no String)");
show(() => isPalindromeNumber(121));
show(() => isPalindromeNumber(-121));                   // the minus sign makes it not a palindrome
show(() => isPalindromeNumber(10));                     // TRAP: reversed it would be 01
show(() => isPalindromeNumber(0));
show(() => isPalindromeNumber(12321));                  // odd length: the middle digit is dropped by the floor
show(() => isPalindromeNumber(1221));
show(() => isPalindromeNumber(1.1));                    // false: JavaScript has no int type, so check Number.isInteger first
show(() => -7 % 10);                                    // TRAP: -7, % keeps the sign: negative input would need Math.abs or an early return

section("Reverse integer and digits");
syntax(["Math.floor(rest / 10) | rest % 10", "drop the last digit | take the last digit (there is no integer division)"]);
show(() => reverseInteger(123));
show(() => reverseInteger(-123));
show(() => reverseInteger(120));                        // trailing zero disappears: 21
show(() => reverseInteger(1534236469));                 // 9646324351 does not fit in 32 bits: 0
show(() => reverseInteger(-(2 ** 31)));                 // -2147483648 reversed is -8463847412: out of range, so 0
show(() => reverseInteger(0));
show(() => Number("1534236469".split("").reverse().join("")));   // the String shortcut does not crash in JavaScript: the range check is your job
show(() => digitsWrong(123));                           // TRAP: [ 1, NaN, NaN ]: map calls parseInt("2", 1) and parseInt("3", 2)
show(() => digits(123));                                // map(Number) ignores the extra arguments
show(() => digitSum(9875));

section("GCD, LCM and primes");
syntax(["[a, b] = [b, a % b] until b is 0", "Euclid; the destructuring swap needs no temporary variable"]);
show(() => gcd(12, 18));
show(() => gcd(7, 13));                                 // coprime: 1
show(() => gcd(0, 5));
show(() => gcd(5, 0));
show(() => (12 / gcd(12, 18)) * 18);                    // lcm = a / gcd * b (divide first: a * b grows faster)
show(() => isPrime(97));
show(() => isPrime(91));                                // 7 * 13
show(() => isPrime(2));                                 // the only even prime
show(() => isPrime(1));                                 // 1 is not prime
show(() => isPrime(-7));
show(() => isPrime(2147483647));                        // 2^31 - 1 is prime: about 23000 odd divisors tried

section("Fibonacci: iterative, memoized, bigint, and why not plain recursion");
show(() => fib(10));
show(() => fib(0));
show(() => fibMemo(50));                                // instant, thanks to the cache
show(() => Number.isSafeInteger(fib(78)));              // true: fib(78) = 8944394323791464 is the last exact one
show(() => fib(79));                                    // TRAP: above 2^53, no overflow error, just a wrong last digit (the true value ends in 1)
show(() => fibBig(79));                                 // 14472334024676221n: bigint is exact
show(() => fib(93));                                    // 12200160415121877000: a double keeps only about 16 digits, the rest is zeros (the true value ends in 738)
show(() => fibBig(93));                                 // 12200160415121876738n
show(() => fibNaiveCalls(20));                          // calls for fib(20) without a cache; the loop does 20 additions
show(() => fibNaiveCalls(30));                          // 2692537 calls for fib(30): exponential growth

section("The interview routine");
syntax(
  ["sorted input + a pair or target", "two pointers or binary search"],
  ["subarray or substring with a condition", "sliding window"],
  ["seen before? count? duplicates?", "Map or Set"],
  ["matching brackets, undo, nesting", "stack (an array with push and pop)"],
  ["shortest path with equal steps", "BFS with a queue (array + head index)"],
  ["count the ways | best value from smaller cases", "dynamic programming"],
  ["top K | K-th", "sort, buckets by count, or a heap written by hand (no built-in PriorityQueue)"],
  ["intervals", "sort by start with a COMPARATOR, then sweep"],
);
note("1 CLARIFY: input size and range, empty input, duplicates, negatives, sorted or not, one answer or all");
note("2 EXAMPLES: walk one normal example and one edge case by hand before coding");
note("3 BRUTE FORCE: say the obvious solution out loud with its cost (often O(n^2))");
note("4 OPTIMIZE: name the pattern from the table above and the trade: extra space for less time");
note("5 CODE: guard clauses first, clear names, no clever one-liners; talk while you type");
note("6 TEST: run your example through the code; then empty, single element, duplicates, negatives, NaN, big numbers");
note("7 COMPLEXITY: always say time AND space, and what the worst case looks like");
note("JavaScript checklist: sort() needs a comparator, % keeps the sign, numbers are exact only to 2^53 (bigint), Array(n).fill([]) shares one array, object keys sort integer-like first");

section("Cost");
bigO(
  ["isPalindromeNumber | reverseInteger | digitSum", "O(log10 x) time, O(1) space (digitSum: O(log10 x) space for the digits)"],
  ["gcd (Euclid)", "O(log min(a, b)) time"],
  ["isPrime (trial division)", "O(sqrt n) time, O(1) space"],
  ["fib iterative | memoized", "O(n) time; O(1) space | O(n) space"],
  ["fib plain recursion", "O(2^n) time: do not use it"],
);
