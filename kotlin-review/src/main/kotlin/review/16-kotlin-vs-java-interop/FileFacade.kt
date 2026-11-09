@file:JvmName("FacadeDemo")

package review.interop

import review.note
import review.section
import review.show
import review.syntax
import review.title

/** A top-level function: it has no class in the Kotlin source, but the JVM needs one. */
fun facadeHello(): String = "hello from the file class"

/**
 * 16e. WHERE TOP-LEVEL FUNCTIONS GO ON THE JVM: the FileNameKt class and @file:JvmName
 *      Run: mvn -q compile exec:java -Dmain.class=review.interop.FacadeDemo
 *      (this file renames its own class, so it is FacadeDemo, not FileFacadeKt)
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("FILE CLASSES  -  how Java calls a Kotlin top-level function")

    section("The default name: file name + Kt")
    syntax(
        "File Tools.kt with fun hello()" to "Java: ToolsKt.hello()",
        "@file:JvmName(\"Tools\") as the first line" to "Java: Tools.hello()   (the annotation goes before `package`)",
    )
    show { Class.forName("review.interop.KotlinVsJavaKt").simpleName }   // file KotlinVsJava.kt -> class KotlinVsJavaKt
    show { Class.forName("review.interop.PlatformTypesKt").simpleName }
    show { isStaticMethod(Class.forName("review.interop.PlatformTypesKt"), "unsafeEnvLength") }   // a top-level function is a static method

    section("A renamed file class")
    show { Class.forName("review.interop.FacadeDemo").simpleName }    // this file uses @file:JvmName("FacadeDemo")
    show { runCatching { Class.forName("review.interop.FileFacadeKt") }.exceptionOrNull()?.javaClass?.simpleName }   // the default name is gone
    show { Class.forName("review.interop.FacadeDemo").getMethod("facadeHello").invoke(null) }   // static: call it with no instance
    note("main() is a top-level function too, so it lives in the file class: that is why exec:java needs the ...Kt name")
    note("Several files with the same @file:JvmName in one package clash; use @file:JvmMultifileClass to merge them")
}
