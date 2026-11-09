package review.generics;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

/**
 * 14c. TYPE ERASURE AND WHAT IT FORBIDS: no new T(), no instanceof List<String>, no generic arrays, raw types
 *      Run: mvn -q compile exec:java -Dmain.class=review.generics.ErasureAndLimits
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class ErasureAndLimits {

    /** Field kept only to show that the DECLARATION still carries its type argument. */
    static List<String> declared;

    /** The Class token is the run-time proof of T, so the method can build a T. */
    static <T> T create(Class<T> type) {
        try {
            return type.getDeclaredConstructor().newInstance();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("cannot create " + type.getSimpleName(), e);   // the checked exception is wrapped
        }
    }

    /** The generic type of the field above, as text. */
    static String declaredFieldType() {
        try {
            return ErasureAndLimits.class.getDeclaredField("declared").getGenericType().toString();
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(e);
        }
    }

    /** new T[n] is illegal; Array.newInstance with a Class token builds a real T[]. */
    @SuppressWarnings("unchecked")
    static <T> T[] newArray(Class<T> type, int size) {
        return (T[]) Array.newInstance(type, size);
    }

    /** An unchecked cast: inside the method T is erased to Object, so the cast checks nothing. */
    @SuppressWarnings("unchecked")
    static <T> T unsafeCast(Object o) {
        return (T) o;
    }

    /** T... is an array of the erased type. @SafeVarargs promises the method only READS the array (static, final or private methods only). */
    @SafeVarargs
    static <T> List<T> listOf(T... items) {
        List<T> out = new ArrayList<>();
        for (T item : items) out.add(item);
        return out;
    }

    /** NOT safe: it hands the varargs array out. javac warns "possible heap pollution"; @SafeVarargs here would be a lie. */
    @SuppressWarnings("unchecked")
    static <T> T[] arrayOf(T... items) {
        return items;
    }

    /** Inside pair, T is unknown, so arrayOf(a, b) builds an Object[] and not a String[]. */
    @SuppressWarnings("unchecked")
    static <T> T[] pair(T a, T b) {
        return arrayOf(a, b);
    }

    /** The raw List turns the checks off, so a String list can be given an Integer. javac warns: unchecked call. */
    @SuppressWarnings({"rawtypes", "unchecked"})
    static void pollute(List raw) {
        raw.add(42);
    }

    public static void main(String[] args) {
        title("ERASURE  -  generics exist at compile time, not at run time");

        section("Erasure: one class at run time");
        syntax("List<String> and List<Integer>", "the compiler checks them, then erases both to List (T becomes Object or its bound)");
        List<String> strings = new ArrayList<>();
        List<Integer> ints = new ArrayList<>();
        show(() -> strings.getClass() == ints.getClass());                 // the same class: the type argument is gone
        show(() -> strings.getClass().getSimpleName());
        show(() -> declaredFieldType());          // the DECLARATION keeps it (Gson TypeToken uses this)
        Object any = strings;
        show(() -> any instanceof List<?>);                                // the only List check allowed at run time
        wontCompile("if (any instanceof List<String>) { }", "Object cannot be safely cast to List<String>: the type argument is erased");
        note("Erasure is why Java generics are 'compile-time only' (Kotlin gets T back only with inline + reified)");

        section("What a type parameter cannot do");
        wontCompile("T make() { return new T(); }", "unexpected type: required class, found type parameter T");
        wontCompile("T[] a = new T[10];", "generic array creation");
        wontCompile("List<String>[] a = new List<String>[10];", "generic array creation");
        wontCompile("Class<T> c = T.class;", "cannot select from a type variable");
        wontCompile("static T shared;", "non-static type variable T cannot be referenced from a static context");
        wontCompile("void f(List<String> a) {}  void f(List<Integer> a) {}", "name clash: same erasure");

        section("Workaround: a Class<T> token");
        syntax("static <T> T create(Class<T> type)", "the caller passes String.class, so the method has a run-time T");
        show(() -> create(StringBuilder.class).append("built").toString());   // newInstance through the token, no cast needed
        show(() -> newArray(String.class, 2));                             // Array.newInstance builds a real String[]
        show(() -> thrown(() -> create(Integer.class)));                   // Integer has no no-argument constructor (NoSuchMethodException, wrapped)
        note("Other fix: pass a factory, Supplier<T>: create(StringBuilder::new)");

        section("Arrays are checked at run time, generics are not");
        Object[] objects = new String[1];                                  // arrays are COVARIANT: this compiles
        show(() -> thrown(() -> { objects[0] = 1; return 0; }));           // ArrayStoreException at run time
        Object[] built = pair("a", "b");
        show(() -> built.getClass().getSimpleName());                      // Object[]: inside pair, T is unknown, so it built an Object[]
        show(() -> thrown(() -> { String[] s = pair("a", "b"); return s; }));   // TRAP: the caller's hidden cast to String[] fails
        show(() -> arrayOf("a", "b").getClass().getSimpleName());          // here the call site knows T = String
        show(() -> listOf(1, 2, 3));                                       // @SafeVarargs is a promise: this method never writes into items
        note("Arrays check the element type at run time (store fails), generics do not; so never mix them: List<String>[] is refused");

        section("Raw types and heap pollution");
        syntax("List raw = new ArrayList();", "raw type: generics switched off, only for old code. Never write new code with it");
        pollute(strings);                                                  // an Integer is now inside a List<String>
        show(() -> strings.size());                                        // no error yet: nothing checked the element
        Object peek = strings.get(0);   show(() -> peek);                  // read as Object: still fine
        show(() -> thrown(() -> { String s = strings.get(0); return s; }));   // the compiler's hidden cast to String fails HERE
        show(() -> thrown(() -> { String s = unsafeCast(42); return s; }));   // unchecked cast: fails at the caller, not inside the method
        note("Heap pollution: a List<String> holds a non-String. The ClassCastException appears later, far from the cause");
        note("javac warns 'unchecked' for raw calls and (T) casts: treat that warning as a bug until proven safe");
    }
}
