/**
 * 20b. TSCONFIG AND STRICTNESS: what tsc checks, which options matter, how tsx/node run TypeScript
 *     Run: npx tsx src/20-modules-and-tooling/tsconfig-and-strictness.ts        (add --plain for ASCII output)
 *     Every wontCompile line here was proved with scripts/proof.sh, passing the option as a flag where the text says "with".
 */
import { readFileSync } from "node:fs";
import { block, note, section, show, syntax, title, wontCompile } from "../output";

title("TSCONFIG AND STRICTNESS  -  the compiler options that decide what is an error");

section("tsc checks, tsx and node only run");
syntax(
  ["npx tsc --noEmit", "type check the whole project (this repo: npm run check): reports errors, writes nothing"],
  ["npx tsx file.ts", "run a file: types are stripped, NOTHING is type checked"],
  ["node file.ts", "Node 24 also strips types itself, but only erasable syntax (no enum, namespace, parameter properties)"],
  ["npx tsc --init", "create a tsconfig.json; tsc -p <folder> uses another one"],
);
// @ts-expect-error: tsx does not type check, so this wrong line still runs
const wrongType: number = "text";
show(() => wrongType);                                   // "text": a number-typed variable; only tsc --noEmit reports the mistake
note("The line above is a deliberate type error marked with // @ts-expect-error; run the checker in CI, the runner never will");
block(readFileSync(new URL("../../tsconfig.json", import.meta.url), "utf8").trimEnd());   // this project's real file
note("The block above is this project's tsconfig.json, read from disk");

section("strict is a bundle of nine flags");
syntax(
  ["noImplicitAny", "no silent any: an untyped parameter is an error (TS7006)"],
  ["strictNullChecks", "null and undefined are separate types: string does not accept null (TS2322)"],
  ["strictFunctionTypes", "function parameters are checked contravariantly (lesson 12)"],
  ["strictBindCallApply", "bind, call and apply check their arguments (TS2345 on a wrong one)"],
  ["strictPropertyInitialization", "a class field must be assigned in the constructor, or declared with ! (TS2564)"],
  ["strictBuiltinIteratorReturn", "built-in iterators finish with undefined, not any"],
  ["noImplicitThis", "a this with no declared type is an error (TS2683)"],
  ["useUnknownInCatchVariables", "catch (e): e is unknown, not any (TS18046)"],
  ["alwaysStrict", "emit \"use strict\" (ES modules are strict anyway)"],
);
wontCompile("function add(a, b) { return a + b; }", "TS7006: Parameter 'a' implicitly has an 'any' type (compiles with --noImplicitAny false)");
wontCompile("const s: string = null;", "TS2322: Type 'null' is not assignable to type 'string' (compiles with --strictNullChecks false)");
wontCompile("try { f(); } catch (e) { e.message; }", "TS18046: 'e' is of type 'unknown' (compiles with --useUnknownInCatchVariables false)");
wontCompile("function f(a: number) { } f.call(null, 'x');", "TS2345: Argument of type 'string' is not assignable to parameter of type 'number' (compiles with --strictBindCallApply false)");
note("Use \"strict\": true in every new project; turn single flags off only while migrating old JavaScript");

section("Worth enabling beyond strict");
syntax(
  ["noUncheckedIndexedAccess", "xs[i] and record[key] get | undefined: the honest type of an index read"],
  ["exactOptionalPropertyTypes", "a?: number no longer accepts an explicit undefined"],
  ["noImplicitReturns | noFallthroughCasesInSwitch", "a path without return | a case that falls into the next"],
  ["noUnusedLocals | noUnusedParameters", "report unused variables | parameters"],
  ["noImplicitOverride", "an overriding method must be written with the override keyword"],
  ["verbatimModuleSyntax | erasableSyntaxOnly", "keep imports as written, type-only ones marked | no enum, namespace or parameter properties"],
);
show(() => [1, 2, 3][5]);                                // undefined at run time; without the flag tsc types it as number
wontCompile("const xs: number[] = [1]; const n: number = xs[5];", "with noUncheckedIndexedAccess: TS2322 Type 'number | undefined' is not assignable to type 'number'");
wontCompile("type T = { a?: number }; const t: T = { a: undefined };", "with exactOptionalPropertyTypes: TS2375 (not assignable with 'exactOptionalPropertyTypes: true')");
wontCompile("function f(x: number) { if (x > 0) return 1; }", "with noImplicitReturns: TS7030 Not all code paths return a value");
wontCompile("switch (x) { case 1: log(1); case 2: break; }", "with noFallthroughCasesInSwitch: TS7029 Fallthrough case in switch");
wontCompile("function h() { const unused = 1; }", "with noUnusedLocals: TS6133 'unused' is declared but its value is never read");
wontCompile("class B extends A { m() { } }", "with noImplicitOverride: TS4114 This member must have an 'override' modifier");
wontCompile("import { PathLike } from 'node:fs';", "with verbatimModuleSyntax: TS1484 'PathLike' is a type and must be imported using a type-only import");
wontCompile("enum Color { Red }", "with erasableSyntaxOnly: TS1294 This syntax is not allowed when 'erasableSyntaxOnly' is enabled");

section("target, lib, module, moduleResolution");
const rows: [string, string, string][] = [
  ["option", "what it controls", "this project"],
  ["target", "the JavaScript version of the OUTPUT syntax", "ES2024"],
  ["lib", "which built-in types exist (toSorted, Promise.withResolvers, DOM ...)", "ES2024 + 2 more"],
  ["module", "the module system of the output (ESNext, CommonJS, NodeNext)", "ESNext"],
  ["moduleResolution", "how an import path is found (bundler, node16/nodenext, node10)", "bundler"],
  ["types", "which @types packages are loaded globally", "node"],
  ["skipLibCheck", "do not type check .d.ts files: faster, hides conflicts in libraries", "true"],
  ["noEmit", "check only, write no JavaScript", "true"],
];
block(rows.map(([option, controls, value]) => option.padEnd(18) + controls.padEnd(70) + value).join("\n"));
wontCompile("[3, 1].toSorted()", "with --lib ES2020: TS2550 Property 'toSorted' does not exist; change the lib option to es2023 or later");
wontCompile("import { x } from './helper';", "with --module nodenext: TS2835 Relative import paths need explicit file extensions (use './helper.js')");
note("target changes the emitted syntax, lib only changes which APIs tsc knows: neither adds a polyfill, the runtime must have the API");
