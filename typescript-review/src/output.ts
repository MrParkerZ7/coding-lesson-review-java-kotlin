/**
 * Print helpers so every lesson reads as plain TypeScript and prints in the same layout.
 *
 * IN THE SOURCE (plain code, one line each, the comment is the note):
 *
 *     syntax(["let name: Type = value;", "explicit type"])          // pairs: [form, meaning]
 *     const i = 42;  show(() => i);                                 // a declaration may come before show on the same line
 *     show(() => 7 / 2);                                            // 3.5: there is no integer division
 *     wontCompile("const n: number = 'a';", "a string is not a number")
 *
 * IN THE OUTPUT (the code text and the comment are read back from the source file, so nothing is typed twice):
 *
 *     syntax  let name: Type = value;           explicit type
 *     const i = 42                     -> 42        number   type of the value (typeof, or the class name)
 *     7 / 2                            -> 3.5       number   there is no integer division
 *     x  const n: number = 'a';        does NOT compile: a string is not a number
 *
 * Rules for a `show` line: one line only; `show(() => ...)` is the last statement on it; the note is the trailing `// comment`;
 * a declaration may come before it on the same line and is then printed as the code.
 *
 * Colors (ANSI) and box lines (UTF-8) look right in the VS Code terminal, Windows Terminal and Git Bash.
 * If a terminal shows garbage instead, switch to plain ASCII with either:
 *     npx tsx src/<lesson>/<file>.ts --plain        or        set the environment variable NO_COLOR=1
 */
import { readFileSync } from "node:fs";
import { fileURLToPath } from "node:url";
import { inspect } from "node:util";

// stack frames then point at the .ts lines, not at the code tsx generated
process.setSourceMapsEnabled(true);

const WIDTH = 92;
const LABEL_WIDTH = 44;
const VALUE_WIDTH = 22;
const TYPE_WIDTH = 10;
const SYNTAX_WIDTH = 54;

const PLAIN = process.env.NO_COLOR !== undefined || process.env.STYLE === "plain" || process.argv.includes("--plain");

// The escape character is built at run time so the source file contains no raw control bytes.
const ESC = String.fromCharCode(27) + "[";

const BOLD_CYAN = "1;96";
const BOLD_WHITE = "1;97";
const BOLD_YELLOW = "1;93";
const BOLD_GREEN = "1;92";
const BOLD_RED = "1;91";
const MAGENTA = "95";
const YELLOW = "93";
const BLUE = "94";
const GRAY = "90";

const paint = (code: string, text: string): string => (PLAIN ? text : `${ESC}${code}m${text}${ESC}0m`);
const pad = (text: string, width: number): string => text.padEnd(width);
const out = (line = ""): void => {
  process.stdout.write(line + "\n");
};

/** Big banner at the top of a file's output. */
export function title(text: string): void {
  if (PLAIN) {
    out("=".repeat(WIDTH));
    out(`  ${text}`);
    out("=".repeat(WIDTH));
    return;
  }
  const inner = WIDTH - 2;
  out(paint(BOLD_CYAN, "╭" + "─".repeat(inner) + "╮"));
  out(paint(BOLD_CYAN, "│") + paint(BOLD_WHITE, pad(`  ${text}`, inner)) + paint(BOLD_CYAN, "│"));
  out(paint(BOLD_CYAN, "╰" + "─".repeat(inner) + "╯"));
}

/** Sub-heading inside a file's output. */
export function section(text: string): void {
  out();
  if (PLAIN) out(`--- ${text} `.padEnd(WIDTH, "-"));
  else out(paint(BOLD_YELLOW, `── ${text} `.padEnd(WIDTH, "─")));
}

type Row = [form: string, meaning: string];

/** The syntax template(s) of a section: pairs of [form, meaning]. */
export function syntax(...forms: Row[]): void {
  templates("syntax", forms);
}

/** The cost of operations: pairs of [operation, cost], e.g. ["arr[i]", "O(1)"]. Complexity is a core interview topic. */
export function bigO(...costs: Row[]): void {
  templates("big-O", costs);
}

function templates(tag: string, rows: Row[]): void {
  rows.forEach(([left, right], index) => {
    const label = index === 0 ? tag : "";
    if (PLAIN) out(`  ${pad(label, 8)}${pad(left, SYNTAX_WIDTH)} ${right}`.trimEnd());
    else out("  " + paint(MAGENTA, pad(label, 8)) + paint(BOLD_GREEN, pad(left, SYNTAX_WIDTH)) + " " + paint(GRAY, right));
  });
}

/**
 * One result line. Write it as `show(() => expression);  // note` or `const x = 1; show(() => x);  // note`.
 * The code text and the note come from the line of the lesson's source file that called this function.
 */
export function show(block: () => unknown): void {
  const value = block();
  const site = callSite();
  printRow(site?.label ?? "show(...)  (source not found)", value, site?.note ?? "");
}

/** Runs a call that may throw: returns the error's class name, or "no exception". Also catches thrown strings and numbers. */
export function thrown(fn: () => unknown): string {
  try {
    fn();
    return "no exception";
  } catch (error) {
    return error instanceof Error ? error.constructor.name : `threw a ${typeName(error)}`;
  }
}

