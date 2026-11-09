package review.objects;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 12c. IMMUTABILITY and final: the recipe, defensive copies, final reference vs immutable object
 *      Run: mvn -q compile exec:java -Dmain.class=review.objects.ImmutabilityAndFinal
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ImmutabilityAndFinal {

    /** The recipe in one class: final class, private final fields, no setters, "with" methods return a new object. */
    static final class Money {
        private final long cents;
        private final String currency;

        Money(long cents, String currency) {
            this.cents = cents;
            this.currency = Objects.requireNonNull(currency, "currency");
        }

        Money plus(Money other) { return new Money(cents + other.cents, currency); }

        @Override
        public String toString() { return cents + " " + currency; }
    }

    /** Leaks in both directions: keeps the caller's list, and hands its own list out. */
    static class Leaky {
        private final List<String> days;

        Leaky(List<String> days) { this.days = days; }

        List<String> days() { return days; }
    }

    /** Copies IN (constructor) and returns something nobody can change (getter). */
    static final class Schedule {
        private final List<String> days;

        Schedule(List<String> days) { this.days = List.copyOf(days); }

        List<String> days() { return days; }
    }

    /** unmodifiableList is a read-only VIEW: it does not copy. */
    static final class ViewOnly {
        private final List<String> days;

        ViewOnly(List<String> days) { this.days = Collections.unmodifiableList(days); }

        List<String> days() { return days; }
    }

    public static void main(String[] args) {
        title("IMMUTABILITY AND final  -  the recipe and its leaks");

        section("The recipe for an immutable class");
        syntax(
                "final class", "nobody can subclass it and add mutable behaviour (String and Integer are final)",
                "private final fields", "assigned once, in the constructor; no setters",
                "plus(...) / withX(...)", "'change' = return a NEW object; the old one stays valid",
                "copy mutable parts IN and OUT", "List.copyOf(list) in the constructor; return an immutable list or a copy (array.clone())",
                "no `this` escape", "do not hand `this` to other code from inside the constructor"
        );
        Money a = new Money(500, "EUR");
        Money b = a.plus(new Money(250, "EUR"));
        show(() -> a);                                               // 500 EUR: plus() did not touch a
        show(() -> b);                                               // 750 EUR
        show(() -> thrown(() -> new Money(1, null)));                // fail fast: a bad object is never created
        wontCompile("class MyString extends String { }", "cannot inherit from final String");

        section("Defensive copies: the leak, the copy and the view");
        List<String> source = new ArrayList<>(List.of("mon"));
        Leaky leaky = new Leaky(source);
        source.add("tue");
        show(() -> leaky.days());                                    // [mon, tue]: the caller still holds the same list as the object
        leaky.days().add("wed");
        show(() -> source);                                          // [mon, tue, wed]: and the getter lets everyone change it
        List<String> source2 = new ArrayList<>(List.of("mon"));
        Schedule schedule = new Schedule(source2);
        source2.add("tue");
        show(() -> schedule.days());                                 // [mon]: List.copyOf took a private copy
        show(() -> thrown(() -> schedule.days().add("x")));          // the getter's list refuses changes
        List<String> source3 = new ArrayList<>(List.of("mon"));
        ViewOnly view = new ViewOnly(source3);
        source3.add("tue");
        show(() -> view.days());                                     // [mon, tue]: a view follows the list it wraps (lesson 13)
        show(() -> thrown(() -> view.days().add("x")));              // read-only through the view, but the owner can still change the list

        section("final: the variable, not the object");
        final List<Integer> numbers = new ArrayList<>();
        numbers.add(1);
        show(() -> numbers);                                         // [1]: final stops reassignment, not mutation
        final int[] array = {1, 2};
        array[0] = 9;
        show(() -> array);                                           // [9, 2]
        wontCompile("final List<Integer> numbers = ...; numbers = new ArrayList<>();", "cannot assign a value to final variable numbers");
        String text = "abc";
        show(() -> text.toUpperCase());                              // "ABC" is a NEW String
        show(() -> text);                                            // abc: String methods never change the String itself
        note("final field = assigned once (and safely published to other threads); immutable object = no way to change its state at all");
        note("Why it matters: an immutable object can be shared by any number of threads with no locks, cached, and used as a map key");
    }
}
