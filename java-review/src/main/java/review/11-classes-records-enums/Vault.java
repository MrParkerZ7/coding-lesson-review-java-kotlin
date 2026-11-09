package review.classes;

/** Model class for the access-modifier demo in ClassBasics: each member has a different visibility. */
public class Vault {
    private final int secret = 42;            // this top-level class only
    int level = 1;                             // no keyword = package-private: the same package
    protected String guard = "guard";          // the same package, plus subclasses in any package
    public String label = "vault";             // everywhere

    public int reveal() { return secret; }     // a class can use its own private members
}
