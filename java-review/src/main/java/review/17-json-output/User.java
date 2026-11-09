package review.json;

import java.util.List;

/** Sample data shared by the files of this lesson. `address` is another record, `zip` inside it may be null. */
public record User(int id, String name, boolean active, double score, List<String> tags, Address address) {

    /** The one object every file of the lesson serializes. */
    public static User sample() {
        return new User(1, "Ann", true, 9.5, List.of("java", "jvm"), new Address("Bangkok", null));
    }
}
