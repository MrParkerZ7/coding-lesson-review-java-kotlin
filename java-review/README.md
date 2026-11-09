# java-review

Runnable notes for reviewing Java syntax and features. The Java twin of [`kotlin-review`](../kotlin-review/README.md): same idea, same layout,
same output, written in Java 21 and generated in one pass on 2026-11-09 (the interview day).
Each lesson is a folder of small files, each with a `main`: read the code, run it, compare the output with the `// comments`.

## My story (why this exists)

- I have a **coding interview on 2026-11-09**. The interview language is not confirmed, so I keep **both** reviews: Kotlin in `kotlin-review`, Java here.
- First priority: **lists, loops and collections/streams syntax**. After that, whatever the plan below says matters most.
- This module sits in `review-lesson-java-kotlin` next to the Spring Boot calculator API and the Kotlin module, so I can compare the two languages side by side.

## Folder convention

```
src/main/java/review/
  Output.java                 <- shared print helpers: title() section() syntax() bigO() show() wontCompile() note() block() thrown()
  0-primitive-data-type/      <- lesson folder: <number>-<kebab-case-name>
    IntegerTypes.java         <- one topic per file, each class with its own main
    ...
```

**Lesson files are plain Java.** A demo is one real line of code with a comment, and the output echoes that line:

```java
syntax("var name = value;", "type inferred");                // prints the syntax template of the section (pairs: form, meaning)
int i = 42;  show(() -> i);                                   // 32 bit: the DEFAULT type for whole numbers
show(() -> 7 / 2);                                            // int / int truncates toward zero
show(() -> thrown(() -> 7 / (i - 42)));                       // run it, print the exception class: ArithmeticException
wontCompile("int big = 3_000_000_000;", "the literal does not fit in an int");   // code the compiler rejects (not run)
```

```
syntax  var name = value;                type inferred
int i = 42                    -> 42       Integer  32 bit: the DEFAULT type for whole numbers
7 / 2                         -> 3        Integer  int / int truncates toward zero
x  int big = 3_000_000_000;   does NOT compile: the literal does not fit in an int
```

The code text and the `// comment` are read back from the source file when the program runs, so nothing is written twice.
Keep each `show(...)` on one line, with the comment after it. A lambda can only use variables that are never reassigned
(effectively final): compute a final value on its own line first, then show it. The type column is the run-time class of the
value, so an `int` prints as `Integer`.
Output is colored (numbers cyan, `true` green, `false` red, chars and strings yellow, types blue, notes gray) with box lines.
It looks right in the IntelliJ Run window, Windows Terminal and Git Bash. If a terminal shows garbage instead,
use plain ASCII: `mvn -q compile exec:java -Dstyle=plain` (or set the environment variable `NO_COLOR=1`).

- Lessons are numbered from **0**, matching the plan below.
- A Java package cannot start with a digit or contain hyphens, so the package is the lesson name without the number:
  `0-primitive-data-type` -> `package review.primitive`. Folder and package differ on purpose; IntelliJ may show a weak
  "package does not match directory" hint, which can be ignored (Maven compiles it).
- Run class name = package + class name: `IntegerTypes.java` in `review.primitive` -> `review.primitive.IntegerTypes`.
- Never write a backslash followed by `u` in a Java source file, not even in a comment: javac reads it as a unicode escape.

## Priority marks

| Mark | Meaning |
|---|---|
| 🔴 **Must** | Used in almost every live-coding problem, or asked in almost every Java Q&A round. Review before the interview. |
| 🟡 **Should** | Often asked or often useful. Review if time allows. |
| ⚪ **Nice** | Senior-level or niche. Skim only if the job description mentions it. |

Two kinds of rounds, and they need different lessons: a **live-coding round** (solve a problem in Java: lessons 0-9) and a
**Java Q&A round** (explain features and traps: lessons 10-18, with lesson 12 the one almost every interviewer asks).

## Lesson plan

