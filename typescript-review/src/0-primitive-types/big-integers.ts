/**
 * 0b. BIGINT: integers of any size (the fix for the 2^53 limit of number)
 *     Run: npx tsx src/0-primitive-types/big-integers.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

/** n! with a number: exact only while the result stays below 2^53. */
function factorialNumber(n: number): number {
  let result = 1;
  for (let i = 2; i <= n; i++) result *= i;
  return result;
}

/** n! with a bigint: exact for any n, the only cost is memory and time. */
function factorialBig(n: bigint): bigint {
  let result = 1n;
  for (let i = 2n; i <= n; i++) result *= i;
  return result;
}

/** The loose comparison on purpose, with both sides typed unknown, because tsc rejects `bigint == number`. */
function looseEquals(a: unknown, b: unknown): boolean {
  return a == b;
}

title("BIGINT  -  whole numbers without a size limit");

section("Declaring and literals");
syntax(
  ["let name: bigint = 10n;", "a bigint literal ends in n"],
  ["BigInt(value)", "convert a number, a numeric string or a boolean (no new: BigInt is not a constructor)"],
  ["typeof value === \"bigint\"", "the runtime type test"],
);
const i = 10n;  show(() => i);                           // the n suffix makes it a bigint, not a number
show(() => typeof i);
show(() => 0xffn);                                       // hex, binary and octal literals take the n suffix too
show(() => 1_000_000n);                                  // numeric separators work as well
const fromNumber = BigInt(42);  show(() => fromNumber);  // from a safe integer
const text = "12345678901234567890";
show(() => BigInt(text));                                // from a string: no precision lost (20 digits)
show(() => BigInt("0xff"));                              // the string may use a 0x prefix
show(() => BigInt(true));                                // a boolean converts too: 1n
show(() => thrown(() => BigInt(1.5)));                   // RangeError: only whole numbers convert
show(() => thrown(() => BigInt("abc")));                 // SyntaxError: not a number
const Big = BigInt as unknown as new (x: number) => bigint;   // the cast hides what tsc forbids
show(() => thrown(() => new Big(1)));                    // TypeError: BigInt is not a constructor (tsc: TS7009 on new BigInt(1))
wontCompile("const b: bigint = 10;", "TS2322: Type 'number' is not assignable to type 'bigint'");
wontCompile("const n: number = 10n;", "TS2322: Type 'bigint' is not assignable to type 'number'");

section("Arithmetic: whole numbers only, no mixing");
syntax(
  ["a + b | a - b | a * b | a ** b", "both sides MUST be bigint"],
  ["a / b | a % b", "integer division: the result is truncated toward zero; % keeps the sign of a"],
);
show(() => 2n + 3n);
show(() => 7n / 2n);                                     // 3n: no fraction, truncated toward zero
show(() => -7n / 2n);                                    // -3n: toward zero, not -4n
show(() => -7n % 3n);                                    // -1n: the sign of the left side, as with number
show(() => 2n ** 64n);                                   // far beyond 2^53: still exact
show(() => thrown(() => 1n / 0n));                       // RangeError: unlike number, bigint division by zero THROWS
show(() => thrown(() => 2n ** -1n));                     // RangeError: a negative exponent has no whole result
wontCompile("1n + 1", "TS2365: Operator '+' cannot be applied to types '1n' and '1'");
const fake = 4n as unknown as number;                    // a cast hides the bigint from tsc, the runtime still knows
show(() => thrown(() => fake + 1));                      // TypeError at run time: bigint + number
show(() => thrown(() => Math.sqrt(fake)));               // TypeError: Math.* wants numbers (tsc: TS2345)
wontCompile("+10n", "TS2736: Operator '+' cannot be applied to type 'bigint' (use Number(x))");
show(() => -10n);                                        // unary minus is fine

section("Why bigint: factorial of 25");
show(() => factorialNumber(25));                         // 1.5511210043330986e+25: rounded, the digits are wrong
show(() => factorialBig(25n));                           // 15511210043330985984000000n: every digit exact
show(() => factorialBig(30n).toString().length);         // 33 digits: the 30! with a number would be approximate
show(() => Number.MAX_SAFE_INTEGER + 2);                 // 9007199254740992: number loses the +2
show(() => BigInt(Number.MAX_SAFE_INTEGER) + 2n);        // 9007199254740993n: bigint keeps it

section("Comparing and converting");
syntax(
  ["a === b | a < b", "=== needs the same type; < and > may mix bigint and number"],
  ["Number(big) | BigInt(num)", "convert; Number(big) rounds when big is above 2^53"],
  ["BigInt.asUintN(bits, x) | BigInt.asIntN(bits, x)", "wrap into an unsigned | signed integer of that many bits"],
);
show(() => 1n === 1n);
show(() => looseEquals(1n, 1));                          // true: == converts, so 1n == 1
show(() => 1n === (1 as unknown as bigint));             // false: === never converts (tsc rejects 1n === 1 directly: TS2367)
show(() => 1n < 2);                                      // true: ordering a bigint against a number is allowed
show(() => 2n > 1.5);                                    // true: compared by mathematical value
show(() => 0n === -0n);                                  // true: a bigint has no negative zero
show(() => Number(2n ** 64n));                           // 18446744073709552000: the nearest double, not exact
show(() => Number(9007199254740993n));                   // 9007199254740992: rounded
show(() => (255n).toString(16));                         // to text in any radix 2..36
show(() => BigInt.asUintN(64, -1n));                     // 18446744073709551615n: the largest unsigned 64-bit value
show(() => BigInt.asIntN(64, 2n ** 63n));                // -9223372036854775808n: overflow wraps, as a Java long would
show(() => BigInt.asIntN(8, 200n));                      // -56n: 200 does not fit a signed byte
note("bigint never overflows by itself: the wrap above happens only when YOU ask for a fixed width");

section("Pitfalls and when to use it");
show(() => thrown(() => JSON.stringify({ id: 1n })));    // TypeError: JSON cannot hold a bigint (lesson 18: convert to a string first)
show(() => String(2n ** 70n));                           // text conversion is the safe way out
show(() => Boolean(0n));                                 // false: 0n is falsy, every other bigint is truthy (lesson on booleans)
show(() => Boolean(-5n));                                // true
show(() => typeof Object(1n));                           // "object": a wrapper object; rarely seen, never create one
note("Use bigint for ids above 2^53, exact big sums, factorials, hashes and crypto; use number for everything else");
note("bigint is slower than number and does not mix with Math.*, so convert at the edges with Number() / BigInt()");
