package review.classes;

import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.title;
import static review.Output.wontCompile;

/**
 * 11f. SEALED TYPES and RECORD PATTERNS: a closed set of kinds, each with its own data
 *      Run: mvn -q compile exec:java -Dmain.class=review.classes.SealedAndPatterns
 *      Or click the green arrow beside `main` in IntelliJ.
 *      (The pattern syntax itself is lesson 8, PatternMatching; here it is used to model data.)
 */
public class SealedAndPatterns {

    sealed interface Payment permits Card, Cash, Pending { }          // permits: the complete list of direct subtypes

    record Card(String last4) implements Payment { }
    record Cash(int amount) implements Payment { }
    record Pending() implements Payment { }                           // a kind with no data

    static String payInfo(Payment p) {
        return switch (p) {                                           // no default: the compiler knows every subtype
            case Card c -> "card ending " + c.last4();
            case Cash(int amount) -> "cash " + amount;                // a record pattern takes the record apart
            case Pending pending -> "waiting";
        };
    }

    sealed interface Expr { }                                         // no permits: the subtypes are in this same file

    record Num(int value) implements Expr { }
    record Add(Expr left, Expr right) implements Expr { }
    record Mul(Expr left, Expr right) implements Expr { }

    static int eval(Expr e) {
        return switch (e) {
            case Num n -> n.value();
            case Add(Expr l, Expr r) -> eval(l) + eval(r);            // nested patterns reach into the tree
            case Mul(Expr l, Expr r) -> eval(l) * eval(r);
        };
    }

    sealed interface Outcome { }

    record Ok(int value) implements Outcome { }
    record Failure(String reason) implements Outcome { }

    static Outcome parseAge(String text) {
        try {
            int n = Integer.parseInt(text);
            return n >= 0 && n <= 150 ? new Ok(n) : new Failure("out of range: " + n);
        } catch (NumberFormatException e) {
            return new Failure("not a number: " + text);
        }
    }

    public static void main(String[] args) {
        title("SEALED  -  exhaustive switch, data-carrying cases");

        section("sealed interface + records");
        syntax(
                "sealed interface S permits A, B { }", "only A and B may implement S; permits may be left out when they are in the same file",
                "record A(...) implements S", "each kind has its OWN fields (a record is implicitly final)",
                "final | sealed | non-sealed", "every permitted subclass must say how far the hierarchy stays closed (records and enums count as final)",
                "switch (s) { case A a -> ...; case B b -> ...; }", "a switch expression over a sealed type needs NO default"
        );
        show(() -> payInfo(new Card("4242")));
        show(() -> payInfo(new Cash(30)));
        show(() -> payInfo(new Pending()));
        show(() -> new Cash(30));                                     // each kind has its own fields and a readable toString
        show(() -> new Pending().equals(new Pending()));              // true: a record with no components is the "data object" of Java
        wontCompile("new Payment();", "Payment is abstract; cannot be instantiated");
        wontCompile("switch (p) { case Card c -> 1; case Cash c -> 2; }", "the switch expression does not cover all possible input values (Pending is missing)");
        wontCompile("record Crypto(String coin) implements Payment { }", "class is not allowed to extend sealed class: Payment (as it is not listed in its 'permits' clause)");
        wontCompile("class Voucher implements Payment { }", "sealed, non-sealed or final modifiers expected");

        section("A sealed class as an expression tree");
        Expr tree = new Add(new Num(2), new Mul(new Num(3), new Num(4)));
        show(() -> tree);
        show(() -> eval(tree));                                       // 2 + 3 * 4

        section("A sealed type as a result: Ok or Failure");
        show(() -> parseAge("42"));
        show(() -> parseAge("x"));
        show(() -> parseAge("200"));
        note("An unchecked exception is invisible in the signature; a sealed Outcome puts both cases in the return type and the caller must handle each");

        section("Enum or sealed?");
        note("enum = a fixed set of VALUES: every constant has the same fields (Level.HIGH); it has valueOf, values(), ordinal, EnumMap");
        note("sealed = a fixed set of KINDS: each kind has its own fields (Card has last4, Cash has amount) and can have many instances");
        note("Sealed beats enum when the cases carry different data; enum beats sealed when you need a name, all values, or a map keyed by it");
        note("A new kind breaks every exhaustive switch at compile time: that is the point (an open interface needs a default branch that swallows the new kind)");
    }
}
