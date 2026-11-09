package review.json

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import review.block
import review.note
import review.section
import review.show
import review.syntax
import review.title

/** A Boolean property whose name starts with "is": the three libraries disagree about its JSON name. */
@Serializable
data class Switch(val isOn: Boolean)

/**
 * 21d. JSON WITH A LIBRARY  (Jackson, Gson, kotlinx.serialization)
 *      Run: mvn -q compile exec:java -Dmain.class=review.json.LibraryJsonKt
 *      Or click the green arrow beside `fun main()` in IntelliJ.
 */
fun main() {
    title("JSON WITH A LIBRARY  -  Jackson, Gson, kotlinx.serialization")

    val user = sampleUser()

    section("Jackson  (Spring Boot uses it)")
    syntax(
        "val mapper = jacksonObjectMapper()" to "the Kotlin module understands data classes and non-null types",
        "mapper.writeValueAsString(obj)" to "object -> JSON text",
        "mapper.readValue<T>(text)" to "JSON text -> object",
    )
    val mapper = jacksonObjectMapper()
    show { mapper.writeValueAsString(user) }                                   // null fields ARE written: "zip":null
    block(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(user))
    val jacksonText = mapper.writeValueAsString(user)
    show { mapper.readValue<User>(jacksonText) == user }                       // round trip: data class equals

    section("Gson  (Google)")
    syntax(
        "val gson = Gson()  |  GsonBuilder().setPrettyPrinting().create()" to "plain / pretty",
        "gson.toJson(obj)" to "object -> JSON text",
        "gson.fromJson(text, T::class.java)" to "JSON text -> object",
    )
    val gson = Gson()
    show { gson.toJson(user) }                                                 // null fields are LEFT OUT: no "zip" key
    block(GsonBuilder().setPrettyPrinting().create().toJson(user))
    val gsonText = gson.toJson(user)
    show { gson.fromJson(gsonText, User::class.java) == user }
    val partial = """{"id":1}"""
    show { gson.fromJson(partial, User::class.java).name }                     // TRAP: name is a non-null String but Gson stored null

    section("kotlinx.serialization  (JetBrains, needs @Serializable)")
    syntax(
        "@Serializable data class User(...)" to "the compiler plugin writes the serializer: no reflection",
        "Json.encodeToString(obj)" to "object -> JSON text",
        "Json.decodeFromString<T>(text)" to "JSON text -> object",
        "Json { prettyPrint = true }" to "configure once, reuse the instance",
    )
    show { Json.encodeToString(user) }                                         // default values are LEFT OUT: zip = null is the default
    show { Json { encodeDefaults = true }.encodeToString(user) }               // ask for them and "zip":null appears
    block(Json { prettyPrint = true }.encodeToString(user))
    val kxText = Json.encodeToString(user)
    show { Json.decodeFromString<User>(kxText) == user }

    section("Trap: a Boolean property named isOn, and the Kotlin module")
    show { jacksonObjectMapper().writeValueAsString(Switch(true)) }            // with the Kotlin module: isOn
    show { Gson().toJson(Switch(true)) }                                       // Gson reads the field: isOn
    show { Json.encodeToString(Switch(true)) }                                 // kotlinx reads the property: isOn
    show { ObjectMapper().writeValueAsString(Switch(true)) }                   // plain Jackson, NO Kotlin module: reads getter isOn(), so the key becomes on
    show { runCatching { ObjectMapper().readValue(jacksonText, User::class.java) }.exceptionOrNull()?.javaClass?.simpleName }   // plain Jackson cannot build a data class (no default constructor)

    note("Null fields: Jackson writes them, Gson drops them, kotlinx drops DEFAULT values. Check what the other side expects")
    note("Gson builds objects without calling the constructor, so Kotlin null checks do not run (see the name above)")
    note("In an interview: say which library you would use in a real service (Jackson with Spring), then write the hand-made one if none is allowed")
}
