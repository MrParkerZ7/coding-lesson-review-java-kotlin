package review.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 3c. COLLECTION TYPES: the family tree, which class to pick, converting between them
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.CollectionTypes
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class CollectionTypes {

    public static void main(String[] args) {
        title("COLLECTION TYPES  -  Iterable, Collection, List, Set, Queue, Map");

        section("The family (interfaces)");
        syntax(
                "Iterable<T>", "anything a for-each loop accepts: for (T x : it)",
                "  Collection<T>", "+ size, isEmpty, contains, add, remove, stream",
                "    List<T>", "ordered, index access, duplicates allowed",
                "    Set<T>", "no duplicates (decided by equals / hashCode, or compareTo in a TreeSet)",
                "    Queue<T>  <  Deque<T>", "FIFO: offer, poll, peek  |  both ends: addFirst, pollLast ...",
                "Map<K, V>", "key -> value. NOT a Collection: loop over entrySet(), keySet(), values()"
        );
        wontCompile("for (var e : Map.of(\"a\", 1)) { }", "a Map is not Iterable: loop over entrySet()");
        wontCompile("List<int> xs = new ArrayList<>();", "generics take objects: List<Integer>, boxing every int");
        note("Arrays are not collections and have no methods (lesson 3a, 3b). Code against the interface: List<Integer> xs = new ArrayList<>();");

        section("Which class? (implementations)");
        syntax(
                "ArrayList", "the default List: an array underneath, fast get(i)",
                "LinkedList", "List AND Deque with nodes; almost never the right choice",
                "HashSet | LinkedHashSet | TreeSet", "no order promised | insertion order | sorted",
                "HashMap | LinkedHashMap | TreeMap", "the same three orders, by key",
                "ArrayDeque", "stack AND queue (use it instead of the legacy Stack and Vector)",
                "PriorityQueue", "hands out the smallest (or, with a Comparator, the largest) first",
                "List.of | Set.of | Map.of", "immutable, since Java 9 (more in lesson 13)"
        );

        section("Converting");
        List<Integer> items = List.of(3, 1, 3, 2);
        show(() -> new LinkedHashSet<>(items));                                  // List -> Set: duplicates gone, first-seen order kept
        show(() -> new TreeSet<>(items));                                        // or sorted
        show(() -> new ArrayList<>(items));                                      // any collection -> a MUTABLE ArrayList copy
        show(() -> items.toArray());                                             // List -> Object[] (the element type is lost)
        show(() -> items.toArray(new Integer[0]));                               // List -> Integer[]: pass an empty array of the type
        show(() -> items.stream().mapToInt(Integer::intValue).toArray());        // List<Integer> -> int[]: unbox with a stream
        show(() -> Arrays.stream(new int[] {3, 1}).boxed().toList());            // int[] -> List<Integer>
        show(() -> Arrays.asList("a", "b"));                                     // values -> fixed-size List
        Map<String, Integer> ages = Map.of("ann", 30);
        show(() -> new ArrayList<>(ages.entrySet()));                            // Map -> List of entries, key=value
        show(() -> new ArrayList<>(ages.values()));                              // or of the values only
        show(() -> "hello".chars().mapToObj(c -> (char) c).toList());            // a String is not a collection: chars() is a stream of int

        section("Which one to use");
        note("List: order matters, duplicates ok, access by index       Set: unique elements, fast contains");
        note("Map: look a value up by key (counting, grouping, cache)   Array: fixed size, fastest, primitives");
        note("ArrayDeque: stack and queue    PriorityQueue: always hands out the smallest (or largest) first");
    }
}
