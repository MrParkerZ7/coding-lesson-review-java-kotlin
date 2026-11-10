package review.threads

import kotlin.concurrent.thread

/*
 * Small helpers shared by the files of lesson 22 (this file has no main): run a body on several threads and wait for them.
 */

/** Starts `threads` threads that each run `body`, then waits until every one of them has finished (join also makes their writes visible to the caller). */
fun runAll(threads: Int, body: () -> Unit) {
    val started = List(threads) { thread { body() } }
    started.forEach { it.join() }
}

/** Starts `threads` threads that each call `operation` `perThread` times, then waits for all of them. */
fun hammer(threads: Int, perThread: Int, operation: () -> Unit) = runAll(threads) { repeat(perThread) { operation() } }

/** The simple class name of the exception that `block` throws, or null when it throws nothing (so show can print either). */
fun thrown(block: () -> Any?): String? = runCatching(block).exceptionOrNull()?.javaClass?.simpleName
