/**
 * 0d. BOOLEANS AND TRUTHINESS: any value works in a condition, && and || return operands
 *     Run: npx tsx src/0-primitive-types/booleans-and-truthiness.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

/** Returns its argument: tsc cannot see the value, so it does not reject an "always truthy" test. */
function id<T>(x: T): T {
  return x;
}

/** Records that it ran, so the short-circuit demos can show which side was evaluated. */
const calls: string[] = [];
function probe(name: string, value: boolean): boolean {
  calls.push(name);
  return value;
}

title("BOOLEANS AND TRUTHINESS  -  8 falsy values, && and || return operands");

section("boolean and conversion");
syntax(
  ["let flag: boolean = true;", "only true and false; TS never converts a number or string to boolean on assignment"],
  ["Boolean(x) | !!x", "the truthiness of any value as a real boolean"],
  ["if (x) | x ? a : b | !x | x && y | x || y", "any value is accepted as a condition: JavaScript converts it"],
);
const flag = true;  show(() => flag);                    // type inferred: boolean
show(() => typeof flag);
wontCompile("const f: boolean = 0;", "TS2322: Type 'number' is not assignable to type 'boolean'");
wontCompile("const f: boolean = true & false;", "TS2447: The '&' operator is not allowed for boolean types (use &&)");
wontCompile("true + true", "TS2365: Operator '+' cannot be applied to types 'boolean' and 'boolean'");
const word = id("hello");
show(() => !!word);                                      // !! is the idiom for "truthiness as a boolean"
show(() => !word);                                       // ! converts, then negates

section("The 8 falsy values (everything else is truthy)");
syntax(["false | 0 | -0 | 0n | \"\" | null | undefined | NaN", "exactly these 8 are falsy; a condition on any of them is false"]);
show(() => Boolean(false));
show(() => Boolean(0));
show(() => Boolean(-0));                                 // negative zero is falsy too
show(() => Boolean(0n));                                 // the bigint zero
show(() => Boolean(""));                                 // the empty string
show(() => Boolean(null));
show(() => Boolean(undefined));
show(() => Boolean(NaN));                                // NaN is falsy: Number("abc") ? ... : ... takes the else branch

section("Truthy surprises");
show(() => Boolean([]));                                 // TRAP: an empty array is truthy: test .length === 0
show(() => Boolean({}));                                 // an empty object is truthy
show(() => Boolean("0"));                                // TRAP: the text "0" is truthy
show(() => Boolean("false"));                            // TRAP: the text "false" is truthy
show(() => Boolean(" "));                                // a space is not empty
show(() => Boolean(-1));                                 // any non-zero number, negative included
show(() => Boolean(Infinity));
show(() => Boolean(new Boolean(false)));                 // TRAP: a wrapper OBJECT is truthy, whatever it wraps
show(() => Boolean(() => 0));                            // every function is truthy
show(() => id<unknown>([]) == false);                           // true: but == converts differently from if (lesson 11)
show(() => id<unknown>("0") == false);                          // true as well, yet "0" is truthy in an if
note("An if (x) test and x == true are different questions: never compare to true or false, test the value");

section("&& and || return an OPERAND, not a boolean");
syntax(
  ["a && b", "a if a is falsy, otherwise b"],
  ["a || b", "a if a is truthy, otherwise b"],
);
const ann: string = id("Ann");
const empty: string = id("");
const zero: number = id(0);
show(() => ann && "second");                             // "second": ann is truthy, so && gives the right operand
show(() => empty && "second");                           // "": the falsy left operand itself, not false
show(() => ann || "default");                            // "Ann": the first truthy operand
show(() => empty || "default");                          // "default": the classic fallback
show(() => zero || 10);                                  // TRAP: 10, a legitimate 0 is replaced; use ?? (lesson 7)
show(() => zero ?? 10);                                  // 0: ?? replaces only null and undefined
show(() => id(1) && id(2) && id(3));                     // 3: the last operand when all are truthy
show(() => id(1) && id(0) && id(3));                     // 0: stops at the first falsy
show(() => id(null) || id(undefined));                   // undefined: the last operand when none is truthy
show(() => typeof (ann && "x"));                         // string: the result is a string, not a boolean
show(() => typeof !!(ann && "x"));                       // boolean: wrap in !! when a real boolean is needed

section("Short-circuit: the right side may never run");
const no = id(false);
const yes = id(true);
const r1 = no && probe("A", true);
const r2 = yes || probe("B", true);
show(() => calls);                                       // [] : neither A nor B was called
const r3 = yes && probe("C", false);
const r4 = no || probe("D", false);
show(() => calls);                                       // C and D ran: the left side did not decide
show(() => [r1, r2, r3, r4]);
note("a && a.b guards a missing value, but it returns the falsy a itself: prefer ?. (lesson 7) and if/else (lesson 8)");
note("Test emptiness explicitly: arr.length === 0, str === \"\", value === undefined; truthiness conflates 0, \"\" and NaN");
