package review.advanced

import review.note
import review.section
import review.show
import review.syntax
import review.title
import review.wontCompile

@DslMarker
annotation class HtmlDsl                                   // classes marked with it cannot reach each other's members implicitly

@HtmlDsl
class Li { var text = "" }

@HtmlDsl
class Ul {
    val items = mutableListOf<String>()
    fun li(init: Li.() -> Unit) { items.add(Li().apply(init).text) }
    operator fun String.unaryPlus() { items.add(this) }    // +"cake" adds an item
}

@HtmlDsl
class Page {
    var title = ""
    private var list: Ul? = null
    fun ul(init: Ul.() -> Unit) { list = Ul().apply(init) }
    fun render(): String = buildString {
        append("<h1>").append(title).append("</h1>")
        list?.let { u ->
            append("<ul>")
            u.items.forEach { append("<li>").append(it).append("</li>") }
            append("</ul>")
        }
    }
}

/** The entry point of the DSL: the lambda has a Page as its receiver. */
fun page(init: Page.() -> Unit): Page = Page().apply(init)

/** The same shape WITHOUT @DslMarker, to see what the marker prevents. */
class LooseOuter {
    val log = mutableListOf<String>()
    fun mark() { log.add("outer.mark") }
    fun inner(init: LooseInner.() -> Unit) { LooseInner().init() }
}
class LooseInner

fun build(init: StringBuilder.() -> Unit): String = StringBuilder().apply(init).toString()

/**
 * 20e. LAMBDA WITH RECEIVER, BUILDER DSL, @DslMarker
 *      Run: mvn -q compile exec:java -Dmain.class=review.advanced.DslBuildersKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("DSL BUILDERS  -  T.() -> Unit, @DslMarker")

    section("A lambda with receiver: inside it, `this` is a T")
    syntax(
        "block: T.() -> Unit" to "a function type WITH receiver: the lambda body runs as if inside T",
        "block: (T) -> Unit" to "the same shape, but the object arrives as `it`",
        "T().apply(block)  |  block(t)  |  t.block()" to "three ways to call it; the receiver is the first argument",
    )
    val withReceiver: StringBuilder.() -> Unit = { append("a"); append("b") }   // no `it`, no `sb.`
    val withParam: (StringBuilder) -> Unit = { it.append("c") }
    val sb = StringBuilder()
    sb.withReceiver()
    withParam(sb)
    show { sb.toString() }                                 // both styles wrote into the same builder
    show { build { append(1); append("x") } }              // our own mini builder: a function that takes T.() -> Unit
    show { buildString { append("built"); append('!') } }  // the standard library's builder, same idea
    show { StringBuilder().apply { append("hi") }.toString() }   // apply takes T.() -> Unit and returns the receiver itself

    section("A tiny HTML-like DSL")
    syntax("fun page(init: Page.() -> Unit): Page" to "each nested { } is a lambda with the receiver of that level")
    val menu = page {
        title = "Menu"                                     // this.title: Page
        ul {
            li { text = "tea" }                            // this.text: Li
            +"cake"                                        // Ul.unaryPlus
        }
    }
    show { menu.render() }                                 // the nesting in the source became the nesting in the output
    note("This is how kotlinx.html, Gradle Kotlin scripts and Ktor routing { } are built")

    section("@DslMarker: stop the wrong nesting")
    val outer = LooseOuter()
    with(outer) { inner { mark() } }                       // mark() belongs to the OUTER receiver; Kotlin silently allows it
    show { outer.log }                                     // without @DslMarker an inner block can call outer members by accident
    wontCompile("page { ul { li { ul { } } } }", "a ul inside an li is rejected: @HtmlDsl hides the outer receivers")
    wontCompile("page { ul { title = \"x\" } }", "title belongs to the outer Page, hidden inside ul by @HtmlDsl")
    note("To reach an outer receiver on purpose, name it: this@page.title = \"x\"")
    note("Inline lambdas with receiver are why apply, with, run, buildString and buildList read like part of the language")
}
