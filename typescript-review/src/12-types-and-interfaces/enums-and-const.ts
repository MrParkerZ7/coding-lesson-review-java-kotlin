/**
 * 12c. ENUMS AND CONST: numeric, string and const enums, and the as-const object that replaces them
 *      Run: npx tsx src/12-types-and-interfaces/enums-and-const.ts        (add --plain for ASCII output)
 */
import { note, section, show, syntax, title, wontCompile } from "../output";

title("ENUMS AND CONST  -  enum is real JavaScript code; as const is the lighter way");

section("Numeric enum: an object with a reverse mapping");
syntax(
  ["enum Name { A, B = 5, C }", "numbers from 0, or one more than the previous member (A=0, B=5, C=6)"],
  ["Name[number]", "the reverse mapping: number to member name (numeric enums only)"],
);
enum Color { Red, Green, Blue }
show(() => Color.Green);
show(() => Color[1]);                                   // reverse mapping: the number back to its name
show(() => Color);                                      // the enum is a plain object holding BOTH directions
show(() => Object.keys(Color));                         // 6 keys for 3 members
show(() => Object.values(Color));                       // TRAP: names AND numbers mixed together
const numbers = Object.values(Color).filter((v) => typeof v === "number");
show(() => numbers);                                    // filter the names out to get the member values
const anyNumber: number = 42;
const outOfRange: Color = anyNumber;                    // compiles: only a numeric LITERAL is range-checked
show(() => outOfRange);                                 // TRAP: not a member of Color, yet accepted
wontCompile("const c: Color = 42", "TS2322: Type '42' is not assignable to type 'Color'");

section("String enum: no reverse mapping");
syntax(["enum Name { A = 'a' }", "every member needs an explicit value; the enum holds only name to value"]);
enum Status { Active = "ACTIVE", Banned = "BANNED" }
show(() => Status.Active);
show(() => Status);                                     // half the size: no number keys
show(() => Object.values(Status));                      // clean: only the values
const label = (s: Status): string => s.toLowerCase();
show(() => label(Status.Banned));
wontCompile("label('ACTIVE')", "TS2345: a plain string is not assignable to Status, only Status.Active is");
note("A string enum is NOMINAL: even a matching text is refused. A literal union (lesson 12b) takes the text itself");

section("const enum: inlined, and not worth it");
syntax(["const enum Name { A }", "tsc replaces each use with the literal value and emits no object at all"]);
const enum Mode { Fast, Slow }
show(() => Mode.Slow);                                  // compiled by tsc to the literal 1 (checked with tsc --outDir)
wontCompile("Object.values(Mode)", "TS2475: const enums can only be used in property or index access expressions");
note("With isolatedModules (what file-by-file tools such as tsx and Babel need) an AMBIENT const enum is error TS2748: most teams avoid const enum");

section("The modern replacement: an as const object and a derived union type");
syntax(
  ["const Name = { A: 1, B: 2 } as const", "a plain object whose values are the literal types 1 and 2, all readonly"],
  ["type Name = (typeof Name)[keyof typeof Name]", "the union of its values: 1 | 2 (the value and the type share one name)"],
);
const Level = { Low: 1, High: 2 } as const;
type Level = (typeof Level)[keyof typeof Level];
const flip = (l: Level): Level => (l === Level.Low ? Level.High : Level.Low);
show(() => Level);                                      // an ordinary object: nothing generated, nothing hidden
show(() => Object.values(Level));                       // clean: no reverse entries
show(() => flip(Level.Low));
wontCompile("flip(3)", "TS2345: Argument of type '3' is not assignable to parameter of type 'Level'");
wontCompile("Level.Low = 5", "TS2540: Cannot assign to 'Low' because it is a read-only property");
show(() => Object.isFrozen(Level));                     // false: as const is compile-time only (add Object.freeze to enforce it)

section("Why the industry moves away from enum");
note("enum is NOT erasable syntax: tsc --erasableSyntaxOnly (TS 5.8) rejects it with TS1294, and so does Node's built-in type stripping");
note("Run node file.ts on Node 24 with an enum and it stops with ERR_UNSUPPORTED_TYPESCRIPT_SYNTAX (see es2023-to-es2025 in lesson 19)");
note("An enum emits real code (tsc turns it into a function that builds the object above); interfaces and type aliases vanish without a trace");
