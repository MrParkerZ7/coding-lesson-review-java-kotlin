package review.lists;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import static review.Output.bigO;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

/**
 * 3f. LISTS: slicing (subList is a view), sorting, searching, shallow copies
 *     Run: mvn -q compile exec:java -Dmain.class=review.lists.ListSliceSortSearch
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class ListSliceSortSearch {

    record Person(String name, int age) {
    }

    public static void main(String[] args) {
        title("LISTS  -  slice, sort, search");

        section("Slicing: subList is a VIEW");
        syntax(
                "xs.subList(from, to)", "a VIEW of part of the list (to is exclusive), not a copy",
                "new ArrayList<>(xs.subList(a, b))", "an independent copy of that part",
                "xs.stream().skip(n).limit(k).toList()", "drop n, take k (lesson 4); there is no take(n) or drop(n) on a List"
        );
        List<Integer> xs = new ArrayList<>(List.of(10, 20, 30, 40, 50));
        List<Integer> middle = xs.subList(1, 3);
        show(() -> middle);                                                      // [20, 30]: index 3 is not included
        middle.set(0, 99);                    show(() -> xs);                    // writing through the view changes xs
        show(() -> new ArrayList<>(xs.subList(0, 2)));                           // a copy: later changes to xs do not reach it
        show(() -> xs.stream().skip(3).toList());                                // like drop(3)
        show(() -> xs.subList(0, Math.min(10, xs.size())));                      // like take(10): clamp the end yourself
        show(() -> thrown(() -> xs.subList(3, 9)));                              // to is past the end
        List<Integer> parent = new ArrayList<>(List.of(1, 2, 3, 4));
        List<Integer> view = parent.subList(1, 3);
        parent.add(5);                        show(() -> thrown(() -> view.size()));   // TRAP: changing the parent size breaks the view
        xs.subList(0, 2).clear();             show(() -> xs);                    // idiom: delete a range through a view

        section("Sorting");
        syntax(
                "Collections.sort(xs) | xs.sort(null)", "natural order, IN PLACE (mutable list, Comparable elements)",
                "xs.sort(Comparator)", "in place with a rule; stable: equal elements keep their order",
                "xs.stream().sorted().toList()", "a NEW sorted list, xs untouched",
                "Collections.reverse | swap | shuffle", "in place: reverse | swap two positions | random order"
        );
        List<Person> people = new ArrayList<>(List.of(new Person("Ann", 30), new Person("Bob", 25), new Person("Cid", 30)));
        Comparator<Person> byAge = Comparator.comparingInt(Person::age);
        people.sort(byAge);                   show(() -> names(people));         // by age; Ann stays before Cid (stable)
        people.sort(byAge.reversed());        show(() -> names(people));         // descending; the tie Ann, Cid keeps its order
        people.sort(byAge.thenComparing(Person::name));   show(() -> names(people));   // age, then name breaks ties
        List<String> letters = new ArrayList<>(List.of("b", "C", "a"));
        Collections.sort(letters);            show(() -> letters);               // TRAP: uppercase sorts BEFORE lowercase (character codes)
        letters.sort(String.CASE_INSENSITIVE_ORDER);   show(() -> letters);
        List<Integer> m = new ArrayList<>(List.of(3, 1, 2));
        m.sort(Comparator.reverseOrder());    show(() -> m);                     // descending
        Collections.reverse(m);               show(() -> m);
        Collections.swap(m, 0, 2);            show(() -> m);
        List<Integer> deck = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8));
        Collections.shuffle(deck, new Random(42));   show(() -> deck);           // the same seed gives the same order every run
        show(() -> failure(() -> Collections.sort(List.of(3, 1, 2))));           // TRAP: sorting an immutable list throws
        note("a - b as a Comparator can overflow: use Integer.compare or Comparator.comparingInt (lesson 13)");

        section("Searching");
        syntax(
                "xs.indexOf(x) | xs.lastIndexOf(x)", "first position | last position, or -1 when missing",
                "Collections.binarySearch(sorted, x)", "index, or -(insertion point) - 1 when missing; the list MUST be sorted",
                "Collections.max(xs) | min(xs) | frequency(xs, x)", "biggest | smallest | how many times x occurs"
        );
        List<Integer> data = List.of(5, 3, 8, 3, 9);
        show(() -> data.indexOf(3));
        show(() -> data.lastIndexOf(3));
        show(() -> Collections.frequency(data, 3));
        show(() -> Collections.max(data));
        show(() -> thrown(() -> Collections.max(new ArrayList<Integer>())));     // max of an empty list throws (a stream gives an Optional)
        List<Integer> sorted = data.stream().sorted().toList();   show(() -> sorted);
        show(() -> Collections.binarySearch(sorted, 8));                         // found at index 3
        show(() -> Collections.binarySearch(sorted, 6));                         // missing: 6 would be inserted at 3, so -(3) - 1
        show(() -> Collections.binarySearch(sorted, 1));                         // would go at 0: -(0) - 1
        show(() -> Collections.binarySearch(sorted, 99));                        // would go at the end (5): -(5) - 1
        int[] arr = {1, 3, 5, 8, 9};
        show(() -> Arrays.binarySearch(arr, 4));                                 // the same rule for arrays: insertion point 2, so -3
        note("On an unsorted list binarySearch returns garbage without an error. With duplicates, WHICH one it finds is unspecified");

        section("Copy is shallow");
        List<List<Integer>> rows = new ArrayList<>(List.of(new ArrayList<>(List.of(1, 2)), new ArrayList<>(List.of(3))));
        List<List<Integer>> shallow = new ArrayList<>(rows);
        rows.get(0).add(99);                  show(() -> shallow);               // TRAP: the inner lists are SHARED
        rows.add(new ArrayList<>());          show(() -> shallow.size());        // the outer list is its own: still 2
        note("A deep copy copies each inner list too: for (List<Integer> r : rows) deep.add(new ArrayList<>(r))");

        section("Cost");
        bigO(
                "sort | Collections.sort", "O(n log n), stable (TimSort)",
                "indexOf | contains | frequency | max | min", "O(n)",
                "Collections.binarySearch", "O(log n) on an ArrayList (needs sorted input)",
                "subList(a, b)", "O(1) to create (a view); a copy is O(k)",
                "reverse | shuffle | swap", "O(n) | O(n) | O(1)"
        );
    }

    private static List<String> names(List<Person> people) {
        return people.stream().map(Person::name).toList();
    }

    /** For calls that return void, which Output.thrown cannot take. */
    private static String failure(Runnable action) {
        try {
            action.run();
            return "no exception";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName();
        }
    }
}
