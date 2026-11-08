package review

import java.io.File
import java.io.FileDescriptor
import java.io.FileOutputStream
import java.io.PrintStream

/**
 * Print helpers so every lesson reads as plain Kotlin and prints in the same layout.
 *
 * IN THE SOURCE (plain code, one line each, the comment is the note):
 *
 *     syntax("val name: Type = value" to "explicit type")       // a syntax template for the section
 *     val i = 42;  show { i }                                   // type inferred: Int
 *     show { 7 / 2 }                                            // Int / Int truncates toward zero
 *     wontCompile("val l: Long = i", "Int is never widened")    // code that is rejected
 *
 * IN THE OUTPUT (the code text and the comment are read back from the source file, so nothing is typed twice):
 *
 *     syntax  val name: Type = value           explicit type
 *     val i = 42                       → 42        Int      type inferred: Int
 *     7 / 2                            → 3         Int      Int / Int truncates toward zero
 *     ×  val l: Long = i               does NOT compile: Int is never widened
 *
 * Rules for a `show` line: one line only; `show { ... }` is the last statement on it; the note is the trailing `// comment`;
 * a declaration may come before it on the same line (`val i = 42; show { i }`) and is then printed as the code.
 *
 * Colors (ANSI) and box lines (UTF-8) look right in the IntelliJ Run window, Windows Terminal and Git Bash.
 * If a terminal shows garbage instead, switch to plain ASCII with either:
 *     mvn -q compile exec:java -Dstyle=plain        or        set the environment variable NO_COLOR=1
 */

private const val WIDTH = 92
private const val LABEL_WIDTH = 44
private const val VALUE_WIDTH = 22
private const val TYPE_WIDTH = 10
private const val SYNTAX_WIDTH = 54

private val plain: Boolean = System.getenv("NO_COLOR") != null || System.getProperty("style") == "plain"

/** Fancy mode writes UTF-8 bytes itself, so box lines survive any default console encoding. */
private val out: PrintStream =
    if (plain) System.out else PrintStream(FileOutputStream(FileDescriptor.out), true, Charsets.UTF_8)

// The escape character is built at runtime so the source file contains no raw control bytes.
private val ESC: String = 27.toChar() + "["

private const val BOLD_CYAN = "1;96"
private const val BOLD_WHITE = "1;97"
private const val BOLD_YELLOW = "1;93"
private const val BOLD_GREEN = "1;92"
private const val BOLD_RED = "1;91"
private const val MAGENTA = "95"
private const val YELLOW = "93"
private const val BLUE = "94"
private const val GRAY = "90"

private fun paint(code: String, text: String): String = if (plain) text else "$ESC${code}m$text${ESC}0m"

/** Big banner at the top of a file's output. */
internal fun title(text: String) {
    if (plain) {
        out.println("=".repeat(WIDTH))
        out.println("  $text")
        out.println("=".repeat(WIDTH))
        return
    }
    val inner = WIDTH - 2
    out.println(paint(BOLD_CYAN, "╭" + "─".repeat(inner) + "╮"))
    out.println(paint(BOLD_CYAN, "│") + paint(BOLD_WHITE, "  $text".padEnd(inner)) + paint(BOLD_CYAN, "│"))
    out.println(paint(BOLD_CYAN, "╰" + "─".repeat(inner) + "╯"))
}

/** Sub-heading inside a file's output. */
internal fun section(text: String) {
    out.println()
    if (plain) out.println("--- $text ".padEnd(WIDTH, '-'))
    else out.println(paint(BOLD_YELLOW, "── $text ".padEnd(WIDTH, '─')))
}

/** The syntax template(s) of a section: `"val name: Type = value" to "explicit type"`. */
internal fun syntax(vararg forms: Pair<String, String>) {
    forms.forEachIndexed { index, (form, meaning) ->
        val tag = if (index == 0) "syntax" else ""
        if (plain) {
            out.println("  ${tag.padEnd(8)}${form.padEnd(SYNTAX_WIDTH)} $meaning".trimEnd())
        } else {
            out.println("  " + paint(MAGENTA, tag.padEnd(8)) + paint(BOLD_GREEN, form.padEnd(SYNTAX_WIDTH)) + " " + paint(GRAY, meaning))
        }
    }
}

/**
 * One result line. Write it as `show { expression }  // note`, or `val x = 1; show { x }  // note`.
 * The code text and the note come from the line of the lesson's source file that called this function.
 */
internal fun show(block: () -> Any?) {
    val value = block()
    val frame = Throwable().stackTrace.getOrNull(1)
    val site = frame?.let { callSite(it) }
    printRow(site?.label ?: "show { ... }  (source not found: run from the kotlin-review folder)", value, site?.note ?: "")
}

