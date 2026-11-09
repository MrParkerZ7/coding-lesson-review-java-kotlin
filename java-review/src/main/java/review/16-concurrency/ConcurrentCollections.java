package review.concurrency;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.LongAdder;

/**
 * 16e. CONCURRENT COLLECTIONS: ConcurrentHashMap, CopyOnWriteArrayList, LongAdder, CountDownLatch
 *      Run: mvn -q compile exec:java -Dmain.class=review.concurrency.ConcurrentCollections
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ConcurrentCollections {

    public static void main(String[] args) throws InterruptedException {
        title("CONCURRENT COLLECTIONS  -  safe sharing without writing locks yourself");

        section("ConcurrentHashMap");
        syntax(
                "new ConcurrentHashMap<K, V>()", "a thread-safe map; HashMap is NOT (lost updates, even corrupted buckets)",
                "map.merge(key, 1, Integer::sum)", "ATOMIC read-modify-write: the safe counter idiom",
                "map.compute(k, (k, v) -> ...) | computeIfAbsent | putIfAbsent", "atomic per key; never do get-then-put yourself"
        );
        ConcurrentHashMap<String, Integer> counts = new ConcurrentHashMap<>();
        Workers.runAll(4, () -> {
            for (int i = 0; i < 1000; i++) counts.merge("hits", 1, Integer::sum);
        });
        show(() -> counts);                                                // {hits=4000}: 4 threads x 1000, nothing lost
        show(() -> counts.compute("hits", (k, v) -> v == null ? 1 : v + 1));
        show(() -> counts.putIfAbsent("hits", 0));                         // returns the existing value and changes nothing
        show(() -> thrown(() -> counts.put("k", null)));                   // no null keys or values (null would mean 'absent')
        ConcurrentHashMap<String, Integer> weak = new ConcurrentHashMap<>(Map.of("a", 1, "b", 2));
        show(() -> thrown(() -> { for (String k : weak.keySet()) weak.remove(k); return 0; }));   // no ConcurrentModificationException
        show(() -> weak.size());                                           // iteration is 'weakly consistent': never fails, may or may not see changes
        note("Each ConcurrentHashMap call is atomic, a SEQUENCE of calls is not: if (!map.containsKey(k)) map.put(k, v) is still a race");

        section("CopyOnWriteArrayList: iterate a snapshot");
        syntax("new CopyOnWriteArrayList<>(list)", "every write copies the whole array: great for read-mostly lists (listeners), bad for many writes");
        List<Integer> plain = new ArrayList<>(List.of(1, 2, 3));
        show(() -> thrown(() -> { for (int n : plain) plain.add(n + 10); return 0; }));   // ArrayList: adding while iterating fails
        CopyOnWriteArrayList<Integer> cow = new CopyOnWriteArrayList<>(List.of(1, 2, 3));
        for (int n : cow) cow.add(n + 10);                                 // the loop walks the OLD snapshot, so it ends after 3 rounds
        show(() -> cow);

        section("LongAdder and AtomicLong");
        syntax("LongAdder adder = new LongAdder()", "a counter split in cells: faster than AtomicLong when many threads add; read with sum()");
        LongAdder adder = new LongAdder();
        Workers.runAll(4, () -> {
            for (int i = 0; i < 1000; i++) adder.increment();
        });
        show(() -> adder.sum());                                           // 4000
        note("LongAdder has no compareAndSet: use AtomicLong when you need a conditional update, LongAdder for pure statistics");

        section("CountDownLatch: wait for N things");
        syntax(
                "new CountDownLatch(n)", "starts at n",
                "latch.countDown() | latch.await()", "each worker counts down once | the waiter blocks until the count is 0"
        );
        CountDownLatch latch = new CountDownLatch(3);
        ConcurrentLinkedQueue<Integer> finished = new ConcurrentLinkedQueue<>();
        for (int id = 1; id <= 3; id++) {
            int worker = id;
            new Thread(() -> {
                finished.add(worker);
                latch.countDown();
            }).start();
        }
        latch.await();
        show(() -> finished.size());                                       // 3: await returned only after all three counted down
        show(() -> latch.getCount());
        note("A latch is single-use: the count never goes back up. Use CyclicBarrier to wait again and again");
        note("Queues, producer / consumer and deadlock: QueuesAndDeadlock");
    }
}
