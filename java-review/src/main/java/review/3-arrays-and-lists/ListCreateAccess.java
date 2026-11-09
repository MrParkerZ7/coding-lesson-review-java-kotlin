package review.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 3d. LISTS: creating (mutable, fixed-size, immutable), reading, copy vs alias, 2D lists
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ListCreateAccess
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ListCreateAccess {

    public static void main(String[] args) {
        title("LISTS  -  create and read");

        section("Creating: three kinds of List");
        syntax(
                "new ArrayList<>() | new ArrayList<>(other)", "mutable and growable | a mutable copy of any collection",
                "List.of(1, 2, 3) | List.copyOf(other)", "IMMUTABLE (Java 9+), rejects null",
                "Arrays.asList(1, 2, 3)", "FIXED SIZE: set is allowed, add and remove are not",
                "Collections.nCopies(n, x) | List.of()", "n times the same value | empty (both immutable)"
        );
        List<Integer> mutable = new ArrayList<>(List.of(1, 2, 3));   show(() -> mutable);
        List<Integer> immutable = List.of(1, 2, 3);
        show(() -> thrown(() -> immutable.add(4)));                              // no add ...
        show(() -> thrown(() -> immutable.set(0, 9)));                           // ... and no set either
        show(() -> thrown(() -> List.of(1, null)));                              // List.of rejects null elements
        show(() -> thrown(() -> immutable.contains(null)));                      // TRAP: even asking for null throws
        show(() -> Collections.nCopies(3, 0));                                   // [0, 0, 0], a list of zeros
        show(() -> IntStream.rangeClosed(1, 5).boxed().toList());                // 1..5 as a List<Integer>
        String[] arr = {"a", "b", "c"};
        List<String> view = Arrays.asList(arr);
        view.set(0, "z");                     show(() -> arr[0]);                // TRAP: the list writes THROUGH to the array ...
        arr[1] = "y";                         show(() -> view);                  // ... and the array writes through to the list
        show(() -> thrown(() -> view.add("d")));                                 // but the size is fixed
        show(() -> thrown(() -> view.remove(0)));                                // remove also throws
        note("The type column shows the REAL class: Arrays$ArrayList is not java.util.ArrayList; ImmutableCollections$... is List.of or toList()");

        section("Reading");
        syntax(
                "xs.get(i) | xs.set(i, x)", "element at index i (from 0) | replace it, returns the old one",
                "xs.getFirst() | xs.getLast()", "Java 21 ends (before 21: xs.get(xs.size() - 1))",
                "xs.size() | xs.isEmpty()", "length | is it empty",
                "xs.contains(x) | xs.indexOf(x)", "is it there | first position, or -1",
                "xs.reversed()", "Java 21: a reverse-order VIEW"
        );
        List<Integer> xs = new ArrayList<>(List.of(10, 20, 30, 40, 50));
        show(() -> xs.get(0));
        show(() -> xs.getFirst());
        show(() -> xs.getLast());
        show(() -> xs.size());
        show(() -> xs.indexOf(30));
        show(() -> xs.indexOf(99));                                              // -1 when not found, no exception
        show(() -> xs.contains(30));
        show(() -> xs.reversed());                                               // a view in reverse order (xs itself is not changed)
        show(() -> thrown(() -> xs.get(99)));                                    // a bad index throws (no getOrNull in Java)
        show(() -> thrown(() -> new ArrayList<Integer>().getFirst()));           // getFirst() on an empty list throws, it does not return null
        note("Always check isEmpty() or size() before get(0): there is no safe getOrNull");

        section("Copy vs alias, equality");
        List<Integer> original = new ArrayList<>(List.of(1, 2, 3));
        List<Integer> alias = original;
        List<Integer> copy = new ArrayList<>(original);
        original.add(4);                      show(() -> alias);                 // alias is the SAME list
        show(() -> copy);                                                        // the copy is independent
        show(() -> List.of(1, 2).equals(new LinkedList<>(List.of(1, 2))));       // true: equals() compares the contents, even across classes
        show(() -> alias == original);                                           // == asks: the same object?
        show(() -> copy == original);                                            // false: a different object with equal contents
        note("new ArrayList<>(other) is a SHALLOW copy: a list of lists shares its inner lists (see 3f)");

        section("2D lists");
        List<List<Integer>> grid = new ArrayList<>();
        for (int r = 0; r < 3; r++) {
            List<Integer> row = new ArrayList<>();                               // a NEW row each time
            for (int c = 0; c < 3; c++) row.add(r * 3 + c);
            grid.add(row);
        }
        show(() -> grid);
        grid.get(1).set(2, 50);               show(() -> grid.get(1));
        show(() -> grid.stream().flatMap(List::stream).toList());                // flatten to one list

        section("Cost");
        bigO(
                "xs.get(i) | xs.set(i, x)", "O(1) on an ArrayList",
                "xs.add(x) at the end", "O(1) amortized (the array grows when it is full)",
                "xs.add(0, x) | xs.remove(i)", "O(n): the rest shifts",
                "xs.contains(x) | xs.indexOf(x)", "O(n)",
                "LinkedList.get(i)", "O(n): walks the nodes"
        );
    }
}
