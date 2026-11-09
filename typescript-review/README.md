# typescript-review

Runnable notes for reviewing TypeScript (and the JavaScript under it). The TypeScript twin of [`kotlin-review`](../kotlin-review/README.md) and
[`java-review`](../java-review/README.md): same idea, same layout, same output, written for TypeScript 5.9 on Node 24 and generated in one pass on
2026-11-09 (the interview day). Each lesson is a folder of small files: read the code, run it, compare the output with the `// comments`.

## My story (why this exists)

- I have a **coding interview on 2026-11-09**. The interview language is not confirmed, so I keep **three** reviews: Kotlin in `kotlin-review`, Java in `java-review`, TypeScript here.
- First priority: **arrays, loops, maps and strings**. After that, whatever the plan below says matters most.
- This module sits in `review-lesson-java-kotlin` next to the other two, so I can compare the languages side by side.

## Folder convention

```
src/
  output.ts                   <- shared print helpers: title() section() syntax() bigO() show() wontCompile() note() block() thrown()
  0-primitive-types/          <- lesson folder: <number>-<kebab-case-name>
    numbers.ts                <- one topic per file, plain top-level code, no main()
    ...
scripts/
  check.sh                    <- strict typecheck of chosen lesson folders
  proof.sh                    <- prove that a snippet does NOT compile
```

**Lesson files are plain TypeScript.** A demo is one real line of code with a comment, and the output echoes that line:

```ts
syntax(["let name = value;", "type inferred"]);                // prints the syntax template of the section (pairs: [form, meaning])
const i = 42;  show(() => i);                                    // there is no int: 42 is a number
show(() => 7 / 2);                                               // 3.5: no integer division
show(() => thrown(() => (1).toFixed(101)));                      // run it, print the error class: RangeError
wontCompile("const n: number = '42';", "a string is not assignable to number");   // code tsc rejects (not run)
```

```
syntax  let name = value;                type inferred
const i = 42                  -> 42       number   there is no int: 42 is a number
7 / 2                         -> 3.5      number   3.5: no integer division
x  const n: number = '42';    does NOT compile: a string is not assignable to number
```

The code text and the `// comment` are read back from the source file when the program runs (through source maps), so nothing is written twice.
Keep each `show(...)` on one line, with the comment after it. The type column is `typeof`, or the class name for objects (`Array`, `Map`, `Object`).
Output is colored (numbers cyan, `true` green, `false` red, strings yellow, types blue, notes gray) with box lines.
It looks right in the VS Code terminal, Windows Terminal and Git Bash. If a terminal shows garbage instead,
use plain ASCII: add `--plain` (or set the environment variable `NO_COLOR=1`).

- Lessons are numbered from **0**, matching the plan below.
- Every file is self-contained: it imports only `../output`, never another lesson.
- Run a file with `npx tsx src/<folder>/<file>.ts` (add `--plain` for ASCII).
- Never delete files with `rm` plus a variable or a glob: Claude Code asks for confirmation even in bypass mode.

## Priority marks

| Mark | Meaning |
|---|---|
| 🔴 **Must** | Used in almost every live-coding problem, or asked in almost every JavaScript / TypeScript Q&A round. Review before the interview. |
| 🟡 **Should** | Often asked or often useful. Review if time allows. |
| ⚪ **Nice** | Senior-level or niche. Skim only if the job description mentions it. |

Two kinds of rounds, and they need different lessons: a **live-coding round** (solve a problem in TypeScript: lessons 0-9) and a
**Q&A round** (explain the language: lessons 10-20, with 10, 11 and 16 the ones almost every interviewer asks).

## Lesson plan

