package review.collectionsdepth;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 13a. UNMODIFIABLE vs IMMUTABLE: a read-only view is not a frozen list
 *      Run: mvn -q compile exec:java -Dmain.class=review.collectionsdepth.UnmodifiableVsImmutable
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class UnmodifiableVsImmutable {

    public static void main(String[] args) {
        title("UNMODIFIABLE vs IMMUTABLE  -  a view follows its source, a copy does not");

        section("Six ways to get a list, six different promises");
        syntax(
                "List.of(a, b)  |  List.copyOf(c)", "truly immutable: nobody can change it; no null elements allowed",
                "Collections.unmodifiableList(l)", "a read-only VIEW of l: you cannot change it, but whoever holds l can, and you see it",
                "Arrays.asList(array)", "fixed size, WRITES THROUGH to the array: set() works, add() and remove() throw",
                "stream.toList()  (Java 16)", "immutable, and null elements are allowed",
                "Collectors.toList()", "no promise; today an ArrayList (mutable). Collectors.toUnmodifiableList() promises read-only",
                "new ArrayList<>(c)", "a mutable copy, independent of c"
        );
        List<Integer> source = new ArrayList<>(List.of(1, 2, 3));
        List<Integer> view = Collections.unmodifiableList(source);
        List<Integer> copy = List.copyOf(source);
        source.add(4);
        show(() -> source);
        show(() -> view);                                            // [1, 2, 3, 4]: the view saw the change, it IS the same list
        show(() -> copy);                                            // [1, 2, 3]: List.copyOf took a snapshot
        show(() -> thrown(() -> view.add(5)));                       // the view refuses changes made THROUGH it
        show(() -> thrown(() -> copy.add(5)));
        show(() -> thrown(() -> List.of(1, 2).set(0, 9)));
        note("Kotlin stops view.add(5) at compile time (List has no add); Java types it as List, so it fails at RUN time with UnsupportedOperationException");

        section("List.of: no nulls, no copy of an immutable list");
        show(() -> thrown(() -> List.of(1, null)));                  // NullPointerException: List.of rejects null elements
        show(() -> thrown(() -> List.of(1, 2).contains(null)));      // TRAP: even asking contains(null) throws
        List<Integer> fixed = List.of(7, 8);
        show(() -> List.copyOf(fixed) == fixed);                     // true: copying an immutable list returns the same object
        show(() -> List.copyOf(source) == source);                   // false: a mutable list gets a real copy
        List<StringBuilder> builders = List.of(new StringBuilder("a"));
        builders.get(0).append("!");
        show(() -> builders);                                        // [a!]: immutable list, but its ELEMENTS are still mutable (shallow)

        section("Arrays.asList writes through");
        String[] array = {"a", "b"};
        List<String> asList = Arrays.asList(array);
        asList.set(0, "z");
        show(() -> array);                                           // [z, b]: the list changed the array
        array[1] = "y";
        show(() -> asList);                                          // [z, y]: and the array changes the list
        show(() -> thrown(() -> asList.add("c")));                   // fixed size
        show(() -> thrown(() -> asList.remove(0)));

        section("Streams and maps");
        List<Integer> fromToList = Stream.of(1, null).toList();
        show(() -> fromToList);                                      // [1, null]: stream.toList() allows null, List.of does not
        show(() -> thrown(() -> fromToList.add(3)));                 // but it is immutable
        List<Integer> collected = Stream.of(1, 2).collect(Collectors.toList());
        show(() -> thrown(() -> collected.add(3)));                  // no exception today, but the Javadoc does not promise it: do not rely on it
        Map<String, Integer> ages = new HashMap<>(Map.of("ann", 30));
        Map<String, Integer> agesView = Collections.unmodifiableMap(ages);
        ages.put("bob", 25);
        show(() -> agesView.size());                                 // 2: the map view follows too (Set and Map have the same family of methods)
        note("Set.copyOf and Map.copyOf are the immutable copies, Collections.unmodifiableSet / unmodifiableMap the views");

        section("Cost");
        bigO(
                "Collections.unmodifiableList(l)", "O(1): a wrapper, nothing is copied",
                "Arrays.asList(array)", "O(1): a wrapper around the array",
                "List.copyOf(c)  |  new ArrayList<>(c)", "O(n): copies every element reference (List.copyOf of an immutable list: O(1))",
                "List.of(a, b, c)", "O(n) in the number of arguments, no extra copy later",
                "get(i) on any of them", "O(1)"
        );
    }
}
