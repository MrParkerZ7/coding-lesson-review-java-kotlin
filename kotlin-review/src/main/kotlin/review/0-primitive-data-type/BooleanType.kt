package review.primitive

import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

/**
 * 0c. BOOLEAN
 *     Run: mvn -q compile exec:java -Dmain.class=review.primitive.BooleanTypeKt
 *     Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("BOOLEAN")

    section("Logic operators")
    syntax(
        "a && b | a || b | !a | a xor b" to "AND | OR | NOT | XOR",
        "val flag: Boolean = true" to "only two values: true and false",
    )
    show { true && false }                                    // AND
    show { true || false }                                    // OR
    show { !true }                                            // NOT
    show { true xor false }                                   // XOR: true when exactly one side is true

    section("Short-circuit: && and || stop early")
    val text: String? = null
    show { text != null && text.length > 3 }                  // right side is skipped when text is null: no crash

    section("Comparisons return Boolean")
    syntax("a > b | a >= b | a < b | a <= b | a == b | a != b" to "every comparison is a Boolean")
    show { 5 > 3 }
    show { 5 == 5 }
    show { 5 != 5 }
    show { 'a' < 'b' }                                        // chars compare by their code
    wontCompile("if (1) { }", "there is no truthy/falsy: the condition must be a Boolean")
}
