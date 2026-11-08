---
project: develop
---
# review-lesson-java-kotlin

> Java and Kotlin review lessons for coding-interview practice, one small lesson at a time.

## What it is

A personal practice repo for reviewing Java and Kotlin syntax and features before coding interviews.
Lessons are written one at a time, on the owner's mark, never in bulk.

## Where it fits

| Part | What |
|---|---|
| [`srping-boot-api-calculator/`](srping-boot-api-calculator/) | the Java side: a Spring Boot calculator API |
| [`kotlin-review/`](kotlin-review/README.md) | the Kotlin side: the lessons, the lesson plan with priority marks, and how to run them |

## Working with it

Run a Kotlin lesson (JDK 21 + Maven): `cd kotlin-review`, then
`mvn -q compile exec:java -Dmain.class=review.primitive.CharTypeKt`.
How lessons are written in this repo: [`docs/_skill/RULES.md`](docs/_skill/RULES.md).
