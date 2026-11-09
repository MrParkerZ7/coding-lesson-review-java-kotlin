/**
 * 0f. TYPE CONVERSIONS: Number() vs parseInt(), String(), Boolean(), and the implicit ones
 *     Run: npx tsx src/0-primitive-types/type-conversions.ts        (add --plain for ASCII output)
 *     (the surprising == and + results: lesson 11; safe parsing with a typed result: lesson 7)
 */
import { inspect } from "node:util";
import { block, note, section, show, syntax, thrown, title, wontCompile } from "../output";

title("TYPE CONVERSIONS  -  explicit is safe, implicit is a trap");

section("Text to number: Number() vs parseInt() vs parseFloat()");
syntax(
  ["Number(text) | +text", "the WHOLE text must be a number, else NaN; \"\" and blanks give 0"],
  ["parseInt(text, radix) | parseFloat(text)", "read a number from the START and ignore the rest; no digits at all gives NaN"],
);
show(() => Number("12px"));                              // NaN: "px" is not part of a number
show(() => parseInt("12px"));                            // 12: stops at the first non-digit
show(() => parseFloat("3.14abc"));                       // 3.14
show(() => parseFloat(".5"));
show(() => parseInt("abc"));                             // NaN: nothing to read
show(() => Number(""));                                  // TRAP: 0, an empty (or blank) text is zero
show(() => parseInt(""));                                // NaN: nothing to read
show(() => Number("  42  "));                            // 42: surrounding blanks are ignored
show(() => +"3");                                        // the unary plus is Number("3")
show(() => parseInt("08"));                              // 8: a leading 0 is NOT octal any more; still pass radix 10 on user input
show(() => parseInt("3.9"));                             // 3: it truncates, it does not round
show(() => parseInt("1e3"));                             // 1: it stops at the e; Number("1e3") would be 1000
show(() => Number("1e3"));
show(() => Number("0x1f"));                              // 31: hex text is understood
show(() => parseInt("1f", 16));                          // 31: the radix says which base
show(() => parseInt("11001", 2));                        // 25: binary text to number
show(() => Number("1_000"));                             // NaN: separators work in source code, not in text
show(() => parseInt("1_000"));                           // 1
show(() => ["1", "2", "3"].map(parseInt));               // TRAP: [1, NaN, NaN]: map passes the index, and parseInt reads it as the radix
show(() => ["1", "2", "3"].map(Number));                 // fine: Number ignores the extra arguments
show(() => ["1", "2", "3"].map((s) => parseInt(s, 10)));
const tiny = 0.0000005;
show(() => String(tiny));                                // "5e-7": the text of a small number uses an exponent
show(() => parseInt(String(tiny)));                      // TRAP: 5, parseInt turns its argument into text first, then stops at the e
show(() => parseInt("12345678901234567890"));            // 12345678901234567000: beyond 2^53 the digits are rounded
show(() => BigInt("12345678901234567890"));              // exact: use bigint for such ids
wontCompile("parseInt(5)", "TS2345: Argument of type 'number' is not assignable to parameter of type 'string'");
note("The decision: Number() when the whole text must be a number (validation), parseInt/parseFloat when a unit may follow");

section("Anything to number: Number(x)");
show(() => Number(null));                                // 0
show(() => Number(undefined));                           // NaN: the same two look-alikes give different numbers
show(() => Number(true));
show(() => Number([]));                                  // 0: [] becomes "" which becomes 0
show(() => Number([5]));                                 // 5: a one-element array becomes its text, then a number
show(() => Number([1, 2]));                              // NaN: "1,2" is not a number
show(() => Number({}));                                  // NaN: "[object Object]"
show(() => Number(new Date(0)));                         // 0: a Date becomes its milliseconds since 1970-01-01 UTC
show(() => thrown(() => Number(Symbol("s"))));           // TypeError: a symbol has no number value
show(() => Number(12345678901234567890n));               // a bigint converts, rounded when it is above 2^53
show(() => Number.isNaN(Number("abc")));                 // the way to test a failed conversion (NaN !== NaN)

section("Anything to text and boolean");
syntax(
  ["String(x) | `${x}` | x.toString()", "String(x) never throws; toString() throws on null and undefined"],
  ["Boolean(x) | !!x", "truthiness: the 8 falsy values (previous file)"],
);
show(() => String(123));
show(() => String(null));                                // "null": the text, not an error
show(() => String(undefined));
show(() => String(true));
show(() => String([1, 2]));                              // "1,2"
show(() => String({}));                                  // "[object Object]": the useless default (lesson 18 shows JSON)
show(() => String(-0));                                  // "0": the minus sign of negative zero is not printed
show(() => String(1e21));                                // "1e+21": large numbers switch to an exponent
show(() => String(0.1 + 0.2));
const nothing = null as unknown as string;               // a cast, because tsc rejects null.toString() at compile time
show(() => thrown(() => nothing.toString()));            // TypeError: Cannot read properties of null
show(() => Boolean("false"));                            // TRAP: true, any non-empty text is truthy

section("Implicit conversion: what tsc stops and what it lets through");
const five = "5" as unknown as number;                   // a cast hides the string from tsc; the runtime still sees a string
show(() => five * 2);                                    // 10: * converts both sides to number
show(() => five - 2);                                    // 3: so does -
show(() => five + 2);                                    // TRAP: "52", + prefers text as soon as one side is text
wontCompile("'5' * 2", "TS2362: left-hand side of an arithmetic operation must be number, bigint or any");
wontCompile("const n: number = '5';", "TS2322: Type 'string' is not assignable to type 'number'");
wontCompile("const s: string = 5;", "TS2322: Type 'number' is not assignable to type 'string'");
show(() => "5" + 2);                                     // "52": tsc ALLOWS string + number, it is the one implicit mix that compiles
show(() => +"5" + 2);                                    // 7: convert first, add second
note("Write the conversion yourself: Number(x), String(x), Boolean(x). Implicit ones hide bugs and tsc only catches some");

section("Conversion table (built at run time)");
const samples: unknown[] = [false, true, 0, "", "0", "12", "abc", null, undefined, [], [5], [1, 2], {}, NaN];
const cell = (text: string, width: number): string => text.padEnd(width);
const rows = [cell("value", 12) + cell("Number()", 10) + cell("String()", 18) + "Boolean()"];
for (const v of samples) {
  rows.push(cell(inspect(v), 12) + cell(String(Number(v)), 10) + cell(JSON.stringify(String(v)), 18) + String(Boolean(v)));
}
block(rows.join("\n"));
note("Read the rows: Number([]) is 0 and Number([5]) is 5 because an array goes through its text; the Boolean column is the 8 falsy values");
