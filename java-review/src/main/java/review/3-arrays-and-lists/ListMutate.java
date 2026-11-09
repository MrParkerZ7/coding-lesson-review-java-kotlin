package review.lists;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 3e. LISTS: adding, removing, changing, and removing safely while looping
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ListMutate
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ListMutate {

    public static void main(String[] args) {
        title("LISTS  -  add, remove, change");

        section("Adding, removing, changing (a mutable List only)");
        syntax(
                "m.add(x) | m.add(i, x) | m.addAll(xs)", "append | insert at index i | append many",
                "m.remove(int i) | m.remove(Object o)", "by INDEX, returns the element | by VALUE, returns a boolean",
                "m.removeIf(pred) | m.removeAll(xs)", "by condition | everything that is also in xs",
                "m.set(i, x) | m.replaceAll(f) | m.clear()", "replace one | replace every element | remove everything"
        );
        List<Integer> m = new ArrayList<>(List.of(1, 2, 3));
        m.add(4);                             show(() -> m);
        m.add(0, 99);                         show(() -> m);                     // insert at index 0: the rest shifts right
        m.addAll(List.of(6, 7));              show(() -> m);
        m.remove(Integer.valueOf(99));        show(() -> m);                     // by VALUE: the argument is an Integer object
        show(() -> m.remove(0));                                                 // by INDEX: returns the removed element (the 1)
        show(() -> m.set(0, 100));                                               // set returns the OLD element
        m.replaceAll(x -> x + 1);             show(() -> m);                     // every element +1
        m.removeIf(x -> x % 2 == 0);          show(() -> m);                     // remove everything matching
        m.clear();                            show(() -> m);
        show(() -> thrown(() -> m.get(0)));                                      // an empty list has no index 0

        section("Trap: remove(int) or remove(Object)?");
        List<Integer> ints = new ArrayList<>(List.of(10, 20, 30));
        show(() -> ints.remove(1));                                              // 20: a plain int means INDEX 1, not the value 1
        show(() -> ints);
        show(() -> ints.remove(Integer.valueOf(10)));                            // true: the Integer overload removes by VALUE
        show(() -> thrown(() -> ints.remove(30)));                               // the 30 is an INDEX: out of range
        show(() -> ints.remove((Integer) 30));                                   // a cast to Integer picks the by-value overload
        show(() -> ints);
        note("List<Integer>.remove(x): an int variable is an INDEX, an Integer is a VALUE");

        section("Removing while looping");
        List<Integer> broken = new ArrayList<>(List.of(1, 2, 3, 4));
        show(() -> removeInForEach(broken, 2));                                  // for-each + remove = ConcurrentModificationException
        List<Integer> three = new ArrayList<>(List.of(1, 2, 3));
        show(() -> removeInForEach(three, 2));                                   // TRAP: removing the second-to-last element throws NOTHING ...
        show(() -> three);                                                       // ... because hasNext() sees the shorter list and the loop just ends
        List<Integer> safe = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        safe.removeIf(x -> x % 2 == 0);       show(() -> safe);                  // 1. removeIf: the simplest safe way
        List<Integer> source = List.of(1, 2, 3, 4, 5, 6);
        show(() -> source.stream().filter(x -> x % 2 == 1).toList());            // 2. build a NEW list with filter (also works on an immutable one)
        List<Integer> backwards = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (int i = backwards.size() - 1; i >= 0; i--) if (backwards.get(i) % 2 == 0) backwards.remove(i);
        show(() -> backwards);                                                   // 3. index loop from the END: the indexes still to visit stay valid
        List<Integer> withIterator = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (Iterator<Integer> it = withIterator.iterator(); it.hasNext(); ) if (it.next() % 2 == 0) it.remove();
        show(() -> withIterator);                                                // 4. Iterator.remove(): the loop-safe way by hand
        note("Rule: never add or remove on a list that a for-each loop is walking, except through the Iterator");

        section("Cost");
        bigO(
                "add(x) at the end", "O(1) amortized",
                "add(0, x) | add(i, x) | remove(i) | remove(Object)", "O(n): the rest shifts (remove(Object) also searches)",
                "removeIf | removeAll | replaceAll", "O(n): one pass",
                "remove inside a loop, one at a time", "O(n^2) for n removals: prefer removeIf",
                "LinkedList add/remove at a known node", "O(1), but finding the node is O(n)"
        );
    }

    /** Removes the target inside a for-each loop and returns the exception it caused, or "no exception". */
    private static String removeInForEach(List<Integer> list, int target) {
        return thrown(() -> {
            for (Integer x : list) if (x == target) list.remove(x);              // x is an Integer: remove(Object), by value
            return null;
        });
    }
}