/** Code that the compiler (tsc, strict) rejects, so it cannot be run: shown with the reason. */
export function wontCompile(code: string, why: string): void {
  if (PLAIN) out(`  XX  ${pad(code, LABEL_WIDTH - 4)} does NOT compile: ${why}`);
  else out("  " + paint(BOLD_RED, "×") + "  " + paint(YELLOW, pad(code, LABEL_WIDTH - 3)) + " " + paint(BOLD_RED, "does NOT compile:") + " " + paint(GRAY, why));
}

/** A plain remark, for things that cannot be printed (an exception, a rule, a tip). */
export function note(text: string): void {
  if (PLAIN) out(`  >> ${text}`);
  else out("  " + paint(BOLD_YELLOW, "!") + "  " + paint(YELLOW, text));
}

/** A multi-line text, such as pretty-printed JSON, printed as an indented block (each line behind a bar). */
export function block(text: string): void {
  for (const line of text.split("\n")) {
    if (PLAIN) out(`  | ${line}`);
    else out("  " + paint(GRAY, "│") + " " + paint(BOLD_CYAN, line));
  }
}

function printRow(label: string, value: unknown, noteText: string): void {
  const type = typeName(value);
  if (PLAIN) {
    const tail = noteText === "" ? "" : `# ${noteText}`;
    out(`  ${pad(label, LABEL_WIDTH)} = ${pad(display(value), VALUE_WIDTH)} ${pad(type, TYPE_WIDTH)} ${tail}`.trimEnd());
    return;
  }
  const typeText = noteText === "" ? type : pad(type, TYPE_WIDTH);
  const tail = noteText === "" ? "" : " " + paint(GRAY, noteText);
  out("  " + pad(label, LABEL_WIDTH) + " " + paint(GRAY, "→") + " " + paint(valueColor(value), pad(display(value), VALUE_WIDTH)) + " " + paint(BLUE, typeText) + tail);
}

/** typeof for primitives, the class name for objects: null is "null" (typeof says "object"). */
function typeName(value: unknown): string {
  if (value === null) return "null";
  const type = typeof value;
  if (type === "object") {
    const ctor = (value as { constructor?: { name?: string } }).constructor;
    return ctor?.name ?? "Object";
  }
  return type;
}

function display(value: unknown): string {
  if (typeof value === "string") return `"${value.replace(/\n/g, "\\n").replace(/\r/g, "\\r").replace(/\t/g, "\\t")}"`;
  return inspect(value, { depth: null, breakLength: Infinity, maxArrayLength: null, maxStringLength: null, compact: true });
}

function valueColor(value: unknown): string {
  if (value === true) return "1;92"; // green
  if (value === false) return "1;91"; // red
  if (typeof value === "string") return "1;93"; // yellow
  if (value === null || value === undefined) return "1;95"; // magenta
  if (typeof value === "number" || typeof value === "bigint") return "1;96"; // cyan
  return "1";
}

// ---------------------------------------------------------------------------------------------
// Reading the call site back from the lesson's source file
// ---------------------------------------------------------------------------------------------

type CallSite = { label: string; note: string };

const sourceCache = new Map<string, string[] | null>();
const FRAME = /\(?((?:file:\/\/\/|[A-Za-z]:[\\/]|\/)[^():]*?\.(?:ts|mts|js|mjs)):(\d+):\d+\)?\s*$/;

function callSite(): CallSite | null {
  const frames = (new Error().stack ?? "").split("\n").slice(1);
  for (const frame of frames) {
    const m = FRAME.exec(frame);
    if (!m) continue;
    const file = m[1]!.startsWith("file:///") ? fileURLToPath(m[1]!) : m[1]!;
    if (/[\\/]output\.(ts|js)$/.test(file)) continue; // skip this helper's own frames
    let lines = sourceCache.get(file);
    if (lines === undefined) {
      try {
        lines = readFileSync(file, "utf8").split(/\r?\n/);
      } catch {
        lines = null;
      }
      sourceCache.set(file, lines);
    }
    const text = lines?.[Number(m[2]) - 1];
    return text === undefined ? null : parseShowLine(text);
  }
  return null;
}

/** Splits `const i = 42;  show(() => i);   // note` into the code ("const i = 42") and the note ("note"). */
function parseShowLine(text: string): CallSite | null {
  const open = /\bshow\s*\(/.exec(text);
  if (!open) return null;
  let before = text.slice(0, open.index).trim();
  while (before.endsWith(";")) before = before.slice(0, -1).trim();
  const bodyStart = open.index + open[0].length;
  let depth = 1;
  let i = bodyStart;
  let quote: string | null = null;
  while (i < text.length && depth > 0) {
    const c = text[i]!;
    if (quote !== null) {
      if (c === "\\") i++;
      else if (c === quote) quote = null;
    } else if (c === '"' || c === "'" || c === "`") {
      quote = c;
    } else if (c === "(") {
      depth++;
    } else if (c === ")") {
      depth--;
    }
    i++;
  }
  if (depth !== 0) return null;
  const body = text.slice(bodyStart, i - 1).trim().replace(/^\(\s*\)\s*=>\s*/, "");
  const rest = text.slice(i);
  const slashes = rest.indexOf("//");
  const noteText = slashes < 0 ? "" : rest.slice(slashes + 2).trim();
  return { label: before === "" ? body : before, note: noteText };
}
