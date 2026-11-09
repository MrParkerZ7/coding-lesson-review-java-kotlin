package review.json;

import static review.Output.block;
import static review.Output.note;
import static review.Output.section;
import static review.Output.show;
import static review.Output.syntax;
import static review.Output.thrown;
import static review.Output.title;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/**
 * 17d. JSON WITH A LIBRARY  (Jackson and Gson)
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.LibraryJson
 *      Or click the green arrow beside `main` in IntelliJ.
 */
public class LibraryJson {

    /** A JavaBean-style class: field isOn, getter isOn(). The two libraries read different things. */
    static class Switch {
        private final boolean isOn;

        Switch(boolean isOn) {
            this.isOn = isOn;
        }

        public boolean isOn() {
            return isOn;
        }
    }

    /** The same data as a record: both libraries use the record component name. */
    public record Flag(boolean isOn) {
    }

    /** A record that checks itself: both libraries build records through the constructor, so the check runs. */
    public record Strict(int id, String name) {
        public Strict {
            Objects.requireNonNull(name, "name is required");
        }
    }

    public static void main(String[] args) throws Exception {
        title("JSON WITH A LIBRARY  -  Jackson and Gson");

        User user = User.sample();

        section("Jackson  (Spring Boot uses it)");
        syntax(
                "ObjectMapper mapper = new ObjectMapper();", "create once, reuse (it is thread-safe once configured)",
                "mapper.writeValueAsString(obj)", "object -> JSON text",
                "mapper.readValue(text, Type.class)", "JSON text -> object (records work in Jackson 2.23)",
                "mapper.writerWithDefaultPrettyPrinter()", "indented output"
        );
        ObjectMapper mapper = new ObjectMapper();
        String jacksonText = mapper.writeValueAsString(user);   show(() -> jacksonText);   // null fields ARE written: "zip":null
        block(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user));          // note the style: "key" : value, short arrays on one line
        boolean jacksonSame = mapper.readValue(jacksonText, User.class).equals(user);   show(() -> jacksonSame);   // round trip: record equals
        String unknown = "{\"id\":1,\"extra\":2}";
        show(() -> thrown(() -> mapper.readValue(unknown, User.class)));   // TRAP: an unknown key FAILS by default
        ObjectMapper skipNulls = new ObjectMapper().setDefaultPropertyInclusion(JsonInclude.Include.NON_NULL);
        String withoutNulls = skipNulls.writeValueAsString(user);   show(() -> withoutNulls);   // one setting drops the nulls

        section("Gson  (Google)");
        syntax(
                "Gson gson = new Gson();  |  new GsonBuilder().setPrettyPrinting().create()", "plain | pretty",
                "gson.toJson(obj)", "object -> JSON text",
                "gson.fromJson(text, Type.class)", "JSON text -> object (records work in Gson 2.15)"
        );
        Gson gson = new Gson();
        String gsonText = gson.toJson(user);   show(() -> gsonText);       // null fields are LEFT OUT: no "zip" key
        block(new GsonBuilder().setPrettyPrinting().create().toJson(user));
        boolean gsonSame = gson.fromJson(gsonText, User.class).equals(user);   show(() -> gsonSame);   // zip is null again, so equals holds
        String withNulls = new GsonBuilder().serializeNulls().create().toJson(user);   show(() -> withNulls);   // ask for them and "zip":null appears
        User ignoredExtra = gson.fromJson(unknown, User.class);   show(() -> ignoredExtra);   // TRAP: Gson ignores the unknown key, the rest stays null / 0
        User partialJackson = mapper.readValue("{\"id\":1}", User.class);   show(() -> partialJackson);   // a missing key is not an error in either library

        section("Generics: the type must travel with you (see lesson 14, erasure)");
        syntax(
                "mapper.readValue(text, new TypeReference<List<User>>() {})", "an anonymous subclass keeps the generic type in its signature",
                "gson.fromJson(text, new TypeToken<List<User>>() {}.getType())", "the same trick in Gson"
        );
        String listText = mapper.writeValueAsString(List.of(user, user));
        List<?> erased = mapper.readValue(listText, List.class);
        show(() -> erased.get(0).getClass().getSimpleName());              // TRAP: List.class has no User in it, so you get maps
        List<User> typed = mapper.readValue(listText, new TypeReference<List<User>>() { });
        show(() -> typed.get(0).getClass().getSimpleName());
        Type listOfUser = new TypeToken<List<User>>() { }.getType();
        List<User> gsonTyped = gson.fromJson(gson.toJson(List.of(user, user)), listOfUser);
        show(() -> gsonTyped.get(1).equals(user));

        section("Trap: a boolean named isOn, and dates");
        String switchByJackson = mapper.writeValueAsString(new Switch(true));   show(() -> switchByJackson);   // Jackson reads the getter isOn(): the key becomes on
        show(() -> gson.toJson(new Switch(true)));                         // Gson reads the field: isOn
        String flagByJackson = mapper.writeValueAsString(new Flag(true));   show(() -> flagByJackson);   // a record: the component name isOn, in both
        show(() -> gson.toJson(new Flag(true)));
        show(() -> thrown(() -> mapper.writeValueAsString(LocalDate.of(2026, 11, 9))));   // Jackson needs the jsr310 module for java.time
        show(() -> gson.toJson(LocalDate.of(2026, 11, 9)));                // Gson 2.15 writes its fields as an object

        note("Null fields: Jackson writes them, Gson drops them. Unknown keys: Jackson fails, Gson ignores. Check what the other side expects");
        section("Required fields: validate in the record");
        show(() -> thrown(() -> mapper.readValue("{\"id\":1}", Strict.class)));   // Jackson: the constructor's NPE, wrapped
        show(() -> thrown(() -> gson.fromJson("{\"id\":1}", Strict.class)));      // Gson: also runs the constructor, wraps the NPE in a RuntimeException
        Strict ok = gson.fromJson("{\"id\":1,\"name\":\"x\"}", Strict.class);   show(() -> ok);
        note("Neither library checks required fields by itself (see partialJackson above): put the check in the compact constructor");
        note("In an interview: say which library you would use in a real service (Jackson with Spring), then write the hand-made one if none is allowed");
    }
}
