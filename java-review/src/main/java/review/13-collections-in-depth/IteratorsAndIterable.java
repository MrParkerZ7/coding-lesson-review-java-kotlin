package review.collectionsdepth;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.stream.StreamSupport;

/**
 * 13b. ITERATORS and ITERABLE: for-each under the hood, fail-fast, ListIterator, your own Iterable
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.IteratorsAndIterable
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class IteratorsAndIterable {

    /** A class you can loop over with for-each, and that gets forEach and spliterator for free. */
    static class Countdown implements Iterable<Integer> {
        private final int from;

        Countdown(int from) { this.from = from; }

        @Override
        public Iterator<Integer> iterator() {
            return new Iterator<>() {
                private int current = from;

                @Override public boolean hasNext() { return current > 0; }

                @Override public Integer next() {
                    if (!hasNext()) throw new NoSuchElementException();
                    return current--;
                }
            };
        }
    }

    /** The loop everyone writes once: remove while looping with for-each. */
    static List<Integer> removeInForEach(List<Integer> list, int target) {
        for (Integer x : list) {
            if (x == target) list.remove(x);                         // remove(Object): changes the list under the iterator
        }
        return list;
    }

    static Object removeNow(Iterator<?> iterator) {              // remove() returns void, so it needs a wrapper to sit inside thrown(...)
        iterator.remove();
        return null;
    }

    public static void main(String[] args) {
        title("ITERATORS AND ITERABLE  -  what for-each really calls");

        section("Iterator and Iterable");
        syntax(
                "Iterator<T>", "hasNext() asks, next() hands the element out and moves on, remove() deletes the last one returned (optional)",
                "Iterable<T>", "one method, iterator(): this is all that for (T x : obj) needs. Collections are Iterables",
                "for (T x : iterable) { }", "the compiler rewrites it to: Iterator<T> it = iterable.iterator(); while (it.hasNext()) { T x = it.next(); ... }",
                "default forEach(Consumer) | spliterator()", "every Iterable gets these; the default spliterator just wraps iterator() (unknown size, poor splitting)"
        );
        List<Integer> seen = new ArrayList<>();
        for (int x : new Countdown(3)) seen.add(x);
        show(() -> seen);                                            // [3, 2, 1]: our class works in for-each
        List<Integer> viaForEach = new ArrayList<>();
        new Countdown(3).forEach(viaForEach::add);
        show(() -> viaForEach);                                      // forEach is a default method of Iterable
        show(() -> StreamSupport.stream(new Countdown(4).spliterator(), false).filter(x -> x % 2 == 0).toList());   // Iterable -> Stream
        Iterator<Integer> it = new Countdown(2).iterator();
        show(() -> it.next());
        show(() -> it.next());
        show(() -> it.hasNext());                                    // false: the countdown is over
        show(() -> thrown(() -> it.next()));                         // next() past the end must throw NoSuchElementException
        show(() -> thrown(() -> removeNow(it)));                    // remove() is optional: the default method throws
        wontCompile("for (int x : Stream.of(1, 2)) { }", "for-each not applicable to expression type Stream: a Stream and an Iterator are not Iterable");
        note("Arrays work in for-each too, but an array is not an Iterable (it has no iterator() method)");

        section("Removing while looping: fail-fast iterators");
        show(() -> thrown(() -> removeInForEach(new ArrayList<>(List.of(1, 2, 3, 4)), 2)));   // the list changed behind the iterator's back
        show(() -> thrown(() -> removeInForEach(new ArrayList<>(List.of(1, 2, 3)), 2)));      // TRAP: removing the second-to-last element throws NOTHING ...
        show(() -> removeInForEach(new ArrayList<>(List.of(1, 2, 3)), 2));                    // ... the loop just ends early, here by luck of hasNext()
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> evens = numbers.iterator();
        while (evens.hasNext()) {
            if (evens.next() % 2 == 0) evens.remove();               // the iterator deletes and keeps itself in step
        }
        show(() -> numbers);                                         // [1, 3, 5]
        List<Integer> other = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        other.removeIf(x -> x % 2 == 0);
        show(() -> other);                                           // [1, 3, 5]: removeIf is the short form
        note("Fail-fast = ArrayList counts its changes (modCount) and next() throws ConcurrentModificationException when the count moved. Best effort, not a guarantee");
        note("Safe ways: iterator.remove(), removeIf, collect what to remove and removeAll, a backwards index loop, or build a new list (streams)");

        section("ListIterator: both directions, set and add");
        syntax(
                "list.listIterator()  |  listIterator(index)", "hasPrevious() / previous() / nextIndex() / set(x) / add(x) / remove()",
                "set(x)", "replaces the element last returned by next() or previous()"
        );
        List<String> words = new ArrayList<>(List.of("a", "b", "c"));
        ListIterator<String> li = words.listIterator();
        while (li.hasNext()) {
            String w = li.next();
            li.set(w.toUpperCase());
            if (w.equals("b")) li.add("b2");                         // inserted right after the current element; the loop skips it
        }
        show(() -> words);                                           // [A, B, b2, C]
        List<String> backwards = new ArrayList<>();
        ListIterator<String> back = words.listIterator(words.size());
        while (back.hasPrevious()) backwards.add(back.previous());
        show(() -> backwards);                                       // [C, b2, B, A]

        section("Cost");
        bigO(
                "hasNext() / next()", "O(1)",
                "a full for-each over n elements", "O(n)",
                "ArrayList iterator.remove()", "O(n): the elements behind it shift left",
                "LinkedList iterator.remove()", "O(1) once you stand on the node (but getting there was O(n))",
                "list.remove(x) inside for-each", "O(n) per removal, and it risks ConcurrentModificationException"
        );
    }
}
