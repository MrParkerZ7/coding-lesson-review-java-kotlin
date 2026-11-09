# kotlin-review

Runnable notes for reviewing Kotlin syntax and features, built lesson by lesson (the first lessons one at a time, the rest generated in one pass on 2026-11-09).
Each lesson is a folder of small files, each with a `main()`: read the code, run it, compare the output with the `// comments`.

## My story (why this exists)

- I have a **coding interview on 2026-11-09** and need to refresh Kotlin quickly.
- First priority: **lists, loops and iterable syntax**. After that, whatever the plan below says matters most.
- This module sits in `review-lesson-java-kotlin` next to the Java Spring Boot calculator API, so I can compare Java and Kotlin side by side.
- I learn **on my own mark**: a new lesson folder is written when I say which one is next, or when I ask for the full set (done once, 2026-11-09, on the interview day).

## Folder convention

```
src/main/kotlin/review/
  Output.kt                   <- shared print helpers: title() section() syntax() bigO() show { } wontCompile() note() block()
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
  One exception: `FileFacade.kt` (lesson 16) uses `@file:JvmName("FacadeDemo")` to show that annotation, so it runs as `review.interop.FacadeDemo`.

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
| 1 | Variables and types: `val` vs `var`, type inference, `==` vs `===`, string templates, `const val`, destructuring, raw strings, NaN and `-0.0` | 🔴 | Asked directly in Q&A rounds (`val` vs `var`, `==` vs `===`); used on every line of code | ✅ `1-variables-and-types/` (3 files, below) |
| 2 | Loops: `for`, ranges, `step` / `downTo` / `until`, `withIndex`, `while`, labels, `break` / `continue`, loop patterns (two pointers, sliding window, prefix sums, binary search), Big-O | 🔴 | Every live-coding problem; the `1..0` and modify-while-looping traps | ✅ `2-loops/` (3 files, below) |
| 3 | Lists and arrays: arrays, the collection family, create, access, mutate, slice, sort, search, copy vs alias, 2D, Big-O | 🔴 | The most common input type in coding problems | ✅ `3-lists-and-arrays/` (5 files, below) |
| 4 | Iterable operations: `map` `filter` `fold` `reduce` `groupBy` `associate` `zip` `windowed` `partition` `runningFold` `sortedWith` `joinToString` | 🔴 | Idiomatic Kotlin solutions; `fold` vs `reduce` is a direct Q&A question | ✅ `4-iterable-operations/` (4 files, below) |
| 5 | Sets, maps, stack, queue, deque and heap: frequency counting, `getOrPut`, `groupingBy().eachCount()`, set operations, `ArrayDeque`, `PriorityQueue`, Big-O | 🔴 | Two Sum, anagrams, duplicates, balanced brackets, top-K: all rely on them | ✅ `5-sets-and-maps/` (4 files, below) |
| 6 | Strings in problems: `split`, regex, `StringBuilder`, `reversed`, char tricks, palindrome / anagram patterns (the `String` type itself is in lesson 0) | 🔴 | Palindrome, anagram, parsing problems | ✅ `6-strings-in-problems/` (4 files, below) |
| 7 | Null safety: `?`, `?.`, `?:`, `!!`, `let`, smart casts, `toIntOrNull`, `requireNotNull`, the `OrNull` family, the `Map[]` lookup | 🔴 | Kotlin's headline feature; asked in nearly every Q&A round and visible in any code you write | ✅ `7-null-safety/` (3 files, below) |
| 8 | Control flow: `if` / `when` as expressions, ranges in `when`, guard clauses, `return` / early exit, `Nothing`, `when` guards | 🔴 | FizzBuzz-style problems; `when` is a direct Q&A question | ✅ `8-control-flow/` (3 files, below) |
| 9 | Interview drills: array, string / hash, interval / grid / DP and classic problems, plus the interview routine | 🔴 | Practice of lessons 0-8 under interview conditions | ✅ `9-interview-drills/` (5 files, below) |
| 10 | Functions: default and named args, single-expression, `vararg`, higher-order functions, lambdas, `it`, `return@label` | 🟡 | Higher-order functions and lambda `return` behaviour are standard Q&A | ✅ `10-functions/` (3 files, below) |
| 11 | Classes and data modeling: constructors / `init`, `data class`, `object` / `companion object`, `enum`, `sealed class` / `sealed interface` | 🟡 | The most common "explain this Kotlin feature" topics; also used in design-style coding tasks | ✅ `11-classes-and-data-modeling/` (5 files, below) |
| 12 | Collections in depth: read-only vs mutable, `Sequence` vs `Iterable`, custom `Iterable` | 🟡 | `Sequence` vs `Iterable` and read-only vs mutable are direct Q&A questions | ✅ `12-collections-in-depth/` (3 files, below) |
| 13 | Scope functions: `let` `run` `with` `apply` `also` | 🟡 | Asked as "difference between them"; easy points | ✅ `13-scope-functions/` (2 files, below) |
| 14 | Extension functions and properties | 🟡 | Asked directly; also how many standard-library functions work | ✅ `14-extension-functions/` (3 files, below) |
| 15 | Exceptions: `try` as an expression, `require` / `check`, `Result`, `use`, common exceptions, `Nothing` | 🟡 | Edge-case handling in coding problems; `Nothing` / `Unit` / `Any` questions | ✅ `15-exceptions/` (4 files, below) |
| 16 | Kotlin vs Java and interop: platform types, `@JvmStatic` / `@JvmOverloads`, nullability across the boundary, Java statics, SAM, `java.time`, file class names | 🟡 | "Why Kotlin over Java?" opens many interviews; this repo is Java + Kotlin | ✅ `16-kotlin-vs-java-interop/` (5 files, below) |
| 17 | Coroutines and Flow: `launch` vs `async`, dispatchers, structured concurrency, `suspend`, cancellation, timeouts, `Flow`, `StateFlow` / `SharedFlow` / `Channel` | 🟡 | The biggest topic in Q&A banks (about 20 of 82 questions in one) but rarely used in algorithm rounds. **Promote to 🔴 if the job is Android, Ktor or reactive Spring** | ✅ `17-coroutines-and-flow/` (5 files, below) |
| 18 | Delegation and properties: `lazy`, `lateinit`, `by`, observable properties, property accessors (`field`, `private set`, `const`), class delegation `by` | ⚪ | Asked at mid level (`lateinit` vs `lazy`) | ✅ `18-delegation-and-properties/` (4 files, below) |
| 19 | Generics and variance: `in` / `out`, `where`, star projection, `reified` | ⚪ | Senior-level questions | ✅ `19-generic-type/` (`GenericBasics` · `Variance` · `ErasureAndReified`, package `review.generics`) |
| 20 | Advanced: `inline` / `noinline` / `crossinline`, `value class`, operator overloading, DSLs, `tailrec`, contracts, `typealias` | ⚪ | Senior-level questions | ✅ `20-advanced/` (6 files, below) |
| 21 | Printing objects as JSON: why `toString()` is not JSON, a hand-written `escapeJson` + recursive `toJson`, pretty printing, reflection, Jackson / Gson / `kotlinx.serialization` | 🟡 | "Print this result as JSON" is a common output task, and on a judge with no library you write it yourself (escaping, `null`, nesting, key order); Q&A asks how the three libraries differ | ✅ `21-json-output/` (4 files, below) |

## Suggested order

Live-coding first, then Q&A: **2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 9**, then **11 -> 13 -> 16 -> 17** if time remains.
Lessons 1 and 8 are short and can be skimmed in between. Every lesson is now written; the order above is the order to read them in.
Lesson 9 (drills) is the one to repeat against the clock. Lesson 21 (JSON output) stands alone: read `HandWrittenJson` first, the libraries last. Lesson 17 needs the `kotlinx-coroutines-core` dependency, already in `pom.xml`.

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
Files of lesson 1 (all in `review.variables`): `ValAndVar` · `Equality` · `StringTemplates`.
Files of lesson 2 (all in `review.loops`): `ForAndRanges` · `WhileAndControl` · `LoopPatterns`.
Files of lesson 3 (all in `review.lists`): `ArraysBasics` · `CollectionTypes` · `ListCreateAccess` · `ListMutate` · `ListSliceSortSearch`.
Files of lesson 4 (all in `review.iterable`): `MapAndFilter` · `FoldAndReduce` · `GroupAndAssociate` · `ZipAndWindow`.
Files of lesson 5 (all in `review.sets`): `SetBasics` · `MapBasics` · `CountingAndGrouping` · `StackQueueDeque`.
Files of lesson 6 (all in `review.strings`): `StringOperations` · `RegexBasics` · `StringBuilderAndChars` · `StringPatterns`.
Files of lesson 7 (all in `review.nullsafety`): `NullableTypes` · `SafeCallElvis` · `SmartCastsAndParsing`.
Files of lesson 8 (all in `review.control`): `IfExpression` · `WhenExpression` · `EarlyReturn`.
Files of lesson 9 (all in `review.drills`): `ArrayDrills` · `InPlaceAndSearchDrills` · `StringAndHashDrills` · `IntervalsAndGraphs` · `ClassicsAndApproach`.
Files of lesson 10 (all in `review.functions`): `ParametersAndReturn` · `Lambdas` · `HigherOrder`.
Files of lesson 11 (all in `review.classes`): `ClassBasics` · `InheritanceAndInterfaces` · `DataClass` · `ObjectAndCompanion` · `EnumAndSealed`.
Files of lesson 12 (all in `review.collectionsdepth`): `ReadOnlyVsMutable` · `SequenceVsIterable` · `CustomIterable`.
Files of lesson 13 (all in `review.scope`): `FiveFunctions` · `ChoosingScope`.
Files of lesson 14 (all in `review.extensions`): `ExtensionFunctions` · `ExtensionProperties` · `ExtensionsInPractice`.
Files of lesson 15 (all in `review.exceptions`): `TryCatch` · `CommonExceptions` · `RequireCheck` · `ResultAndNothing`.
Files of lesson 16 (all in `review.interop`): `KotlinVsJava` · `PlatformTypes` · `JavaFromKotlin` · `JvmAnnotations` · `FileFacade`.
Files of lesson 17 (all in `review.coroutines`): `CoroutineBasics` · `StructuredConcurrency` · `CancellationAndTimeouts` · `FlowBasics` · `StateSharedAndChannels`.
Files of lesson 18 (all in `review.delegation`): `Properties` · `LazyAndLateinit` · `PropertyDelegates` · `ClassDelegation`.
Files of lesson 19 (all in `review.generics`): `GenericBasics` · `Variance` · `ErasureAndReified`.
Files of lesson 20 (all in `review.advanced`): `InlineFunctions` · `ValueClass` · `OperatorOverloading` · `Tailrec` · `DslBuilders` · `Contracts`.
Files of lesson 21 (all in `review.json`): `ToStringIsNotJson` · `HandWrittenJson` · `ReflectionJson` · `LibraryJson`.