| # | Lesson | Priority | Why it matters for the interview | Status |
|---|---|---|---|---|
| 0 | Primitive types: `number`, `bigint`, `string`, `boolean`, `symbol`, `typeof`, truthiness, conversions | 🔴 | One number type (a double): `0.1 + 0.2`, no integer division, the 2^53 limit, `NaN`, falsy values and conversions decide many live-coding answers | ✅ `0-primitive-types/` (6 files, below) |
| 1 | Variables and types: `let` / `const` / `var`, scope, hoisting, annotations, `==` vs `===`, formatting | 🔴 | `var` vs `let` vs `const` and `==` vs `===` are asked directly; used on every line of code | ✅ `1-variables-and-types/` (5 files, below) |
| 2 | Loops: `for`, `for...of`, `for...in`, `while`, labels, iterators and generators, loop patterns, Big-O | 🔴 | Every live-coding problem; the `for...in` and `forEach` traps | ✅ `2-loops/` (4 files, below) |
| 3 | Arrays: create, mutate, copy vs alias, search, sort, 2D, typed arrays, Big-O | 🔴 | The most common input type in coding problems; default `sort()` is lexicographic | ✅ `3-arrays/` (6 files, below) |
| 4 | Array methods: `map` `filter` `reduce` `flatMap` `groupBy`, lazy iterators | 🔴 | Idiomatic solutions; `reduce` on an empty array and `map(parseInt)` are classic traps | ✅ `4-array-methods/` (4 files, below) |
| 5 | Maps, sets and structures: `Set`, `Map`, objects as dictionaries, stack, queue, heap by hand, Big-O | 🔴 | Two Sum, anagrams, duplicates, top-K rely on them; JavaScript has no priority queue, so you write the heap | ✅ `5-maps-sets-and-structures/` (7 files, below) |
| 6 | Strings in problems: `split`, regex, char codes, palindrome / anagram patterns | 🔴 | Palindrome, anagram, parsing problems | ✅ `6-strings-in-problems/` (4 files, below) |
| 7 | `null` and `undefined`: `?.`, `??`, narrowing, safe parsing | 🔴 | Asked in nearly every Q&A round: `null` vs `undefined`, `??` vs `||` | ✅ `7-null-and-undefined/` (4 files, below) |
| 8 | Control flow: `if`, ternary, `switch`, early return | 🔴 | FizzBuzz-style problems; `switch` fall-through and exhaustiveness are direct questions | ✅ `8-control-flow/` (4 files, below) |
| 9 | Interview drills: array, string / hash, interval / grid / DP and classic problems, plus the interview routine | 🔴 | Practice of lessons 0-8 under interview conditions | ✅ `9-interview-drills/` (5 files, below) |
| 10 | Functions and closures: arrows, `this`, closures, higher-order functions | 🔴 | `this` binding and closures are asked in nearly every JavaScript Q&A round | ✅ `10-functions-and-closures/` (4 files, below) |
| 11 | JavaScript quirks: coercion, hoisting, prototypes, oddities | 🔴 | The classic "weird JavaScript" questions: `[] + {}`, `typeof null`, `==` tables | ✅ `11-js-quirks/` (4 files, below) |
| 12 | Types and interfaces: `type` vs `interface`, unions, narrowing, enums, compatibility | 🟡 | The core of a TypeScript Q&A round. **Promote to 🔴 if the job is a TypeScript role** | ✅ `12-types-and-interfaces/` (4 files, below) |
| 13 | Classes and objects: `private` vs `#private`, inheritance, object features, immutability | 🟡 | Common "explain this feature" topics; TS `private` is compile-time only | ✅ `13-classes-and-objects/` (4 files, below) |
| 14 | Generics: constraints, `keyof`, patterns, erasure | 🟡 | Asked often in TypeScript Q&A | ✅ `14-generics/` (5 files, below) |
| 15 | Utility and advanced types: `Partial` `Pick` `Omit`, mapped and conditional types | ⚪ | Senior-level TypeScript questions | ✅ `15-utility-and-advanced-types/` (3 files, below) |
| 16 | Async and the event loop: promises, `async` / `await`, microtasks vs timers, patterns | 🔴 | Event-loop order is asked in nearly every JavaScript interview | ✅ `16-async-and-event-loop/` (7 files, below) |
| 17 | Error handling: `try` / `catch` / `finally`, custom errors, common errors, Result pattern | 🟡 | `finally` behaviour and error classes are standard Q&A | ✅ `17-error-handling/` (5 files, below) |
| 18 | Printing objects as JSON: `toString` is not JSON, `JSON.stringify` traps, `JSON.parse`, a hand-written serializer | 🟡 | "Print this result as JSON" is a common output task; the `stringify` traps (`undefined`, `NaN`, `Map`, `BigInt`, cycles) are classic | ✅ `18-json-output/` (4 files, below) |
| 19 | Modern JavaScript: what changed in ES2015 to ES2025 and TypeScript 5 | 🟡 | "What is new in ES20xx?" is a classic opener | ✅ `19-modern-js/` (3 files, below) |
| 20 | Modules and tooling: ESM vs CommonJS, `tsconfig` strictness, declaration files | ⚪ | Asked for tooling-heavy roles | ✅ `20-modules-and-tooling/` (3 files, below) |

## Suggested order

Live-coding first, then Q&A: **2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 9**, then **10 -> 11 -> 16 -> 12 -> 17** if time remains.
Lessons 0, 1 and 8 are short and can be skimmed in between. Lesson 9 (drills) is the one to repeat against the clock.
Lesson 18 stands alone: read `hand-written-json` first.

## How the priorities were decided