/** Code that the compiler rejects, so it cannot be run: shown with the reason. */
internal fun wontCompile(code: String, why: String) {
    if (plain) {
        out.println("  XX  ${code.padEnd(LABEL_WIDTH - 4)} does NOT compile: $why")
    } else {
        out.println("  " + paint(BOLD_RED, "×") + "  " + paint(YELLOW, code.padEnd(LABEL_WIDTH - 3)) + " " + paint(BOLD_RED, "does NOT compile:") + " " + paint(GRAY, why))
    }
}

/** A plain remark, for things that cannot be printed (an exception, a rule, a tip). */
internal fun note(text: String) {
    if (plain) out.println("  >> $text")
    else out.println("  " + paint(BOLD_YELLOW, "!") + "  " + paint(YELLOW, text))
}

private fun printRow(label: String, value: Any?, note: String) {
    val type = value?.let { it::class.simpleName } ?: "null"
    if (plain) {
        val noteText = if (note.isEmpty()) "" else "# $note"
        out.println("  ${label.padEnd(LABEL_WIDTH)} = ${display(value).padEnd(VALUE_WIDTH)} ${type.padEnd(TYPE_WIDTH)} $noteText".trimEnd())
        return
    }
    val typeText = if (note.isEmpty()) type else type.padEnd(TYPE_WIDTH)
    val noteText = if (note.isEmpty()) "" else " " + paint(GRAY, note)
    out.println(
        "  " + label.padEnd(LABEL_WIDTH) + " " + paint(GRAY, "→") + " " +
            paint(valueColor(value), display(value).padEnd(VALUE_WIDTH)) + " " +
            paint(BLUE, typeText) + noteText
    )
}

private fun display(value: Any?): String = when (value) {
    is Char -> "'$value'"
    is String -> "\"$value\""
    else -> value.toString()
}

private fun valueColor(value: Any?): String = when (value) {
    true -> "1;92"            // green
    false -> "1;91"           // red
    is Char, is String -> "1;93"   // yellow
    null -> "1;95"            // magenta
    is Number -> "1;96"       // cyan
    else -> "1"
}

// ---------------------------------------------------------------------------------------------
// Reading the call site back from the lesson's source file
// ---------------------------------------------------------------------------------------------

private class CallSite(val label: String, val note: String)

private object Anchor

private val sourceCache = HashMap<String, List<String>?>()

private fun callSite(frame: StackTraceElement): CallSite? {
    val fileName = frame.fileName ?: return null
    val pkg = frame.className.substringBeforeLast('.', "")
    val key = "$pkg/$fileName"
    val lines = if (sourceCache.containsKey(key)) sourceCache[key] else findSource(fileName, pkg).also { sourceCache[key] = it }
    val text = lines?.getOrNull(frame.lineNumber - 1) ?: return null
    return parseShowLine(text)
}

/** Splits `   val i = 42;  show { i }   // note` into the code ("val i = 42") and the note ("note"). */
private fun parseShowLine(text: String): CallSite? {
    val open = Regex("""\bshow\s*\{""").find(text) ?: return null
    val before = text.substring(0, open.range.first).trim().trimEnd(';').trim()
    val bodyStart = open.range.last + 1
    var depth = 1
    var i = bodyStart
    var inString = false
    var inChar = false
    while (i < text.length && depth > 0) {
        val c = text[i]
        when {
            inString -> if (c == '\\') i++ else if (c == '"') inString = false
            inChar -> if (c == '\\') i++ else if (c == '\'') inChar = false
            c == '"' -> inString = true
            c == '\'' -> inChar = true
            c == '{' -> depth++
            c == '}' -> depth--
        }
        i++
    }
    if (depth != 0) return null
    val body = text.substring(bodyStart, i - 1).trim()
    val note = text.substring(i).substringAfter("//", "").trim()
    return CallSite(label = before.ifEmpty { body }, note = note)
}

private fun findSource(fileName: String, pkg: String): List<String>? {
    for (root in sourceRoots()) {
        val hit = root.walkTopDown().firstOrNull { it.isFile && it.name == fileName && declaresPackage(it, pkg) }
        if (hit != null) return hit.readLines()
    }
    return null
}

private fun declaresPackage(file: File, pkg: String): Boolean =
    file.useLines { lines -> lines.firstOrNull { it.startsWith("package ") }?.removePrefix("package ")?.trim() == pkg }

/** Candidate `src/main/kotlin` folders: from the working folder and from where the compiled classes live. */
private fun sourceRoots(): List<File> {
    val starts = mutableListOf(File("").absoluteFile)
    runCatching {
        var dir: File? = File(Anchor::class.java.protectionDomain.codeSource.location.toURI())
        repeat(5) {
            dir?.let { starts += it }
            dir = dir?.parentFile
        }
    }
    return starts
        .flatMap { listOf(File(it, "src/main/kotlin"), File(it, "kotlin-review/src/main/kotlin")) }
        .filter { it.isDirectory }
        .distinct()
}
