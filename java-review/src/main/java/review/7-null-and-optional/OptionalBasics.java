package review.nullsafety;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * 7b. OPTIONAL BASICS: of, ofNullable, map, flatMap, filter, orElse, orElseGet, orElseThrow
 *     (Optional is a box that holds one value or nothing: a return type that says "there may be no result")
 *     Run: mvn -q compile exec:java -Dmain.class=review.nullsafety.OptionalBasics
 *     Or click the green arrow beside `main` in IntelliJ.
 */
public class OptionalBasics {

    record Address(String city) {
    }

    record Person(String name, Address address) {   // address may be null
    }

    private static final Map<String, Integer> AGES = Map.of("ann", 31, "bob", 25);

    /** An age from the table: empty when the name is unknown. */
    static Optional<Integer> ageOf(String name) {
        return Optional.ofNullable(AGES.get(name));
    }

    /** The city of a person: empty when there is no address or no city. */
    static Optional<String> cityOf(Person p) {
        return Optional.ofNullable(p.address()).map(Address::city);
    }

    /** The whole chain, null-safe at every link, with a default at the end. */
    static String cityOrUnknown(Person p) {
        return Optional.ofNullable(p).map(Person::address).map(Address::city).orElse("unknown");
    }

    /** The same rule with null checks: harder to read, and one forgotten check is an NPE. */
    static String cityOrUnknownChecks(Person p) {
        if (p != null && p.address() != null && p.address().city() != null) return p.address().city();
        return "unknown";
    }

    /** A default that costs something: it logs that it was built. */
    static String fallback(List<String> log) {
        log.add("fallback built");
        return "fallback";
    }

    public static void main(String[] args) {
        title("OPTIONAL BASICS  -  a box for 'maybe a value'");

        Person ann = new Person("Ann", new Address("Oslo"));
        Person bob = new Person("Bob", null);
        Person cy = new Person("Cy", new Address(null));
        String missing = null;

        section("Creating: of, ofNullable, empty");
        syntax(
                "Optional.of(x)", "a box holding x; NullPointerException when x is null",
                "Optional.ofNullable(x)", "empty when x is null, else holds x: the bridge from null-returning code",
                "Optional.empty()", "the empty box"
        );
        show(() -> Optional.of("java"));
        show(() -> Optional.ofNullable(missing));
        show(() -> Optional.empty());
        show(() -> thrown(() -> Optional.of(missing)));                 // TRAP: of(null) throws, ofNullable(null) is empty
        show(() -> ageOf("ann"));
        show(() -> ageOf("zed"));                                       // no age on file: empty, not null
        show(() -> ageOf("ann").isPresent());
        show(() -> ageOf("zed").isEmpty());                             // isEmpty() is Java 11

        section("Reading: orElse, orElseGet, orElseThrow");
        syntax(
                "opt.orElse(other)", "the value, or other. other is computed EAGERLY, before the call",
                "opt.orElseGet(() -> other)", "the value, or the Supplier's result. Computed LAZILY, only when empty",
                "opt.orElseThrow() | orElseThrow(() -> new X())", "the value, or NoSuchElementException | your own exception"
        );
        show(() -> ageOf("zed").orElse(-1));
        show(() -> ageOf("ann").orElse(-1));                            // 31: the left side wins when a value is there
        List<String> log = new ArrayList<>();
        Optional<String> present = Optional.of("value");
        present.orElse(fallback(log));
        int afterOrElse = log.size();  show(() -> afterOrElse);         // TRAP: 1, fallback(log) RAN although the value was there
        present.orElseGet(() -> fallback(log));
        int afterOrElseGet = log.size();  show(() -> afterOrElseGet);   // still 1: orElseGet did not call it. Use orElseGet for costly defaults
        show(() -> thrown(() -> ageOf("zed").orElseThrow()));
        show(() -> thrown(() -> ageOf("zed").orElseThrow(() -> new IllegalArgumentException("unknown name"))));

        section("Transforming: map, flatMap, filter");
        syntax(
                "opt.map(f)", "apply f to the value if present; a null result becomes empty",
                "opt.flatMap(f)", "f itself returns an Optional: avoids Optional<Optional<T>>",
                "opt.filter(predicate)", "keep the value only if it passes, else empty"
        );
        show(() -> ageOf("ann").map(a -> a + 1));
        show(() -> ageOf("zed").map(a -> a + 1));                       // empty: the lambda never runs
        show(() -> ageOf("ann").filter(a -> a > 40));                   // the value failed the test: empty
        show(() -> Optional.of("x").map(s -> (String) null));           // a function returning null gives empty, not an exception
        show(() -> cityOf(ann));
        show(() -> cityOf(bob));                                        // address is null
        show(() -> cityOf(cy));                                         // city is null
        show(() -> cityOf(ann).map(String::length));
        show(() -> Optional.of("ann").flatMap(OptionalBasics::ageOf));  // flatMap for a function that returns an Optional
        show(() -> Optional.of("ann").map(OptionalBasics::ageOf));      // TRAP: map nests the boxes: Optional[Optional[31]]

        section("Acting: ifPresent, ifPresentOrElse, or, stream");
        syntax(
                "opt.ifPresent(consumer)", "run the consumer only when a value is there",
                "opt.ifPresentOrElse(consumer, runnable)", "two branches (Java 9)",
                "opt.or(() -> otherOptional)", "when empty, try another Optional (Java 9)",
                "opt.stream()", "a Stream of 0 or 1 elements (Java 9): flatMap a list of Optionals to its values"
        );
        List<String> seen = new ArrayList<>();
        ageOf("ann").ifPresent(a -> seen.add("saw " + a));
        ageOf("zed").ifPresent(a -> seen.add("saw " + a));              // nothing happens
        ageOf("zed").ifPresentOrElse(a -> seen.add("saw " + a), () -> seen.add("no age"));
        show(() -> seen);
        show(() -> ageOf("zed").or(() -> ageOf("bob")));
        show(() -> Stream.of("ann", "zed", "bob").map(OptionalBasics::ageOf).flatMap(Optional::stream).toList());   // the empty one drops out
        note("toList() is unmodifiable: the type column shows its internal class ImmutableCollections$ListN");

        section("A chain instead of nested null checks");
        show(() -> cityOrUnknown(ann));
        show(() -> cityOrUnknown(bob));                                 // no address
        show(() -> cityOrUnknown(cy));                                  // no city
        show(() -> cityOrUnknown(null));                                // no person at all
        show(() -> cityOrUnknownChecks(null));                          // the null-check version agrees, with three && checks
        note("Optional is for RETURN values. Misuses (fields, parameters, get()): next file OptionalTraps");
    }
}