| # | Lesson | Priority | Why it matters for the interview | Status |
|---|---|---|---|---|
| 0 | Primitive and basic types: `byte` `short` `int` `long` `float` `double` `boolean` `char`, overflow, integer division, casts, unsigned helpers, `BigInteger` / `BigDecimal`, the `String` type, wrapper types | 🔴 | `int` overflow, `/` vs `%`, `0.1 + 0.2`, `char` arithmetic, the `Integer` cache and `==`, `BigDecimal` for money decide many live-coding answers | ✅ `0-primitive-data-type/` (9 files, below) |
| 1 | Variables and types: `var`, `final`, effectively final, `==` vs `equals`, text blocks, formatting | 🔴 | `==` vs `equals` and `final` are asked directly; used on every line of code | ✅ `1-variables-and-types/` (5 files, below) |
| 2 | Loops: `for`, enhanced `for`, ranges with `IntStream`, `while`, labels, `break` / `continue`, loop patterns (two pointers, sliding window, prefix sums, binary search), Big-O | 🔴 | Every live-coding problem; the modify-while-looping trap | ✅ `2-loops/` (5 files, below) |
| 3 | Arrays and lists: arrays, `Arrays`, `ArrayList`, `List.of`, `Arrays.asList`, create, access, mutate, sublist, sort, search, copy vs alias, 2D, Big-O | 🔴 | The most common input type in coding problems | ✅ `3-arrays-and-lists/` (6 files, below) |
| 4 | Streams: `map` `filter` `flatMap` `reduce` `collect` `groupingBy` `partitioningBy` `toMap` `IntStream` | 🔴 | Idiomatic Java solutions; a single-use stream and laziness are classic traps | ✅ `4-streams/` (5 files, below) |
| 5 | Sets, maps, stack, queue, deque and heap: `HashSet` `TreeSet` `HashMap` `merge` `computeIfAbsent` `ArrayDeque` `PriorityQueue`, Big-O | 🔴 | Two Sum, anagrams, duplicates, balanced brackets, top-K: all rely on them | ✅ `5-sets-and-maps/` (7 files, below) |
| 6 | Strings in problems: `split`, regex, `StringBuilder`, char tricks, palindrome / anagram patterns | 🔴 | Palindrome, anagram, parsing problems | ✅ `6-strings-in-problems/` (4 files, below) |
| 7 | Null handling and `Optional`: `Objects.requireNonNull`, null in collections, `Optional`, safe parsing | 🔴 | Java has no null-safe types: every Q&A round asks how you defend against `NullPointerException` | ✅ `7-null-and-optional/` (4 files, below) |
| 8 | Control flow: `if`, ternary, `switch` expressions, pattern matching, early return | 🔴 | FizzBuzz-style problems; modern `switch` is a standard "what is new" question | ✅ `8-control-flow/` (5 files, below) |
| 9 | Interview drills: array, string / hash, interval / grid / DP and classic problems, plus the interview routine | 🔴 | Practice of lessons 0-8 under interview conditions | ✅ `9-interview-drills/` (5 files, below) |
| 10 | Methods and lambdas: overloading, varargs, functional interfaces, lambdas, method references, closures | 🟡 | Lambdas and `Function` / `Predicate` / `Supplier` are standard Q&A | ✅ `10-methods-and-lambdas/` (5 files, below) |
| 11 | Classes, records, enums, sealed types, nested classes | 🟡 | The most common "explain this Java feature" topics; also used in design-style coding tasks | ✅ `11-classes-records-enums/` (7 files, below) |
| 12 | The object contract: `equals` / `hashCode`, immutability, pass by value, static and init order | 🔴 | Asked in nearly every Java Q&A round: a class without `hashCode` is lost in a `HashSet` | ✅ `12-object-contract/` (6 files, below) |
| 13 | Collections in depth: unmodifiable vs immutable, iterators, `Comparable` / `Comparator`, stream laziness | 🟡 | `Collections.unmodifiableList` vs `List.of` and the `Comparator` overflow trap are direct questions | ✅ `13-collections-in-depth/` (4 files, below) |
| 14 | Generics and wildcards: bounds, `? extends` / `? super` (PECS), erasure | 🟡 | Asked often in Java Q&A: "why is `List<Object>` not a `List<String>`?" | ✅ `14-generics/` (3 files, below) |
| 15 | Exceptions: checked vs unchecked, `try` / `catch` / `finally`, try-with-resources, custom exceptions, traps | 🟡 | Checked vs unchecked and `finally` behaviour are standard Q&A | ✅ `15-exceptions/` (5 files, below) |
| 16 | Concurrency: threads, `synchronized`, executors, `CompletableFuture`, concurrent collections, virtual threads | 🟡 | The biggest Java Q&A topic for backend jobs. **Promote to 🔴 if the job is backend (Spring)** | ✅ `16-concurrency/` (7 files, below) |
| 17 | Printing objects as JSON: why `toString()` is not JSON, a hand-written `escapeJson` + recursive `toJson`, reflection, Jackson / Gson | 🟡 | "Print this result as JSON" is a common output task, and on a judge with no library you write it yourself | ✅ `17-json-output/` (4 files, below) |
| 18 | Modern Java: what changed in Java 8, 11, 17 and 21 | 🟡 | "What is new in Java 17 / 21?" is a classic opener | ✅ `18-modern-java/` (4 files, below) |

## Suggested order

Live-coding first, then Q&A: **2 -> 3 -> 4 -> 5 -> 6 -> 7 -> 9**, then **12 -> 10 -> 15 -> 16** if time remains.
Lessons 0, 1 and 8 are short and can be skimmed in between. Lesson 9 (drills) is the one to repeat against the clock.
Lesson 17 stands alone: read `HandWrittenJson` first, the libraries last.

