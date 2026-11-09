/**
 * 20c. DECLARATION FILES: .d.ts, declare, @types/*, and the cost of any
 *     Run: npx tsx src/20-modules-and-tooling/declaration-files.ts        (add --plain for ASCII output)
 *     Every wontCompile line was proved with scripts/proof.sh (the untyped-package case with a small fake package).
 */
import { existsSync, readFileSync } from "node:fs";
import { note, section, show, syntax, thrown, title, wontCompile } from "../output";

declare const BUILD_ID: string; // ambient: tsc believes it exists; nothing at run time creates it

type User = { id: number; name: string };

/** A user-defined type guard: the only honest way to turn unknown data into User. */
function isUser(value: unknown): value is User {
  if (typeof value !== "object" || value === null) return false;
  const record = value as Record<string, unknown>;
  return typeof record.id === "number" && typeof record.name === "string";
}

/** JSON.parse is typed any; reading it into unknown forces the check before use. */
function parseUser(text: string): User | undefined {
  const data: unknown = JSON.parse(text);
  return isUser(data) ? data : undefined;
}

/** Where a package inside node_modules lives, relative to this lesson file. */
const modulePath = (path: string): URL => new URL(`../../node_modules/${path}`, import.meta.url);

title("DECLARATION FILES  -  types for code that has none");

section("What a .d.ts file is");
syntax(
  ["declare const X: T; | declare function f(): T;", "AMBIENT declaration: X exists at run time, trust me; emits no code"],
  ["declare module \"name\" { export function f(): T; }", "types for a module that has none"],
  ["declare global { interface Array<T> { ... } }", "add to a built-in or global type (in a file that is a module)"],
  ["file.d.ts", "types only, no run-time code; tsc --declaration writes one for every .ts file"],
  ["\"types\": \"index.d.ts\"  |  npm i -D @types/name", "a package ships its own types  |  community types from DefinitelyTyped"],
);
show(() => typeof BUILD_ID);                             // "undefined": declare created no variable
show(() => thrown(() => BUILD_ID));                      // ReferenceError: tsc accepted the read, the run-time did not
note("A declare is a promise to the compiler: a bundler define, a script tag or the host must supply the value");
wontCompile("declare const BUILD: string = 'x';", "TS1039: Initializers are not allowed in ambient contexts");
wontCompile("declare function f(): number { return 1; }", "TS1183: An implementation cannot be declared in ambient contexts (in a .d.ts)");

section("Packages without types");
syntax(
  ["import x from \"untyped-js-package\"", "plain JavaScript: tsc finds the .js file but no types (noImplicitAny makes that an error)"],
  ["npm i -D @types/untyped-js-package", "install the community declarations, if DefinitelyTyped has them"],
  ["declare module \"untyped-js-package\" { ... }", "or write the few declarations you use yourself, in any .d.ts of the project"],
);
wontCompile("import { parse } from 'legacy-lib';", "TS7016: Could not find a declaration file for module 'legacy-lib' (a .js file with no types)");
wontCompile("import { nope } from 'not-installed-anywhere';", "TS2307: Cannot find module or its corresponding type declarations");
show(() => existsSync(modulePath("@types/node/index.d.ts")));   // true: @types/node is only declarations; it runs nothing
const nodeTypes = JSON.parse(readFileSync(modulePath("@types/node/package.json"), "utf8")) as { name: string };
show(() => nodeTypes.name);                              // "@types/node": the types of process, node:fs, Buffer ... come from here
const es2023 = readFileSync(modulePath("typescript/lib/lib.es2023.array.d.ts"), "utf8");
show(() => es2023.includes("toSorted"));                 // true: the "lib" option picks which of these files tsc reads
note("Built-in types such as Array are also .d.ts files (lib.*.d.ts) shipped inside the typescript package");

section("any, unknown and data from outside");
syntax(
  ["JSON.parse(text)  // any", "the standard library declares the result as any: tsc stops checking everything after it"],
  ["const data: unknown = JSON.parse(text);", "park it in unknown, then narrow with a type guard (x is T)"],
);
const lib = readFileSync(modulePath("typescript/lib/lib.es5.d.ts"), "utf8");
const parseSignature = "parse(text: string, reviver?: (this: any, key: string, value: any) => any): any;";
show(() => lib.includes(parseSignature));                // true: the standard declaration of JSON.parse ends in any
const parsed = JSON.parse('{"count":1}');                // any
const wrongText: string = parsed.count + 1;              // compiles: an expression built from any is any
show(() => wrongText);                                   // 2: a number held by a string-typed variable
show(() => thrown(() => parsed.missing.deep));           // TypeError: any let a wrong property name through
show(() => parseUser('{"id":1,"name":"Ann"}'));          // { id: 1, name: 'Ann' }: the guard accepted it
show(() => parseUser('{"id":"1","name":"Ann"}'));        // undefined: id is a string, the guard says no
show(() => parseUser("[]"));                             // undefined
note("Types are promises about the shape; only a run-time check (typeof, a guard, a schema library) proves them");
