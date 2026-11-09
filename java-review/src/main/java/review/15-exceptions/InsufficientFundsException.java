package review.exceptions;

/** A custom CHECKED exception (extends Exception) that carries data. Shared by the files of this lesson. */
public class InsufficientFundsException extends Exception {

    private final int missing;

    public InsufficientFundsException(int missing) {
        super("missing " + missing);
        this.missing = missing;
    }

    public int getMissing() {
        return missing;
    }
}
