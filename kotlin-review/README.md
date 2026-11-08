# kotlin-review

Runnable notes for reviewing Kotlin syntax and features, built **one lesson at a time**.
Each lesson is a folder of small files, each with a `main()`: read the code, run it, compare the output with the `// comments`.

## My story (why this exists)

- I have a **coding interview on 2026-11-09** and need to refresh Kotlin quickly.
- First priority: **lists, loops and iterable syntax**. After that, whatever the plan below says matters most.
- This module sits in `review-lesson-java-kotlin` next to the Java Spring Boot calculator API, so I can compare Java and Kotlin side by side.
- I learn **slowly, one lesson at a time, on my own mark**: a new lesson folder is written only when I say which one is next.

## Folder convention

```
src/main/kotlin/review/
  Output.kt                   <- shared print helpers: title() section() syntax() show { } wontCompile() note()
  0-primitive-data-type/      <- lesson folder: <number>-<kebab-case-name>
    IntegerTypes.kt           <- one topic per file, each with its own main()
    ...
```

**Lesson files are plain Kotlin.** A demo is one real line of code with a comment, and the output echoes that line:

```kotlin
syntax("val name: Type = value" to "explicit type")      // prints the syntax template of the section
val i = 42;  show { i }                                   // type inferred: Int
show { 7 / 2 }                                            // Int / Int truncates toward zero
wontCompile("val l: Long = i", "Int is never widened")    // code the compiler rejects (not run)
```

```
syntax  val name: Type = value          explicit type
val i = 42                    → 42      Int      type inferred: Int
7 / 2                         → 3       Int      Int / Int truncates toward zero
×  val l: Long = i            does NOT compile: Int is never widened
```

The code text and the `// comment` are read back from the source file when the program runs, so nothing is written twice.
Keep each `show { }` on one line, with the comment after it.
Output is colored (numbers cyan, `true` green, `false` red, chars and strings yellow, types blue, notes gray) with box lines.
It looks right in the IntelliJ Run window, Windows Terminal and Git Bash. If a terminal shows garbage instead,
use plain ASCII: `mvn -q compile exec:java -Dstyle=plain` (or set the environment variable `NO_COLOR=1`).

- Lessons are numbered from **0**, matching the plan below. A lesson folder is created only when that lesson is written.
- A Kotlin package cannot start with a digit or contain hyphens, so the package is the lesson name without the number:
  `0-primitive-data-type` -> `package review.primitive`. Folder and package differ on purpose; IntelliJ may show a weak
  "package does not match directory" hint, which can be ignored.
- Run class name = package + file name + `Kt`: `IntegerTypes.kt` in `review.primitive` -> `review.primitive.IntegerTypesKt`.

## Priority marks

| Mark | Meaning |
|---|---|
| 🔴 **Must** | Used in almost every live-coding problem, or asked in almost every Kotlin Q&A round. Review before the interview. |
| 🟡 **Should** | Often asked or often useful. Review if time allows. |
| ⚪ **Nice** | Senior-level or niche. Skim only if the job description mentions it. |

Two kinds of rounds, and they need different lessons: a **live-coding round** (solve a problem in Kotlin: lessons 0-8 and the drills)
and a **Kotlin Q&A round** (explain features: lessons 10-20).

## Lesson plan

