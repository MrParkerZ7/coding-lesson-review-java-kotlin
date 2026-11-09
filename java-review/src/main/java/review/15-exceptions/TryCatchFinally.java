package review.exceptions;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

import java.util.ArrayList;
import java.util.List;

/**
 * 15c. TRY, CATCH, FINALLY  (every order is recorded in a list and shown)
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.TryCatchFinally
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class TryCatchFinally {

    /** The first matching catch wins, so write the narrow types first. */
    static String rightOrder(Runnable block) {
        try {
            block.run();
            return "ok";
        } catch (NumberFormatException e) {
            return "NumberFormat";
        } catch (IllegalArgumentException e) {
            return "IllegalArgument";
        } catch (RuntimeException e) {
            return "Runtime";
        }
    }

    static String multi(RuntimeException toThrow) {
        try {
            throw toThrow;
        } catch (IllegalStateException | IllegalArgumentException e) {   // multi-catch: one handler, unrelated types only
            return "state-or-argument";
        }
    }

    static String withFinally(List<String> log, boolean fail) {
        try {
            log.add("try");
            if (fail) throw new IllegalStateException("boom");
            return "returned";
        } catch (IllegalStateException e) {
            log.add("catch");
            return "caught";
        } finally {
            log.add("finally");
        }
    }

    static int primitiveReturn() {
        int x = 1;
        try {
            return x;                      // the value 1 is saved HERE
        } finally {
            x = 2;                         // too late for the primitive
        }
    }

    static StringBuilder objectReturn() {
        StringBuilder sb = new StringBuilder("a");
        try {
            return sb;                     // the REFERENCE is saved
        } finally {
            sb.append("-finally");         // the object it points to still changes
        }
    }

    @SuppressWarnings("finally")           // javac -Xlint:finally: "finally clause cannot complete normally"
    static String returnInFinally() {
        try {
            throw new IllegalStateException("lost");
        } finally {
            return "finally wins";         // the exception vanishes without a trace
        }
    }

    @SuppressWarnings("finally")
    static String throwInFinally() {
        try {
            throw new IllegalStateException("original");
        } finally {
            throw new IllegalArgumentException("from finally");   // replaces the original exception
        }
    }

    /** What a block threw: class name and message, or "no exception". */
    static String caught(Runnable block) {
        try {
            block.run();
            return "no exception";
        } catch (RuntimeException e) {
            return e.getClass().getSimpleName() + ": " + e.getMessage();
        }
    }

    public static void main(String[] args) {
        title("TRY / CATCH / FINALLY  -  the order things happen in");

        section("catch: the first match wins");
        syntax(
                "catch (A | B e)", "multi-catch: A and B must NOT be related by inheritance",
                "catch (Narrow e) { } catch (Broad e) { }", "narrow first; the compiler refuses the wrong order"
        );
        show(() -> rightOrder(() -> Integer.parseInt("x")));               // NumberFormatException is an IllegalArgumentException
        show(() -> rightOrder(() -> "x".repeat(-1)));                      // only an IllegalArgumentException
        show(() -> rightOrder(() -> List.of().add(null)));                 // UnsupportedOperationException: only a RuntimeException
        show(() -> rightOrder(() -> Integer.parseInt("7")));               // nothing thrown
        show(() -> multi(new IllegalStateException("s")));                 // one handler for two types
        wontCompile("catch (Exception e) { } catch (IllegalArgumentException e) { }", "exception IllegalArgumentException has already been caught");
        wontCompile("catch (IllegalArgumentException | NumberFormatException e)", "alternatives in a multi-catch cannot be related by subclassing");
        note("Kotlin lets the wrong order compile (dead code); Java makes it an error");

        section("finally always runs, and the return value is fixed before it");
        List<String> ok = new ArrayList<>();
        List<String> bad = new ArrayList<>();
        show(() -> withFinally(ok, false));                                // the return value is kept
        show(() -> ok);                                                    // try, finally: the finally ran after the return statement
        show(() -> withFinally(bad, true));
        show(() -> bad);                                                   // try, catch, finally: always this order
        show(() -> primitiveReturn());                                     // 1, not 2: the value was saved before finally
        show(() -> objectReturn());                                        // a-finally: the saved REFERENCE sees the later change
        note("finally is skipped only if the JVM stops: System.exit, a crash, or an endless loop in the try");

        section("Trap: return or throw inside finally");
        show(() -> returnInFinally());                                     // no exception reaches the caller
        show(() -> caught(() -> throwInFinally()));                        // the original IllegalStateException is gone
        note("Never return or throw from finally: it hides the real error. javac warns only with -Xlint:finally");
        note("Closing resources safely has its own file: TryWithResources");
    }
}
