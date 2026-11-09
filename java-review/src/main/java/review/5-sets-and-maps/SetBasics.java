package review.sets;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 5a. SETS: unique elements and fast contains
 *     Run: mvn -q compile exec:java -Dmain.class=review.sets.SetBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class SetBasics {

    /** A class WITHOUT equals and hashCode: two objects with the same id are still two different elements. */
    static class Plain {
        final int id;

        Plain(int id) {
            this.id = id;
        }
    }

    /** A record has equals and hashCode written for it. */
    record Point(int x, int y) {
    }

    /** Set algebra on COPIES, because addAll / retainAll / removeAll change the set they are called on. */
    static <T> Set<T> union(Set<T> a, Set<T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.addAll(b);
        return result;
    }

    static <T> Set<T> intersection(Set<T> a, Set<T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.retainAll(b);
        return result;
    }

    static <T> Set<T> difference(Set<T> a, Set<T> b) {
        Set<T> result = new LinkedHashSet<>(a);
        result.removeAll(b);
        return result;
    }

    /** First value that appears a second time, or null. add() is false when the value was already in the set. O(n). */
    static Integer firstDuplicate(List<Integer> xs) {
        Set<Integer> seen = new HashSet<>();
        for (int x : xs) if (!seen.add(x)) return x;
        return null;
    }

    public static void main(String[] args) {
        title("SETS  -  no duplicates, fast lookup");

        section("Creating: three orders");
        syntax(
                "new HashSet<>(coll)", "no order promised; the fastest",
                "new LinkedHashSet<>(coll)", "insertion order (first-seen)",
                "new TreeSet<>(coll)", "sorted (natural order, or a Comparator); the elements must be Comparable",
                "Set.of(1, 2, 3) | Set.copyOf(coll)", "IMMUTABLE (Java 9+), no nulls, iteration order differs from run to run"
        );
        List<Integer> raw = List.of(30, 10, 20, 10);
        show(() -> new HashSet<>(raw));                                          // the hash table's order, not 30 10 20: not promised
        show(() -> new LinkedHashSet<>(raw));                                    // duplicates dropped, first-seen order kept
        show(() -> new TreeSet<>(raw));                                          // sorted
        show(() -> Set.of(1, 2).equals(Set.of(2, 1)));                           // true: equality ignores the order
        show(() -> thrown(() -> Set.of(1, 1)));                                  // Set.of rejects a duplicate ...
        show(() -> Set.copyOf(List.of(1, 1, 2)).size());                         // ... Set.copyOf just drops it
        show(() -> thrown(() -> Set.of(1).add(2)));                              // immutable
        show(() -> new TreeSet<>(Set.of("b", "a", "c")));                        // sort a Set.of before printing: its own order changes every run

        section("Changing and asking");
        syntax(
                "s.add(x) | s.remove(x)", "true when the set changed, false when it did not",
                "s.contains(x) | s.size()", "fast membership test | number of elements"
        );
        Set<Integer> s = new HashSet<>(List.of(1, 2));
        show(() -> s.add(3));                                                    // true: it was new
        show(() -> s.add(3));                                                    // false: already there, nothing changed
        show(() -> s.remove(1));
        show(() -> s.contains(2));
        show(() -> s);
        note("The type column shows Boolean for a boolean and Integer for an int: primitives are shown boxed (lesson 0)");

        section("Set algebra");
        syntax(
                "r.addAll(b) | r.retainAll(b) | r.removeAll(b)", "union | intersection | difference: they change r, so copy first",
                "a.containsAll(b) | Collections.disjoint(a, b)", "is b inside a | do they share nothing"
        );
        Set<Integer> a = new TreeSet<>(List.of(1, 2, 3));
        Set<Integer> b = new TreeSet<>(List.of(3, 4));
        show(() -> union(a, b));
        show(() -> intersection(a, b));
        show(() -> difference(a, b));
        show(() -> a.containsAll(List.of(1, 2)));
        a.retainAll(b);                       show(() -> a);                     // TRAP: retainAll changed a itself (the helpers copy first)

        section("Your own classes need equals and hashCode");
        Set<Plain> plain = new HashSet<>();
        plain.add(new Plain(1));
        plain.add(new Plain(1));
        show(() -> plain.size());                                                // 2: no equals / hashCode, so every object is "different"
        Set<Point> points = new HashSet<>();
        points.add(new Point(1, 2));
        points.add(new Point(1, 2));
        show(() -> points.size());                                               // 1: a record compares its fields
        show(() -> points.contains(new Point(1, 2)));                            // true: found by value
        note("Writing equals and hashCode by hand, and why both: lesson 12");

        section("Two classic uses");
        List<Integer> dup = List.of(1, 2, 2);
        show(() -> new HashSet<>(dup).size() != dup.size());                     // contains a duplicate? compare the sizes
        show(() -> firstDuplicate(List.of(4, 2, 7, 2, 4)));                      // the add() idiom: check and insert in one call
        show(() -> firstDuplicate(List.of(1, 2, 3)));                            // null when there is none
        note("set.add(x) doing the check and the insert in one call is the interview idiom for duplicates");
        note("TreeSet also answers floor / ceiling / headSet questions: see 5d TreeNavigation");

        section("Cost");
        bigO(
                "HashSet | LinkedHashSet: add, remove, contains", "O(1) on average",
                "TreeSet: add, remove, contains", "O(log n), the set stays sorted",
                "list.contains(x)", "O(n): a set is the fix for slow lookups",
                "building a set from n elements", "O(n) hashed, O(n log n) sorted"
        );
    }
}
