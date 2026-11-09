package review.objects;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

/**
 * 12f. SINGLETONS and OBJECT IDENTITY: eager, holder, enum; == on objects without equals
 *      Run: mvn -q compile exec:java -Dmain.class=review.objects.SingletonsAndIdentity
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class SingletonsAndIdentity {

    static final List<String> LOG = new ArrayList<>();

    /** Eager: the instance is created when the class is initialized. */
    static class Eager {
        private static final Eager INSTANCE = new Eager();

        private Eager() { }

        static Eager get() { return INSTANCE; }
    }

    /** Holder idiom: lazy (the Holder class is initialized on the first get) and thread-safe, with no synchronized. */
    static class Lazy {
        private Lazy() { LOG.add("Lazy created"); }

        private static class Holder {
            static final Lazy INSTANCE = new Lazy();
        }

        static Lazy get() { return Holder.INSTANCE; }
    }

    /** Enum: the shortest singleton. */
    enum Registry {
        INSTANCE;

        private int hits;

        int hit() { return ++hits; }
    }

    static class Plain { }

    /** Try to build a second instance through the private constructor, with reflection. */
    static String attack(Class<?> type) {
        try {
            Constructor<?> constructor = type.getDeclaredConstructors()[0];
            constructor.setAccessible(true);
            Object second = constructor.getParameterCount() == 0 ? constructor.newInstance() : constructor.newInstance("X", 9);
            return second == null ? "nothing" : "second instance created";
        } catch (Exception e) {
            return e.getClass().getSimpleName();
        }
    }

    public static void main(String[] args) {
        title("SINGLETONS AND IDENTITY  -  one instance, and == on plain objects");

        section("Singleton variants");
        syntax(
                "private constructor + static final INSTANCE", "eager: created when the class is initialized",
                "private static class Holder { static final X I = new X(); }", "lazy and thread-safe, because class initialization is (previous file)",
                "enum X { INSTANCE; ... }", "the shortest; safe against serialization and reflection tricks",
                "synchronized / double-checked locking", "lazy without a holder: needs a volatile field to be correct, rarely worth it"
        );
        show(() -> Eager.get() == Eager.get());                      // true: one object
        show(() -> LOG);                                             // []: the Lazy singleton does not exist yet
        Lazy first = Lazy.get();
        Lazy second = Lazy.get();
        show(() -> LOG);                                             // [Lazy created]: built on the first get(), only once
        show(() -> first == second);
        Registry.INSTANCE.hit();
        show(() -> Registry.INSTANCE.hit());                         // 2: the one instance keeps its state
        show(() -> attack(Eager.class));                             // reflection broke the private constructor: a second "singleton"
        show(() -> attack(Registry.class));                          // the JVM refuses to create enum instances reflectively
        note("A singleton is global state in disguise: hard to test and to replace. Often passing one instance around is better (dependency injection)");

        section("== on objects without equals");
        Plain p1 = new Plain();
        Plain p2 = new Plain();
        show(() -> p1 == p2);                                        // false: == compares references (identity)
        show(() -> p1.equals(p2));                                   // false: Object.equals IS ==, until you override it
        show(() -> p1.equals(p1));                                   // true
        note("== on objects asks 'the same object?'; equals asks 'equal value?' and needs an override (EqualsAndHashCode). Strings and Integer caching: lesson 0");
    }
}
