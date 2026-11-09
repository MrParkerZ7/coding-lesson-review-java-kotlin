package review.loops;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 2c. LOOPING WITH LAMBDAS AND ITERATORS: forEach has no break, Iterator.remove
 *     Run: mvn -q compile exec:java -Dmain.class=review.loops.ForEachAndIterator
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ForEachAndIterator {

    /** it.remove() without a next() before it throws IllegalStateException. */
    private static Object removeBeforeNext() {
        Iterator<Integer> fresh = new ArrayList<>(List.of(1)).iterator();
        fresh.remove();
        return null;
    }

    public static void main(String[] args) {
        title("FOREACH AND ITERATOR  -  loops that are not for or while");

        section("forEach(...) with a lambda");
        syntax(
                "xs.forEach(x -> ...)", "Iterable.forEach: the lambda runs once per element",
                "map.forEach((k, v) -> ...)", "Map.forEach: key and value as two parameters"
        );
        Map<String, Integer> ages = new LinkedHashMap<>();
        ages.put("ann", 30);
        ages.put("bob", 25);
        List<String> lines = new ArrayList<>();
        ages.forEach((k, v) -> lines.add(k + "=" + v));   show(() -> lines);   // a Map loop without entrySet()

        section("break and continue do not exist inside a lambda");
        syntax(
                "xs.forEach(x -> { if (...) return; ... })", "return inside the lambda acts like continue",
                "xs.stream().takeWhile(cond).forEach(...)", "stops at the first element where cond is false: a break (Java 9)"
        );
        wontCompile("xs.forEach(x -> { if (x == 1) break; })", "break outside switch or loop: a lambda is not a loop");
        List<Integer> skipTwo = new ArrayList<>();
        List.of(1, 2, 3, 4).forEach(x -> {
            if (x == 2) return;
            skipTwo.add(x);
        });
        show(() -> skipTwo);                                           // return = continue, the other elements still run
        List<Integer> stopAtThree = new ArrayList<>();
        List.of(1, 2, 3, 4).stream().takeWhile(x -> x != 3).forEach(stopAtThree::add);
        show(() -> stopAtThree);                                       // like a break at 3
        note("When you need break, continue, the index or a changing counter, a plain for loop is the simplest and clearest choice (lesson 1: a lambda cannot change a local)");

        section("Removing while looping: Iterator.remove");
        syntax(
                "it = xs.iterator(); while (it.hasNext()) { ...; it.remove(); }", "the legal way to delete during a loop",
                "xs.removeIf(x -> condition)", "the short form (lesson 3 shows all the safe ways)"
        );
        List<Integer> numbers = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        Iterator<Integer> it = numbers.iterator();
        while (it.hasNext()) {
            if (it.next() % 2 == 0) it.remove();
        }
        show(() -> numbers);                                           // the evens are gone, no ConcurrentModificationException
        List<Integer> small = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        small.removeIf(x -> x > 3);   show(() -> small);               // the same job without writing the loop
        show(() -> thrown(() -> removeBeforeNext()));                  // it.remove() is only legal right after it.next()
        note("A for-each that removes from its own list throws ConcurrentModificationException: ForAndRanges");
    }
}
