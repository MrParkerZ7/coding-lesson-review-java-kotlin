---
project: develop
---
# review-lesson-java-kotlin

> Java, Kotlin and TypeScript review lessons for coding-interview practice, one small lesson at a time.

## What it is

A personal practice repo for reviewing Java, Kotlin and TypeScript syntax and features before coding interviews.
Lessons are written on the owner's mark: one at a time by default, the full set when the owner asks for it.

## Where it fits

| Part | What |
|---|---|
| [`srping-boot-api-calculator/`](srping-boot-api-calculator/) | the Java side: a Spring Boot calculator API |
| [`kotlin-review/`](kotlin-review/README.md) | the Kotlin lessons: the lesson plan with priority marks, and how to run them |
| [`java-review/`](java-review/README.md) | the same review in Java (JDK 21), for when the interview language is Java |
| [`typescript-review/`](typescript-review/README.md) | the same review in TypeScript 5.9 on Node 24, for when the interview language is TypeScript or JavaScript |

## Working with it

Run a lesson (JDK 21 + Maven):
- Kotlin: `cd kotlin-review`, then `mvn -q compile exec:java -Dmain.class=review.primitive.CharTypeKt`.
- Java: `cd java-review`, then `mvn -q compile exec:java -Dmain.class=review.primitive.CharType`.
- TypeScript (Node 24): `cd typescript-review`, `npm install` once, then `npx tsx src/0-primitive-types/numbers.ts`.

How lessons are written in this repo: [`docs/_skill/RULES.md`](docs/_skill/RULES.md).