| # | Lesson | Priority | Why it matters for the interview | Status |
|---|---|---|---|---|
| 0 | Primitive and basic types: Int/Long/Double/Float/Boolean/Char, overflow, integer division, conversions, unsigned, `BigInteger` / `BigDecimal`, the `String` type | 🔴 | Int overflow, `/` vs `%`, `0.1 + 0.2`, `Char` arithmetic and explicit `toInt()` / `toLong()` decide many live-coding answers; `BigDecimal` is the standard answer for money | ✅ `0-primitive-data-type/` (8 files, below) |
| 1 | Variables and types: `val` vs `var`, type inference, `==` vs `===`, string templates | 🔴 | Asked directly in Q&A rounds (`val` vs `var`, `==` vs `===`); used on every line of code | ⬜ |
| 2 | Loops: `for`, ranges, `step` / `downTo` / `until`, `withIndex`, `while`, labels, `break` / `continue` | 🔴 | Every live-coding problem; the `1..0` and modify-while-looping traps | ⬜ |
| 3 | Lists and arrays: create, access, mutate, slice, sort, copy vs alias, 2D | 🔴 | The most common input type in coding problems | ⬜ |
| 4 | Iterable operations: `map` `filter` `fold` `reduce` `groupBy` `associate` `zip` `windowed` `partition` | 🔴 | Idiomatic Kotlin solutions; `fold` vs `reduce` is a direct Q&A question | ⬜ |
| 5 | Sets and maps: frequency counting, `getOrPut`, `groupingBy().eachCount()`, set operations | 🔴 | Two Sum, anagrams, duplicates, top-K: all rely on them | ⬜ |
| 6 | Strings in problems: `split`, `StringBuilder`, `reversed`, char tricks, palindrome / anagram patterns (the `String` type itself is in lesson 0) | 🔴 | Palindrome, anagram, parsing problems | ⬜ |
| 7 | Null safety: `?`, `?.`, `?:`, `!!`, `let`, smart casts, `toIntOrNull` | 🔴 | Kotlin's headline feature; asked in nearly every Q&A round and visible in any code you write | ⬜ |
| 8 | Control flow: `if` / `when` as expressions, ranges in `when`, `return` / early exit | 🔴 | FizzBuzz-style problems; `when` is a direct Q&A question | ⬜ |
| 9 | Interview drills: FizzBuzz, Two Sum, palindrome, Kadane, binary search, merge sorted, top-K | 🔴 | Practice of lessons 0-8 under interview conditions | ⬜ |
| 10 | Functions: default and named args, single-expression, `vararg`, higher-order functions, lambdas, `it`, `return@label` | 🟡 | Higher-order functions and lambda `return` behaviour are standard Q&A | ⬜ |
| 11 | Classes and data modeling: constructors / `init`, `data class`, `object` / `companion object`, `enum`, `sealed class` / `sealed interface` | 🟡 | The most common "explain this Kotlin feature" topics; also used in design-style coding tasks | ⬜ |
| 12 | Collections in depth: read-only vs mutable, `Sequence` vs `Iterable`, custom `Iterable` | 🟡 | `Sequence` vs `Iterable` and read-only vs mutable are direct Q&A questions | ⬜ |
| 13 | Scope functions: `let` `run` `with` `apply` `also` | 🟡 | Asked as "difference between them"; easy points | ⬜ |
| 14 | Extension functions and properties | 🟡 | Asked directly; also how many standard-library functions work | ⬜ |
| 15 | Exceptions: `try` as an expression, `require` / `check`, `Result`, `Nothing` | 🟡 | Edge-case handling in coding problems; `Nothing` / `Unit` / `Any` questions | ⬜ |
| 16 | Kotlin vs Java and interop: platform types, `@JvmStatic` / `@JvmOverloads`, nullability across the boundary | 🟡 | "Why Kotlin over Java?" opens many interviews; this repo is Java + Kotlin | ⬜ |
| 17 | Coroutines and Flow: `launch` vs `async`, dispatchers, structured concurrency, `suspend`, `Flow` | 🟡 | The biggest topic in Q&A banks (about 20 of 82 questions in one) but rarely used in algorithm rounds. **Promote to 🔴 if the job is Android, Ktor or reactive Spring** | ⬜ |
| 18 | Delegation and properties: `lazy`, `lateinit`, `by`, observable properties | ⚪ | Asked at mid level (`lateinit` vs `lazy`) | ⬜ |
| 19 | Generics and variance: `in` / `out`, `where`, star projection, `reified` | ⚪ | Senior-level questions | ✅ `19-generic-type/` (`GenericBasics` · `Variance` · `ErasureAndReified`, package `review.generics`) |
| 20 | Advanced: `inline` / `noinline` / `crossinline`, `value class`, operator overloading, DSLs, `tailrec`, contracts | ⚪ | Senior-level questions | ⬜ |

## Suggested order

Live-coding first, then Q&A: **2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 9**, then **11 -> 13 -> 16 -> 17** if time remains.
Lessons 1 and 8 are short and can be skimmed in between. Lesson 0 is done.

## How the priorities were decided

The marks are my judgment, drawn from a small amount of evidence, not an official ranking:

- One 82-question Kotlin guide groups its questions roughly as: coroutines / Flow about 20, functions and lambdas, classes and data modeling, null safety, generics, extension functions and delegation, collections (`fold` vs `reduce`, `Sequence` vs `Iterable`, read-only vs mutable), scope functions (one question).
  It has **no dedicated loop question**, but loops are inside every live-coding problem, which is why they are 🔴 here.
- Other guides repeat the same core: null safety, data and sealed classes, collections operations, coroutines, plus Ktor / Spring Boot for backend roles.
- Advice for timed coding tests: get a correct answer first, and check edge cases (single element, negatives, zero, duplicates, maximum sizes).
- Most pages found were Android-oriented or only showed snippets, so check the job description and re-rank.

Sources: [TechPrep: 82 Kotlin interview questions](https://www.techprep.app/blog/kotlin-interview-questions) (the only page read in full),
[Cracking the Kotlin Interview](https://medium.com/mobile-app-development-publication/cracking-the-kotlin-interview-3aed964cf1b5),
[Kotlin interview questions (JavaGuides)](https://www.javaguides.net/2024/06/kotlin-interview-questions.html),
[Codility tests in Kotlin (Dev Genius)](https://blog.devgenius.io/preparing-for-codility-tests-in-kotlin-a-practical-guide-for-interview-success-514bbca5c672),
[Kotlin backend interview questions (Second Talent)](https://secondtalent.com/?p=31036). The last four were seen only as search-result summaries, not read in full.

## Run

IntelliJ: right-click `kotlin-review/pom.xml` -> **Add as Maven Project** (once), then click the green arrow beside `fun main()`.

Command line (JDK 21 + Maven):

```
cd kotlin-review
mvn -q compile exec:java                                                     # 0-primitive-data-type / IntegerTypes (default)
mvn -q compile exec:java -Dmain.class=review.primitive.DecimalTypesKt        # any other file: <package>.<FileName>Kt
```

Files of lesson 0 (all in `review.primitive`): `IntegerTypes` · `DecimalTypes` · `BooleanType` · `CharType` · `TypeConversions` · `UnsignedTypes` · `BigNumbers` · `StringType`.
Files of lesson 19 (all in `review.generics`): `GenericBasics` · `Variance` · `ErasureAndReified`.
