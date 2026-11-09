package review.json;

import static review.Output.block;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.json.HandWrittenJson.toJson;
import static review.json.HandWrittenJson.toPrettyJson;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 17c. JSON FROM ANY OBJECT BY REFLECTION  (no library)
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.ReflectionJson
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ReflectionJson {

    /** A class with a value that is computed, not stored: reflection over FIELDS cannot see it. */
    static class Rect {
        private final int w;
        private final int h;

        Rect(int w, int h) {
            this.w = w;
            this.h = h;
        }

        int area() {
            return w * h;
        }
    }

    /** static fields belong to the class, transient fields are marked 'not part of the state': both are skipped. */
    static class Account {
        static int created = 0;
        private final String owner;
        private final transient String password;

        Account(String owner, String password) {
            this.owner = owner;
            this.password = password;
        }
    }

    static class Base {
        int id = 7;
    }

    static class Child extends Base {
        String name = "kid";
    }

    /** A non-static inner class that uses its outer object keeps a hidden synthetic field this$0 pointing at it (javac 18+ omits it when the outer object is never used). */
    static class Outer {
        String label = "outer";

        class Inner {
            int v = 5;

            String describe() {
                return label + v;                                          // uses the outer object, so javac keeps this$0
            }
        }
    }

    /** A node that points back at itself: a cycle. */
    static class Node {
        String name;
        Node next;

        Node(String name) {
            this.name = name;
        }
    }

    /** Any value to a tree of Maps, Lists and plain values (what toJson understands). Plain objects are read field by field. */
    public static Object toTree(Object value) {
        return switch (value) {
            case null -> null;
            case Boolean b -> b;
            case Number n -> n;
            case String s -> s;
            case Character c -> c;
            case Enum<?> e -> e.name();
            case Map<?, ?> m -> {
                Map<String, Object> out = new LinkedHashMap<>();
                for (Map.Entry<?, ?> entry : m.entrySet()) out.put(String.valueOf(entry.getKey()), toTree(entry.getValue()));
                yield out;
            }
            case Iterable<?> items -> {
                List<Object> out = new ArrayList<>();
                for (Object item : items) out.add(toTree(item));
                yield out;
            }
            case Object[] array -> toTree(Arrays.asList(array));
            default -> objectToMap(value);
        };
    }

    /** Records: the components, in declaration order. Plain objects: every instance field of the class and its parents. */
    public static Map<String, Object> objectToMap(Object obj) {
        Map<String, Object> result = new LinkedHashMap<>();
        try {
            if (obj.getClass().isRecord()) {
                for (RecordComponent component : obj.getClass().getRecordComponents()) {
                    Method accessor = component.getAccessor();
                    accessor.setAccessible(true);
                    result.put(component.getName(), toTree(accessor.invoke(obj)));
                }
                return result;
            }
            Deque<Class<?>> chain = new ArrayDeque<>();                    // getDeclaredFields sees ONE class: walk up to the parents
            for (Class<?> c = obj.getClass(); c != null && c != Object.class; c = c.getSuperclass()) chain.addFirst(c);
            for (Class<?> c : chain) {
                for (Field field : c.getDeclaredFields()) {
                    int modifiers = field.getModifiers();
                    if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers) || field.isSynthetic()) continue;
                    field.setAccessible(true);                             // the fields are private
                    result.put(field.getName(), toTree(field.get(obj)));
                }
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(e);
        }
        return result;
    }

    public static void main(String[] args) {
        title("JSON BY REFLECTION  -  one function for every class");

        section("Idea: object -> Map -> toJson");
        syntax(
                "obj.getClass().isRecord() | getRecordComponents()", "a record's components, always in DECLARATION order",
                "obj.getClass().getDeclaredFields()", "every field the class itself declares, private ones too",
                "field.setAccessible(true)  |  field.get(obj)", "needed to read a private field | the value; recurse into it if it is an object"
        );
        User user = User.sample();
        show(() -> new ArrayList<>(objectToMap(user).keySet()));           // the component names, in declaration order
        show(() -> toJson(toTree(user)));                                  // one function for any record or class
        show(() -> toJson(toTree(user)).equals(toJson(HandWrittenJson.toMap(user))));   // the same text as the hand-written User map
        show(() -> toJson(toTree(Arrays.asList(user.address(), null))));   // lists of objects work, null stays null
        block(toPrettyJson(toTree(user), 0));

        section("Plain classes: what is and is not a field");
        show(() -> toJson(toTree(new Rect(3, 4))));                        // area is missing: it is a method, not a field
        show(() -> new Rect(3, 4).area());                                 // ... although the value exists
        show(() -> toJson(toTree(new Account("ann", "secret"))));          // static created and transient password are skipped
        show(() -> Child.class.getDeclaredFields().length);                // TRAP: one field, name: getDeclaredFields ignores the parent
        show(() -> toJson(toTree(new Child())));                           // the walk up the superclass chain finds id too
        Outer.Inner inner = new Outer().new Inner();
        show(() -> Arrays.stream(inner.getClass().getDeclaredFields()).map(Field::getName).toList());   // v and the hidden this$0
        show(() -> toJson(toTree(inner)));                                 // this$0 is synthetic: skipped, or it would walk into the outer object
        note("A record needs no tricks: its components are the data, in the order you wrote them");

        section("Traps");
        show(() -> thrown(() -> toTree(LocalDate.of(2026, 11, 9))));       // JDK classes are sealed off since Java 17 (java.base is not opened)
        Node loop = new Node("a");
        loop.next = loop;
        show(() -> thrown(() -> toTree(loop)));                            // a cycle recurses forever: an Error, not an Exception
        note("Field order from getDeclaredFields comes from the JVM: in practice declaration order, but the API does not promise it");
        note("Libraries add what this walker lacks: annotations (@JsonIgnore in Jackson, @Expose in Gson), date formats, naming rules");
        note("Reflection is a trick for the interview board: in real code use a library (LibraryJson)");
    }
}