The marks are my judgment, not an official ranking. They start from the evidence collected for the Kotlin plan
(see [`kotlin-review/README.md`](../kotlin-review/README.md) § How the priorities were decided: live-coding problems use arrays, loops and maps;
Q&A banks ask about null handling, closures, `this`, the event loop and types), then swap the Kotlin-only topics for their JavaScript counterparts
(coercion and hoisting quirks, closures, the event loop, structural typing).
No new online research was done for this module, so check the job description and re-rank.

## Run

Once: `cd typescript-review`, then `npm install`. Use **Node 24**: every lesson was verified on it, and a few ES2025 features in lessons 17 and 19 (`Promise.try`, `RegExp.escape`, `using`) do not exist on Node 22.

```
cd typescript-review
npm start                                                          # 0-primitive-types / numbers (default)
npx tsx src/0-primitive-types/big-integers.ts                      # any other file: npx tsx src/<folder>/<file>.ts
npx tsx src/0-primitive-types/numbers.ts --plain                   # ASCII output
npm run check                                                      # strict typecheck of every lesson
bash scripts/check.sh src/3-arrays                                 # typecheck one lesson folder
bash scripts/proof.sh /path/to/snippet.ts                          # does this snippet compile? prints the real tsc errors
```

Files of lesson 0 (`src/0-primitive-types/`): `numbers` · `big-integers` · `strings-basics` · `booleans-and-truthiness` · `symbols-and-typeof` · `type-conversions`.
Files of lesson 1 (`src/1-variables-and-types/`): `let-const-var` · `type-annotations` · `assertions-and-satisfies` · `equality-and-identity` · `text-and-formatting`.
Files of lesson 2 (`src/2-loops/`): `for-loops` · `while-and-control` · `iterators-and-generators` · `loop-patterns`.
Files of lesson 3 (`src/3-arrays/`): `array-basics` · `array-mutation` · `copy-vs-alias` · `search-and-sort` · `two-dimensional` · `typed-arrays`.
Files of lesson 4 (`src/4-array-methods/`): `map-filter-find` · `reduce` · `grouping-and-counting` · `lazy-iterators`.
Files of lesson 5 (`src/5-maps-sets-and-structures/`): `set-basics` · `map-basics` · `objects-as-dictionaries` · `counting-and-grouping` · `stack-and-queue` · `deque-and-bfs` · `heap-by-hand`.
Files of lesson 6 (`src/6-strings-in-problems/`): `string-operations` · `regex-basics` · `chars-and-codes` · `string-patterns`.
Files of lesson 7 (`src/7-null-and-undefined/`): `null-vs-undefined` · `optional-chaining-and-nullish` · `narrowing-and-guards` · `safe-parsing`.
Files of lesson 8 (`src/8-control-flow/`): `if-and-ternary` · `switch-statement` · `early-return` · `classics-with-control-flow`.
Files of lesson 9 (`src/9-interview-drills/`): `array-drills` · `in-place-and-search-drills` · `string-and-hash-drills` · `intervals-and-graphs` · `classics-and-approach`.
Files of lesson 10 (`src/10-functions-and-closures/`): `function-forms` · `this-and-binding` · `closures` · `higher-order-functions`.
Files of lesson 11 (`src/11-js-quirks/`): `coercion-table` · `hoisting-and-scope` · `prototypes-under-the-hood` · `array-and-object-oddities`.
Files of lesson 12 (`src/12-types-and-interfaces/`): `object-types` · `unions-and-narrowing` · `enums-and-const` · `type-compatibility`.
Files of lesson 13 (`src/13-classes-and-objects/`): `class-basics` · `inheritance-and-abstract` · `object-features` · `equality-and-immutability`.
Files of lesson 14 (`src/14-generics/`): `generic-basics` · `constraints-and-keyof` · `generic-collections` · `generic-patterns` · `erasure-and-variance`.
Files of lesson 15 (`src/15-utility-and-advanced-types/`): `utility-types` · `mapped-and-conditional` · `type-level-tests`.
Files of lesson 16 (`src/16-async-and-event-loop/`): `promises` · `event-loop-order` · `await-and-timers` · `async-patterns` · `async-error-handling` · `async-loops-and-cancel` · `callbacks-to-promises`.
Files of lesson 17 (`src/17-error-handling/`): `try-catch-finally` · `error-objects-and-using` · `custom-errors` · `result-and-never` · `common-errors`.
Files of lesson 18 (`src/18-json-output/`): `to-string-is-not-json` · `json-stringify` · `json-parse` · `hand-written-json`. Helper files without output: `sample-data`.
Files of lesson 19 (`src/19-modern-js/`): `es2015-to-es2019` · `es2020-to-es2022` · `es2023-to-es2025`.
Files of lesson 20 (`src/20-modules-and-tooling/`): `modules` · `tsconfig-and-strictness` · `declaration-files`. Helper files without output: `modules-helper`.