## How the priorities were decided

The marks are my judgment, not an official ranking. They start from the evidence collected for the Kotlin plan
(see [`kotlin-review/README.md`](../kotlin-review/README.md) § How the priorities were decided: live-coding problems use lists, loops and maps;
Q&A banks ask about null handling, collections, `equals`, generics and concurrency), then swap the Kotlin-only topics (null-safe types, scope functions,
extension functions, coroutines) for their Java counterparts (`Optional`, the object contract, generics wildcards, `CompletableFuture`).
No new online research was done for this module, so check the job description and re-rank.

## Run

IntelliJ: right-click `java-review/pom.xml` -> **Add as Maven Project** (once), then click the green arrow beside `main`.

Command line (JDK 21 + Maven):

```
cd java-review
mvn -q compile exec:java                                                   # 0-primitive-data-type / IntegerTypes (default)
mvn -q compile exec:java -Dmain.class=review.primitive.DecimalTypes        # any other file: <package>.<ClassName>
```

Files of lesson 0 (all in `review.primitive`): `IntegerTypes` · `DecimalTypes` · `BooleanType` · `CharType` · `TypeConversions` · `UnsignedTypes` · `BigNumbers` · `StringType` · `WrapperTypes`.
Files of lesson 1 (all in `review.variables`): `VarAndFinal` · `ScopeAndDefaults` · `Equality` · `ComparingValues` · `TextAndFormatting`.
Files of lesson 2 (all in `review.loops`): `ForAndRanges` · `WhileAndControl` · `ForEachAndIterator` · `LoopPatterns` · `SlidingWindowAndSearch`.
Files of lesson 3 (all in `review.lists`): `ArraysBasics` · `ArraysUtilities` · `CollectionTypes` · `ListCreateAccess` · `ListMutate` · `ListSliceSortSearch`.
Files of lesson 4 (all in `review.streams`): `MapAndFilter` · `CollectAndReduce` · `GroupAndPartition` · `ZipAndWindow` · `StreamTraps`.
Files of lesson 5 (all in `review.sets`): `SetBasics` · `MapBasics` · `MapKinds` · `TreeNavigation` · `CountingAndGrouping` · `StackQueueDeque` · `HeapBasics`.
Files of lesson 6 (all in `review.strings`): `StringOperations` · `RegexBasics` · `StringBuilderAndChars` · `StringPatterns`.
Files of lesson 7 (all in `review.nullsafety`): `NullBasics` · `OptionalBasics` · `OptionalTraps` · `ParsingSafely`.
Files of lesson 8 (all in `review.control`): `IfAndTernary` · `SwitchExpression` · `PatternMatching` · `EarlyReturn` · `ClassicsWithControlFlow`.
Files of lesson 9 (all in `review.drills`): `ArrayDrills` · `InPlaceAndSearchDrills` · `StringAndHashDrills` · `IntervalsAndGraphs` · `ClassicsAndApproach`.
Files of lesson 10 (all in `review.methods`): `MethodsAndOverloading` · `ParametersAndReturn` · `LambdasAndFunctionalInterfaces` · `CaptureAndThis` · `MethodReferences`.
Files of lesson 11 (all in `review.classes`): `ClassBasics` · `Inheritance` · `Interfaces` · `Records` · `Enums` · `SealedAndPatterns` · `NestedClasses`. Helper types without `main`: `Vault`.
Files of lesson 12 (all in `review.objects`): `EqualsAndHashCode` · `EqualityTraps` · `ImmutabilityAndFinal` · `PassByValue` · `StaticAndInit` · `SingletonsAndIdentity`.
Files of lesson 13 (all in `review.collectionsdepth`): `UnmodifiableVsImmutable` · `IteratorsAndIterable` · `ComparableAndComparator` · `StreamLaziness`.
Files of lesson 14 (all in `review.generics`): `GenericBasics` · `WildcardsPECS` · `ErasureAndLimits`.
Files of lesson 15 (all in `review.exceptions`): `CheckedVsUnchecked` · `CommonExceptions` · `TryCatchFinally` · `TryWithResources` · `ExceptionTraps`. Helper types without `main`: `InsufficientFundsException`.
Files of lesson 16 (all in `review.concurrency`): `ThreadsBasics` · `SynchronizedAndLocks` · `AtomicsAndVolatile` · `ExecutorsAndFutures` · `ConcurrentCollections` · `QueuesAndDeadlock` · `VirtualThreads`. Helper types without `main`: `Workers`.
Files of lesson 17 (all in `review.json`): `ToStringIsNotJson` · `HandWrittenJson` · `ReflectionJson` · `LibraryJson`. Helper types without `main`: `Address` · `User`.
Files of lesson 18 (all in `review.modern`): `Java8to11` · `JavaTime` · `Java14to17` · `Java21`.
