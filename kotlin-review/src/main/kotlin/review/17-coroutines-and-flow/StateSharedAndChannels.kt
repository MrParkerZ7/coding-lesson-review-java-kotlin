package review.coroutines

import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.coroutines.yield
import review.note
import review.section
import review.show
import review.syntax
import review.title

/**
 * 17e. StateFlow, SharedFlow and Channel: hot streams and hand-offs
 *      Run: mvn -q compile exec:java -Dmain.class=review.coroutines.StateSharedAndChannelsKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("StateFlow, SharedFlow, Channel  -  hot streams")

    section("Which one?")
    syntax(
        "Flow  (cold)" to "each collector starts its own run of the builder",
        "StateFlow  (hot)" to "ONE current value; a new collector gets it at once; equal values are dropped",
        "SharedFlow  (hot)" to "events to all current collectors; replay = N keeps the last N for late ones",
        "Channel" to "a queue between coroutines; each value goes to exactly ONE receiver",
    )

    section("StateFlow: a value holder you can observe")
    syntax(
        "val state = MutableStateFlow(initial)" to "must have an initial value",
        "state.value  |  state.value = x" to "read and write the current value from anywhere",
        "state.update { old -> new }" to "atomic read-modify-write",
    )
    val counter = MutableStateFlow(0)
    show { counter.value }                                 // always has a value, even before anyone collects
    counter.value = 5
    show { counter.value }                                 // the write above
    counter.update { it + 1 }
    show { counter.value }                                 // update read 5 and wrote 6
    val mode = MutableStateFlow(0)
    val seenModes = mutableListOf<Int>()
    runBlocking {
        val collector = launch(start = CoroutineStart.UNDISPATCHED) { mode.collect { seenModes.add(it) } }
        mode.value = 1; yield()
        mode.value = 1; yield()                            // same value again: StateFlow ignores it
        mode.value = 2; yield()
        collector.cancel()
    }
    show { seenModes }                                     // starts with the current value 0; the duplicate 1 is missing
    show { runBlocking { mode.first() } }                  // first() on a StateFlow returns the current value at once

    section("SharedFlow: events and replay")
    syntax(
        "MutableSharedFlow<T>(replay = n)" to "no initial value; replay = the last n values go to late collectors",
        "shared.emit(x)  |  tryEmit(x)" to "suspending send | non-suspending send",
        "shared.replayCache" to "the values a new collector would get",
    )
    val replaying = MutableSharedFlow<Int>(replay = 2)
    runBlocking { for (i in 1..4) replaying.emit(i) }
    show { replaying.replayCache }                         // only the last 2 are kept
    show { runBlocking { replaying.take(2).toList() } }    // a LATE collector still receives them
    val noReplay = MutableSharedFlow<Int>()
    runBlocking { noReplay.emit(1) }                       // nobody is listening and nothing is kept: the event is lost
    show { runBlocking { withTimeoutOrNull(100) { noReplay.first() } } }   // TRAP: a late collector waits for the next event (here: gives up)
    val live = MutableSharedFlow<String>()
    val got = mutableListOf<String>()
    runBlocking {
        val collector = launch(start = CoroutineStart.UNDISPATCHED) { live.collect { got.add(it) } }
        live.emit("a")
        live.emit("b")
        collector.cancel()
    }
    show { got }                                           // a collector that was already waiting gets every event

    section("Channel: send and receive between coroutines")
    syntax(
        "val ch = Channel<T>(capacity)" to "0 = rendezvous (default), n = buffer of n, Channel.UNLIMITED",
        "ch.send(x)  |  ch.receive()" to "suspend while the channel is full | empty",
        "ch.trySend(x)  |  ch.tryReceive()" to "never suspend: report success or failure",
        "ch.close()  and  for (x in ch)" to "close ends the loop after the last value",
    )
    val channel = Channel<Int>()
    val received = mutableListOf<Int>()
    runBlocking {
        launch { for (i in 1..3) channel.send(i); channel.close() }   // send suspends until the receiver is ready
        for (x in channel) received.add(x)
    }
    show { received }
    val small = Channel<Int>(2)
    show { small.trySend(1).isSuccess }
    show { small.trySend(2).isSuccess }
    show { small.trySend(3).isSuccess }                    // TRAP: capacity 2 and nobody receiving: the third one does not fit
    show { small.tryReceive().getOrNull() }                // values come out in the order they went in
    note("Two receivers on one Channel SPLIT the values between them; two collectors of a SharedFlow each get ALL of them")
    note("callbackFlow { trySend(x); awaitClose { unregister() } } turns a callback API into a Flow; stateIn / shareIn turn a cold Flow hot")
}
