# Rules for work on review-lesson-java-kotlin

How lessons and docs are written in this repo (the "docs pattern"). Details and the lesson list live in
[`kotlin-review/README.md`](../../kotlin-review/README.md) (§ Folder convention, § Lesson plan); this file is the short rule set.

## Lessons

1. **One lesson at a time, on the owner's mark.** Write only the lesson the owner names next. Never several lessons in one go, never ahead of the plan.
2. **Lesson folder** = `kotlin-review/src/main/kotlin/review/<number>-<kebab-case-name>/`, numbered from 0 as in the README lesson plan (`0-primitive-data-type`). Create the folder only when that lesson is written.
3. **One topic per file**, never a whole lesson in one file. Each file has its own `main()` and a KDoc header with its title and a `Run:` line.
4. **Package** = the lesson name without its number (`0-primitive-data-type` -> `package review.primitive`). Folder and package differ on purpose. The run class is `<package>.<FileName>Kt`.
5. **Lessons are numbered from 0 and priority-marked** (🔴 Must · 🟡 Should · ⚪ Nice) in the README lesson plan. When a lesson is written, set its Status in that table; when the plan changes, keep numbers, folders and the "live-coding / Q&A" ranges consistent.

## Printing

6. **Write the syntax as plain Kotlin, never inside a quoted string.** One real line of code per demo: `val i = 42;  show { i }  // type inferred: Int` for a declaration, `show { 7 / 2 }  // Int / Int truncates` for an expression. No `show("7 / 2", 7 / 2, "note")` style: the code must not be typed twice.
7. **How `show { }` works:** it prints the code text and the trailing `// comment` of its own source line, read back from the lesson file at run time, plus the real value and its type. So a `show` call stays on ONE line, `show { ... }` is the last statement on it, and the note is its `// comment`. A declaration may come before it on the same line; it is then printed as the code.
8. **Name the syntax:** start each section that introduces syntax with `syntax("val name: Type = value" to "explicit type", ...)`, the template written with placeholders. Use `wontCompile(code, why)` for code the compiler rejects (confirm every claim with a scratch compile first), `note()` for a rule or an exception, `title()` once per file and `section()` per group. All print through `review.Output` (`kotlin-review/src/main/kotlin/review/Output.kt`); no bare `println` for results.
9. **Keep both output modes working:** the colored and box-line default, and plain ASCII (`-Dstyle=plain` or `NO_COLOR`), so keep strings given to `syntax`, `note` and `wontCompile` ASCII-only. Never put a raw control character, or a backslash-u escape of one, in a source file: `Output.kt` builds the ESC character at run time (`27.toChar()`).

## Content

10. **Teach the interview traps:** every lesson shows the surprise an interviewer likes (overflow, `0.1 + 0.2`, integer division, `1..0` is empty, `all {}` on an empty list ...), with the real value printed.
11. **Verify before saying done:** compile and run every file of the lesson with `mvn -q compile exec:java -Dmain.class=<package>.<FileName>Kt` and check the printed values against the notes.
