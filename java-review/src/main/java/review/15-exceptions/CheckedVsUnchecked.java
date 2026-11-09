package review.exceptions;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;
import static review.Output.wontCompile;

import java.io.IOException;

/**
 * 15a. CHECKED vs UNCHECKED: the exception tree, throws, custom exceptions, wrapping with a cause
 *      Run: mvn -q compile exec:java -Dmain.class=review.exceptions.CheckedVsUnchecked
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class CheckedVsUnchecked {

    /** A custom UNCHECKED exception: extends RuntimeException, so callers are not forced to handle it. */
    static class InvalidOrderException extends RuntimeException {
        InvalidOrderException(String message) {
            super(message);
        }
    }

    /** Checked: the signature must say so with `throws`. */
    static int withdraw(int balance, int amount) throws InsufficientFundsException {
        if (amount > balance) throw new InsufficientFundsException(amount - balance);
        return balance - amount;
    }

    static int checkQuantity(int quantity) {
        if (quantity <= 0) throw new InvalidOrderException("quantity must be > 0, was " + quantity);   // no throws needed
        return quantity;
    }

    static String describeWithdraw(int balance, int amount) {
        try {
            return "left " + withdraw(balance, amount);
        } catch (InsufficientFundsException e) {
            return "short by " + e.getMissing();
        }
    }

    /** Wrap a checked exception in an unchecked one and keep the original as the CAUSE. */
    static void load() {
        try {
            throw new IOException("disk");
        } catch (IOException e) {
            throw new IllegalStateException("cannot load settings", e);
        }
    }

    public static void main(String[] args) {
        title("CHECKED vs UNCHECKED  -  who has to handle it");

        section("The tree");
        syntax(
                "Throwable", "root: everything you can throw",
                "  Error", "serious JVM trouble (OutOfMemoryError, StackOverflowError): do not catch",
                "  Exception", "CHECKED: the compiler forces catch-or-declare (IOException, SQLException)",
                "    RuntimeException", "UNCHECKED: no forcing (NullPointerException, IllegalArgumentException, ...)"
        );
        show(() -> IOException.class.getSuperclass().getSimpleName());     // IOException is checked: a plain Exception
        show(() -> RuntimeException.class.getSuperclass().getSimpleName());   // RuntimeException IS an Exception, but is exempt from checking
        show(() -> NumberFormatException.class.getSuperclass().getSimpleName());   // catching the parent catches it
        show(() -> StackOverflowError.class.getSuperclass().getSimpleName());   // an Error is not an Exception
        Throwable error = new StackOverflowError();
        show(() -> error instanceof Exception);                            // so catch (Exception e) does NOT stop it
        wontCompile("new StackOverflowError() instanceof Exception", "StackOverflowError cannot be converted to Exception (when the type is known)");

        section("Checked: catch it or declare it");
        syntax(
                "void m() throws IOException", "declares it: the CALLER must catch or declare it too",
                "try { m(); } catch (IOException e) { ... }", "handles it here"
        );
        wontCompile("void a() { throw new IOException(\"x\"); }", "unreported exception IOException; must be caught or declared to be thrown");
        wontCompile("void b() { io(); }  // io() throws IOException", "unreported exception IOException; must be caught or declared to be thrown");
        wontCompile("try { int x = 1; } catch (IOException e) { }", "exception IOException is never thrown in body of corresponding try statement");
        wontCompile("class B extends A { void f() throws IOException {} }", "f() in B cannot override f() in A: overridden method does not throw IOException");
        note("An override may throw fewer or narrower checked exceptions, never new or broader ones");

        section("Custom exceptions");
        syntax(
                "class MyEx extends Exception { ... }", "checked: callers must handle it (use when they can recover)",
                "class MyEx extends RuntimeException { ... }", "unchecked: for bugs and caller mistakes; the modern default"
        );
        show(() -> describeWithdraw(100, 30));
        show(() -> describeWithdraw(10, 25));                              // the checked exception carried the missing amount
        show(() -> thrown(() -> checkQuantity(0)));                        // unchecked: nothing in the signature, thrown anyway
        show(() -> thrown(() -> withdraw(5, 9)));
        InvalidOrderException order = new InvalidOrderException("empty order");
        show(() -> order.getMessage());                                    // the message set by the constructor

        section("Wrapping: keep the cause");
        syntax("throw new XException(\"what failed\", cause)", "wrap a low-level exception in one that fits your layer");
        IllegalStateException wrapper = captured();
        show(() -> wrapper.getMessage());
        show(() -> wrapper.getCause().getClass().getSimpleName());         // the original is kept as the cause
        show(() -> wrapper.getCause().getMessage());
        show(() -> new RuntimeException(wrapper.getCause()).getMessage()); // TRAP: wrapping only a cause makes the message cause.toString()
        note("Always pass the cause on: a wrapper without it loses the real stack trace and the real reason");
        note("Interview answer: checked = recoverable, forced by the compiler; unchecked = programming error. Spring and most modern code prefer unchecked");
    }

    private static IllegalStateException captured() {
        try {
            load();
            return null;
        } catch (IllegalStateException e) {
            return e;
        }
    }
}
