/**
 * 20a. MODULES: import / export, default vs named, import type, ESM vs CommonJS
 *     Run: npx tsx src/20-modules-and-tooling/modules.ts        (add --plain for ASCII output)
 *     The demo imports modules-helper.ts, a file that only exports (it prints nothing).
 */
import { readFileSync } from "node:fs";
import { basename } from "node:path";
import Greeter, { VERSION, add, counter, increment, loads, type Point } from "./modules-helper";
import Anything from "./modules-helper";
import * as helper from "./modules-helper";
import { note, section, show, syntax, title, wontCompile } from "../output";

title("MODULES  -  import, export, and the two module systems");

section("Exporting and importing");
syntax(
  ["export const x = 1; export function f() {}", "NAMED exports: any number per file, imported by name"],
  ["export default value;", "ONE default export per file, imported under any name"],
  ["import { x, f as g } from \"./file\";", "named import, with an optional rename (as)"],
  ["import Name from \"./file\";", "default import (no braces)"],
  ["import * as ns from \"./file\";", "the whole module as one namespace object"],
  ["import type { T } from \"./file\"; | import { type T, x }", "types only: erased, no run-time import"],
  ["export { x } from \"./file\"; | export * from \"./file\";", "re-export (the barrel file pattern)"],
);
show(() => VERSION);                                     // a named import, used like a local const
show(() => add(2, 3));
show(() => new Greeter().greet("Ann"));                  // the default import
show(() => Anything === Greeter);                        // true: a default export has no name of its own, the importer picks one
const point: Point = { x: 1, y: 2 };                     // Point exists only for tsc: the import left no trace
show(() => point);
show(() => Object.keys(helper));                         // the namespace lists the VALUES (sorted): no Point, no Shape
note("Prefer named exports: tools can rename and find them, and a typo in an import name is a compile error");

section("Live bindings and one instance");
show(() => counter);                                     // 0
increment();
show(() => counter);                                     // 1: the import is a live VIEW of the exporting module's variable
wontCompile("import { counter } from './a'; counter = 5;", "TS2632: Cannot assign to 'counter' because it is an import");
const again = await import("./modules-helper");          // a dynamic import returns a promise of the module
show(() => again === helper);                             // true: the module is evaluated ONCE and shared
show(() => loads);                                       // [ 'evaluated' ]: its top-level code ran a single time
wontCompile("import type G from './a'; new G();", "TS1361: 'G' cannot be used as a value because it was imported using 'import type'");
note("A circular import (a imports b, b imports a) works for functions called later, but a let/const/class read while loading is still uninitialised: move the shared part into a third file");

section("ESM (import / export) vs CommonJS (require / module.exports)");
syntax(
  ["import { x } from \"./a\";  |  const { x } = require(\"./a\");", "static, hoisted, analysable  |  a function call, runs where it is written"],
  ["export const x = 1;  |  exports.x = 1;", "named exports  |  properties of exports"],
  ["export default f;  |  module.exports = f;", "the default export  |  replaces the whole export object"],
  ["await import(\"./a\")", "ESM: asynchronous, usable anywhere, also loads CommonJS"],
  ["\"type\": \"module\" in package.json | .mjs | .cjs", "how Node decides which system a file uses"],
);
const pkg = JSON.parse(readFileSync(new URL("../../package.json", import.meta.url), "utf8")) as { type?: string };
show(() => pkg.type);                                    // "module": this project's files are ES modules
show(() => typeof require);                              // "undefined": require does not exist in an ES module
show(() => typeof __dirname);                            // "undefined": use import.meta.dirname instead
show(() => basename(import.meta.dirname));               // the folder of this file
show(() => basename(import.meta.filename));              // this file
show(() => import.meta.url.startsWith("file://"));       // import.meta.url is this file's address
note("ES modules are strict mode, load asynchronously and allow top-level await (this file uses it); CommonJS is the older Node system");
note("Node built-ins are imported with the node: prefix (node:fs, node:path); a relative ESM import in plain Node needs the file extension");
