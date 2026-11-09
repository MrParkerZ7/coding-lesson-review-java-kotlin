package review.generics;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 14b. WILDCARDS AND PECS: invariance, ? extends, ? super, ?
 *      Run: mvn -q compile exec:java -Dmain.class=review.generics.WildcardsPECS
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class WildcardsPECS {

    static class Animal {
        final String name;

        Animal(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "Animal(" + name + ")";
        }
    }

    static class Dog extends Animal {
        Dog(String name) {
            super(name);
        }

        @Override
        public String toString() {
            return "Dog(" + name + ")";
        }
    }

    /** ? extends: the list PRODUCES Numbers for us. We can read; we cannot add. */
    static double sum(List<? extends Number> numbers) {
        double total = 0;
        for (Number n : numbers) total += n.doubleValue();
        return total;
    }

    /** ? super: the list CONSUMES Integers from us. We can add; what we read is only an Object. */
    static void addOneToThree(List<? super Integer> sink) {
        for (int i = 1; i <= 3; i++) sink.add(i);
    }

    /** ? alone: the element type does not matter, so only type-independent methods are allowed. */
    static int countNulls(List<?> list) {
        int nulls = 0;
        for (Object o : list) if (o == null) nulls++;
        return nulls;
    }

    /** PECS in one signature: read from src (Producer Extends), write to dst (Consumer Super). */
    static <T> void copyAll(List<? extends T> src, List<? super T> dst) {
        for (T item : src) dst.add(item);
    }

    public static void main(String[] args) {
        title("WILDCARDS AND PECS  -  invariance, ? extends, ? super");

        section("Invariant: List<Dog> is NOT a List<Animal>");
        syntax("List<Dog> is not a subtype of List<Animal>", "even though Dog is a subtype of Animal: generics are invariant");
        wontCompile("List<Animal> a = new ArrayList<Dog>();", "ArrayList<Dog> cannot be converted to List<Animal>: it would let you add a Cat to a list of Dogs");
        wontCompile("List<Object> o = new ArrayList<String>();", "ArrayList<String> cannot be converted to List<Object>");
        List<Animal> animals = new ArrayList<>(List.of(new Animal("Tom")));
        List<Dog> dogs = new ArrayList<>(List.of(new Dog("Rex"), new Dog("Max")));
        note("Arrays ARE covariant (Object[] o = new String[1]) and fail at run time instead: see ErasureAndLimits");

        section("? extends T: read only (Producer)");
        syntax("List<? extends Number>", "a list of SOME subtype of Number: reading gives a Number, writing is refused");
        show(() -> sum(List.of(1, 2, 3)));                                 // List<Integer> is accepted
        show(() -> sum(List.of(1.5, 2.5)));                                // so is List<Double>
        List<? extends Animal> readOnly = dogs;
        show(() -> readOnly.get(0));                                       // reading gives an Animal (here it is a Dog)
        wontCompile("readOnly.add(new Dog(\"Fido\"));", "Dog cannot be converted to CAP#1: the list might really be a List<Cat>");
        List<? extends Number> onlyNull = new ArrayList<Integer>();
        onlyNull.add(null);
        show(() -> onlyNull);                                              // null is the one value that fits every type, so add(null) compiles

        section("? super T: write only (Consumer)");
        syntax("List<? super Integer>", "a list of SOME supertype of Integer: writing Integers is safe, reading gives an Object");
        List<Number> numbers = new ArrayList<>();
        List<Object> objects = new ArrayList<>();
        addOneToThree(numbers);
        addOneToThree(objects);
        show(() -> numbers);                                               // a List<Number> accepts Integers
        show(() -> objects);                                               // so does a List<Object>
        List<? super Dog> sink = animals;
        sink.add(new Dog("Fido"));
        show(() -> animals);                                               // a Dog went into a List<Animal> through ? super Dog
        Object first = sink.get(0);   show(() -> first);                                                 // what comes out of ? super Dog is only an Object
        wontCompile("Dog d = sink.get(0);", "CAP#1 cannot be converted to Dog: reading from ? super Dog gives only Object");

        section("? unbounded");
        syntax("List<?>", "a list of unknown element type: you can read Objects and call size(), not add (except null)");
        show(() -> countNulls(Collections.nCopies(2, null)));
        show(() -> countNulls(List.of("a", "b")));
        wontCompile("List<?> l = animals; l.add(new Animal(\"x\"));", "Animal cannot be converted to CAP#1: only null can be added");
        note("List<?> is NOT the raw type List: List<?> keeps the checks, the raw List turns them off (see ErasureAndLimits)");

        section("PECS: Producer Extends, Consumer Super");
        syntax(
                "Collection<? extends T> src", "you READ T from it: it produces T",
                "Collection<? super T> dst", "you WRITE T into it: it consumes T",
                "Comparator<? super T>", "a comparator of any supertype of T can compare Ts"
        );
        List<Animal> zoo = new ArrayList<>(List.of(new Animal("Tom")));
        copyAll(dogs, zoo);                                                // from List<Dog> (extends) into List<Animal> (super)
        show(() -> zoo);                                                   // from a List<Dog> into a List<Animal>
        List<Animal> target = new ArrayList<>(Collections.nCopies(2, new Animal("-")));
        Collections.copy(target, List.of(new Dog("Ann"), new Dog("Bo")));  // copy(List<? super T> dest, List<? extends T> src)
        show(() -> target);                                                // overwrites in place: dest must already be as long as src
        Comparator<Animal> byName = Comparator.comparing(a -> a.name);
        List<Dog> sorted = new ArrayList<>(List.of(new Dog("Max"), new Dog("Ann")));
        sorted.sort(byName);                                               // sort wants Comparator<? super Dog>: an Animal comparator fits
        show(() -> sorted);
        note("If you both read AND write the same collection, use plain List<T>, no wildcard. Return types: avoid wildcards");
    }
}
