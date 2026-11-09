/**
 * 8d. CLASSICS WITH CONTROL FLOW: FizzBuzz, leap year, grades, day names, sign, odd/even
 *     Run: npx tsx src/8-control-flow/classics-with-control-flow.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title } from "../output";

/** FizzBuzz word for one number: the 15 case MUST come first. */
function fizzBuzzWord(n: number): string {
  if (n % 15 === 0) return "FizzBuzz";
  if (n % 3 === 0) return "Fizz";
  if (n % 5 === 0) return "Buzz";
  return String(n);
}

/** FizzBuzz with the branches in the wrong order: 15 is caught by the Fizz test. */
function fizzBuzzWrong(n: number): string {
  if (n % 3 === 0) return "Fizz";
  if (n % 5 === 0) return "Buzz";
  if (n % 15 === 0) return "FizzBuzz";
  return String(n);
}

/** FizzBuzz for 1..n. Array.from with a length builds the range (lesson 2). */
const fizzBuzz = (n: number): string[] => Array.from({ length: n }, (_, i) => fizzBuzzWord(i + 1));

/** Leap year: divisible by 400, or by 4 but not by 100. */
function isLeapYear(y: number): boolean {
  if (y % 400 === 0) return true;
  if (y % 100 === 0) return false;
  return y % 4 === 0;
}

/** Grades, tested from the top: no gaps between the ranges, and bad input is rejected first (a NaN fails every comparison). */
function gradeOf(score: number): string {
  if (!(score >= 0 && score <= 100)) return "invalid";
  if (score >= 90) return "A";
  if (score >= 80) return "B";
  if (score >= 70) return "C";
  return "F";
}

/** TRAP: integer ranges like 80..89 leave a gap for a decimal score such as 89.5. */
function gradeWithGap(score: number): string {
  if (score >= 90 && score <= 100) return "A";
  if (score >= 80 && score <= 89) return "B";
  return "invalid";
}

function dayName(n: number): string {
  switch (n) {
    case 1: return "Mon";
    case 2: return "Tue";
    case 3: return "Wed";
    case 4: return "Thu";
    case 5: return "Fri";
    case 6: return "Sat";
    case 7: return "Sun";
    default: return "?";
  }
}

const DAYS = ["Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"];
const dayNameTable = (n: number): string => DAYS[n - 1] ?? "?";   // the same with data: 0 and 1.5 give undefined, so "?"

/** Days in a month, with grouped cases; undefined for a month that does not exist. */
function daysInMonth(month: number, year: number): number | undefined {
  switch (month) {
    case 4: case 6: case 9: case 11:
      return 30;
    case 2:
      return isLeapYear(year) ? 29 : 28;
    case 1: case 3: case 5: case 7: case 8: case 10: case 12:
      return 31;
    default:
      return undefined;
  }
}

function signOf(n: number): number {
  if (n > 0) return 1;
  if (n < 0) return -1;
  return 0;
}

/** TRAP: % keeps the sign of the left side, so an odd negative number gives -1, not 1. */
const isOddWrong = (n: number): boolean => n % 2 === 1;
const isOdd = (n: number): boolean => n % 2 !== 0;
const clamp = (x: number, low: number, high: number): number => Math.min(Math.max(x, low), high);

title("CLASSICS  -  the small control-flow problems every interview starts with");

section("FizzBuzz");
show(() => fizzBuzz(15));                               // 15 is FizzBuzz, not Fizz
show(() => fizzBuzz(0));                                // no numbers, so an empty array
show(() => fizzBuzz(1));
show(() => fizzBuzzWrong(15));                          // TRAP: "Fizz": 15 % 3 matched first, FizzBuzz is never produced
note("Most specific condition first: in an if / else if chain the order is part of the logic");

section("Leap year, days in a month");
syntax(["y % 400 === 0 || (y % 4 === 0 && y % 100 !== 0)", "the one-line form of the three rules"]);
show(() => isLeapYear(1900));                           // divisible by 100 but not by 400
show(() => isLeapYear(2000));
show(() => isLeapYear(2024));
show(() => isLeapYear(2023));
show(() => daysInMonth(2, 2024));
show(() => daysInMonth(2, 1900));
show(() => daysInMonth(4, 2026));
show(() => daysInMonth(12, 2026));
show(() => daysInMonth(13, 2026));                      // not a month: undefined

section("Grades: boundaries and gaps");
show(() => gradeOf(90));                                // boundary: 90 is an A
show(() => gradeOf(89));
show(() => gradeOf(89.5));                              // B: testing from the top leaves no gap
show(() => gradeWithGap(89.5));                         // TRAP: "invalid", 89.5 fits neither 80..89 nor 90..100
show(() => gradeOf(101));
show(() => gradeOf(-1));
show(() => gradeOf(NaN));                               // "invalid": !(NaN >= 0 && ...) is true; without the guard NaN would fall through to F

section("Day names: switch or a table");
show(() => dayName(3));
show(() => dayName(9));
show(() => dayNameTable(1));
show(() => dayNameTable(0));                            // DAYS[-1] is undefined, so the default "?"

section("Sign, odd and even, clamp");
show(() => signOf(-9));
show(() => signOf(0));
show(() => signOf(NaN));                                // 0: NaN is neither above nor below zero (Math.sign(NaN) is NaN)
show(() => Math.sign(-0));                              // -0: the built-in keeps the sign of zero, signOf says 0
show(() => isOddWrong(7));
show(() => isOddWrong(-7));                             // TRAP: false, -7 % 2 is -1
show(() => isOdd(-7));                                  // test !== 0, never === 1
show(() => clamp(15, 0, 10));
show(() => clamp(-3, 0, 10));
show(() => clamp(5, 0, 10));
