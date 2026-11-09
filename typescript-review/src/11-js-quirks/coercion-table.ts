/**
 * 11a. COERCION TABLE: the "weird JavaScript" answers, with the real values
 *      Run: npx tsx src/11-js-quirks/coercion-table.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

title("COERCION TABLE  -  == and + convert types; TypeScript refuses most of these lines");
note("Lines tagged [TS refuses] have a ts-expect-error comment above them in the source: tsc rejects them, tsx runs them anyway");

section("== converts types first, === never does");
syntax(
  ["a === b", "same type and same value: no conversion. Use this one"],
  ["a == b", "null == undefined; number vs string: the string becomes a number; a boolean becomes a number; an object becomes its primitive"],
);
// @ts-expect-error
show(() => [] == false);                                // [TS refuses] true: [] -> "" -> 0 and false -> 0
// @ts-expect-error
show(() => [] == ![]);                                  // [TS refuses] true: ![] is false (arrays are truthy), then the same as above
show(() => null == undefined);                          // true: the one pair that is equal to each other and to nothing else
show(() => null == 0);                                  // false: null is only == to undefined
// @ts-expect-error
show(() => "0" == false);                               // [TS refuses] true: both sides become the number 0
// @ts-expect-error
show(() => "0" == 0);                                   // [TS refuses] true: the string becomes a number
// @ts-expect-error
show(() => "" == 0);                                    // [TS refuses] true: "" becomes 0, so "0" == 0 and "" == 0 ...
// @ts-expect-error
show(() => "0" == "");                                  // [TS refuses] false: two strings compare as text, so == is NOT transitive
// @ts-expect-error
show(() => NaN == NaN);                                 // [TS refuses] false: NaN is the only value not equal to itself
// @ts-expect-error
show(() => [1] == 1);                                   // [TS refuses] true: [1] -> "1" -> 1
wontCompile("[] == false", "TS2367: no overlap between never[] and boolean (TS2839 adds: objects compare by reference)");
note("Rule of thumb: always ===; the one accepted use of == is x == null (null or undefined, lesson 7)");

section("+ : a string on either side means concatenation");
syntax(
  ["a + b", "after converting objects to primitives: if EITHER side is a string, join as text; otherwise add as numbers"],
  ["a - b | a * b | a / b | +a", "always numbers: both sides are converted with Number()"],
);
show(() => 1 + "2");                                   // "12": the number became text
show(() => "3" + 1);                                    // "31"
// @ts-expect-error
show(() => "3" - 1);                                    // [TS refuses] 2: minus has no text meaning, so "3" became 3
// @ts-expect-error
show(() => "5" * "2");                                  // [TS refuses] 10
show(() => "b" + "a" + +"a" + "a");                     // "baNaNa": +"a" is NaN, and NaN + "a" is text
// @ts-expect-error
show(() => true + true);                                // [TS refuses] 2: booleans become 1 + 1
// @ts-expect-error
show(() => null + 1);                                   // [TS refuses] 1: null becomes 0
// @ts-expect-error
show(() => undefined + 1);                              // [TS refuses] NaN: undefined becomes NaN
wontCompile("'3' - 1", "TS2362: the left-hand side of an arithmetic operation must be of type number");
wontCompile("true + true", "TS2365: operator + cannot be applied to types boolean and boolean");

section("Objects and arrays: [] and {} become text first");
// @ts-expect-error
show(() => [] + {});                                    // [TS refuses] "[object Object]": [] -> "" and {} -> "[object Object]"
// @ts-expect-error
show(() => [] + []);                                    // [TS refuses] "": two empty texts (an empty string, not 0 and not [])
show(() => +[]);                                        // 0: [] -> "" -> 0 (TS lets unary + take [] and {})
show(() => +{});                                        // NaN: "[object Object]" is not a number
// @ts-expect-error
show(() => [1, 2] + [3]);                               // [TS refuses] "1,23": each array becomes its join(","), then the texts meet
// @ts-expect-error
show(() => [] + null + 1);                              // [TS refuses] "null1": "" + null is text, then + 1 stays text
wontCompile("[] + {}", "TS2365: operator + cannot be applied to types never[] and {}");

section("How an object becomes a primitive: valueOf, toString, Symbol.toPrimitive");
syntax(["obj[Symbol.toPrimitive](hint)", "hint is 'number', 'string' or 'default'; without it: valueOf() then toString()"]);
const money = {
  [Symbol.toPrimitive](hint: string): string | number { return hint === "number" ? 42 : hint === "string" ? "42 USD" : "default"; },
};
show(() => +money);                                     // hint "number"
show(() => `${money}`);                                 // hint "string" (a template literal)
show(() => money + "");                                 // hint "default": + and == use this one
// @ts-expect-error
show(() => ({ valueOf: () => 42 }) + 1);                // [TS refuses] 43: valueOf wins for + (hint "default")
show(() => `${{ valueOf: () => 42 }}`);                 // "[object Object]": a template literal asks toString first

section("Numbers that surprise");
show(() => typeof NaN);                                 // "number": Not-a-Number is a number
show(() => 0.1 * 3);                                    // 0.30000000000000004 (lesson 0: numbers)
show(() => 9999999999999999);                           // 10000000000000000: above 2^53 the double rounds to the next even integer
// @ts-expect-error
show(() => 1 < 2 < 3);                                  // [TS refuses] true: (1 < 2) is true, true becomes 1, 1 < 3
// @ts-expect-error
show(() => 3 > 2 > 1);                                  // [TS refuses] false: (3 > 2) is true, true becomes 1, 1 > 1 is false
show(() => "10" < "9");                                 // true: two strings compare character by character, "1" < "9"
// @ts-expect-error
show(() => null >= 0);                                  // [TS refuses] true: >= converts null to 0, but == does not (null == 0 is false)
wontCompile("null >= 0", "TS18050: the value 'null' cannot be used here");
note("TS refuses most of this on purpose: it is the compiler's answer to 'why is JavaScript weird'. Convert explicitly: Number(x), String(x)");
