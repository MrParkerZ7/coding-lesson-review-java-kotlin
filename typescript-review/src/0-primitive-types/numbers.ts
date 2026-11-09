/**
 * 0a. NUMBERS: there is ONE number type (a 64-bit double)
 *     Run: npx tsx src/0-primitive-types/numbers.ts        (add --plain for ASCII output)
 *     Or run the npm script:  npm start
 */
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("NUMBERS  -  number, NaN, Infinity, safe integers");

section("Declaring and literals");
syntax(
  ["let name: number = value;", "explicit type"],
  ["let name = value;", "type inferred from the value (number)"],
  ["42 | 3.14 | 0xff | 0b1010 | 0o17 | 1_000_000 | 1e3", "integer | decimal | hex | binary | octal | digit separator | exponent"],
);
const i = 42;       show(() => i);                      // there is no int: 42 is a number
const d = 3.14;     show(() => d);                      // the same type for decimals
show(() => 0xff);                                       // hexadecimal literal
show(() => 0b1010);                                     // binary literal
show(() => 1_000_000);                                  // underscores are only for readability
show(() => 1e3);                                        // exponent form
show(() => typeof 42 === typeof 4.2);                   // one type for both
wontCompile("const n: number = '42';", "a string is not assignable to number");

section("Limits: a double has 53 bits of integer precision");
syntax(["Number.MAX_SAFE_INTEGER | Number.MIN_SAFE_INTEGER", "the integers that are exact: +-(2^53 - 1)"]);
show(() => Number.MAX_SAFE_INTEGER);                    // 9007199254740991
show(() => Number.MIN_SAFE_INTEGER);
show(() => Number.MAX_SAFE_INTEGER + 1);                // still exact
show(() => Number.MAX_SAFE_INTEGER + 2);                // TRAP: not +2, the double cannot hold it
show(() => 2 ** 53 === 2 ** 53 + 1);                    // two different integers, the same double
show(() => Number.isSafeInteger(2 ** 53));
show(() => Number.MAX_VALUE);                           // the largest finite double
show(() => Number.MAX_VALUE * 2);                       // overflow gives Infinity, no exception
note("Need integers above 2^53 (ids, factorials, sums)? Use bigint (the next file)");

section("Division and remainder: no integer division");
syntax(
  ["a / b", "always a floating-point division: 7 / 2 is 3.5"],
  ["Math.trunc(a / b) | Math.floor(a / b)", "integer part toward zero | toward minus infinity"],
  ["a % b", "remainder, keeps the sign of a"],
  ["((a % b) + b) % b", "modulo that is never negative when b is positive"],
);
show(() => 7 / 2);                                      // 3.5: there is no integer division
show(() => Math.trunc(7 / 2));                          // the integer part
show(() => Math.trunc(-7 / 2));                         // toward zero
show(() => Math.floor(-7 / 2));                         // toward minus infinity: differs for negatives
show(() => 7 % 3);
show(() => -7 % 3);                                     // % keeps the sign of the left side
show(() => ((-7 % 3) + 3) % 3);                         // the non-negative modulo
show(() => 7 / 0);                                      // Infinity: no exception
show(() => -7 / 0);
show(() => 0 / 0);                                      // NaN

section("Floating-point traps");
show(() => 0.1 + 0.2);                                  // binary fractions cannot hold 0.1 exactly
show(() => 0.1 + 0.2 === 0.3);
show(() => Math.abs(0.1 + 0.2 - 0.3) < Number.EPSILON); // compare with a tolerance instead
show(() => (0.1 + 0.2).toFixed(2));                     // toFixed returns a STRING
show(() => Math.round(2.5));                            // rounds half up (toward +infinity)
show(() => Math.round(-2.5));                           // -2: half goes toward +infinity, not away from zero
show(() => 1.005 * 1000);                               // 1004.9999999999999: money in doubles is a bug
note("Money: work in integer cents, or use a decimal library; never sum prices as doubles");

section("NaN, Infinity and -0");
syntax(
  ["Number.isNaN(x)", "the only reliable NaN test (NaN !== NaN)"],
  ["Number.isFinite(x)", "false for NaN, Infinity and -Infinity"],
);
const nan = Number("abc");                              // the usual way a NaN is born: a failed conversion
show(() => nan === nan);                                // NaN is not equal to anything, itself included
wontCompile("if (nan === NaN) { }", "TS2845: this condition will always return 'false'");
show(() => Number.isNaN(NaN));
show(() => Number.isNaN("abc"));                        // false: no conversion, "abc" is not the NaN value
show(() => isNaN("abc" as unknown as number));          // true: the old global isNaN converts first
show(() => Number.isFinite(1 / 0));
show(() => -0 === 0);                                   // true: they compare equal
show(() => Object.is(-0, 0));                           // false: Object.is tells them apart
show(() => 1 / -0);                                     // -Infinity

section("Bit operations work on 32-bit integers");
syntax(
  ["a & b | a | b | a ^ b | ~a", "and | or | xor | not (on 32-bit signed integers)"],
  ["a << n | a >> n | a >>> n", "shift left | shift right (keeps sign) | shift right (zero fill)"],
);
show(() => 5 & 3);
show(() => 5 | 3);
show(() => 5 ^ 3);
show(() => ~5);
show(() => 5 << 1);                                     // multiply by 2
show(() => -8 >> 1);                                    // keeps the sign
show(() => -8 >>> 28);                                  // zero fill: a big positive number
show(() => 2 ** 31 | 0);                                // TRAP: bit operations convert to 32 bits first
show(() => 3000000000 | 0);                             // wraps like a Java int
show(() => 7.9 | 0);                                    // the idiom for "truncate to int" (only inside 32 bits)

section("Errors you can trigger");
show(() => thrown(() => (1).toFixed(101)));             // RangeError: the digits argument is out of range
show(() => thrown(() => (5).toString(1)));              // RangeError: radix must be 2..36
show(() => (255).toString(16));                         // number to hex text
show(() => (255).toString(2));
