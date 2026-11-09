package review.objects;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 12d. PASS BY VALUE: Java copies the value, and for objects the value is the reference
 *      Run: mvn -q compile exec:java -Dmain.class=review.objects.PassByValue
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class PassByValue {

    static class Person {
        String name;

        Person(String name) { this.name = name; }

        @Override
        public String toString() { return "Person(" + name + ")"; }
    }

    static void increment(int n) { n++; }
    static void bump(Integer n) { n++; }                             // boxes a NEW Integer; the caller's variable is unaffected
    static void shout(String s) { s = s.toUpperCase(); }

    static void rename(Person p) { p.name = "Bo"; }                  // follows the copied reference and changes the shared object
    static void replace(Person p) { p = new Person("New"); }         // rebinds the local copy only
    static void swap(Person a, Person b) { Person t = a; a = b; b = t; }
    static boolean sameObject(Object a, Object b) { return a == b; }

    static void addItem(List<String> list) { list.add("x"); }
    static void reset(List<String> list) { list = new ArrayList<>(); list.add("only"); }
    static int[] grow(int[] array) { return Arrays.copyOf(array, array.length + 1); }   // an array cannot grow in place: return the new one

    static List<Integer> sortedBad(List<Integer> in) { Collections.sort(in); return in; }            // sorts the CALLER's list
    static List<Integer> sortedGood(List<Integer> in) { List<Integer> copy = new ArrayList<>(in); Collections.sort(copy); return copy; }

    public static void main(String[] args) {
        title("PASS BY VALUE  -  mutate through the copy, never rebind the caller");

        section("The rule");
        syntax(
                "void f(int n)", "n is a COPY of the number: the method cannot change the caller's variable",
                "void f(Person p)", "p is a COPY of the reference: caller and method point at the SAME object",
                "p.name = ...", "follows the reference: the caller sees the change",
                "p = new Person(...)", "rebinds only the copy: the caller's variable still points at the old object"
        );
        int n = 5;
        increment(n);
        show(() -> n);                                               // 5
        Integer boxed = 5;
        bump(boxed);
        show(() -> boxed);                                           // 5: Integer is immutable and the parameter was rebound
        String word = "abc";
        shout(word);
        show(() -> word);                                            // abc: same reason (immutable + rebound)

        section("Objects: mutate vs reassign");
        Person ann = new Person("Ann");
        rename(ann);
        show(() -> ann);                                             // Person(Bo): the shared object was changed
        Person bob = new Person("Bob");
        replace(bob);
        show(() -> bob);                                             // Person(Bob): the caller's variable was not touched
        Person x = new Person("X");
        Person y = new Person("Y");
        swap(x, y);
        show(() -> x + " " + y);                                     // Person(X) Person(Y): the swap trap, as in lesson 10
        show(() -> sameObject(ann, ann));                            // true: inside the method the parameter is the very same object
        note("Pass by value, always: the value of an object variable is a reference, copied. Java has no pass by reference");

        section("Lists and arrays as parameters");
        List<String> items = new ArrayList<>();
        addItem(items);
        show(() -> items);                                           // [x]: the method added to the caller's list
        reset(items);
        show(() -> items);                                           // [x]: the method's new list was thrown away
        int[] numbers = {1, 2};
        int[] longer = grow(numbers);
        show(() -> numbers);                                         // [1, 2]: the original array has a fixed length
        show(() -> longer);                                          // [1, 2, 0]: the caller must use the RETURNED array
        List<Integer> data = new ArrayList<>(List.of(3, 1, 2));
        sortedBad(data);
        show(() -> data);                                            // [1, 2, 3]: a surprise side effect on the caller's list
        List<Integer> data2 = new ArrayList<>(List.of(3, 1, 2));
        List<Integer> result = sortedGood(data2);
        show(() -> data2);                                           // [3, 1, 2]: untouched
        show(() -> result);                                          // [1, 2, 3]
        note("A method that only needs to READ a collection should not mutate it: copy first, or return a new one (immutability: previous file)");
    }
}
