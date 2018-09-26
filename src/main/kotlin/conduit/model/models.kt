package conduit.model

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator
import org.http4k.core.Status
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as m

data class Email @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Password @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Token @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Username @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Bio @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Image @JsonCreator(mode = m) constructor(@JsonValue val value: String)

data class User(
    val id: Int,
    val email: Email,
    val password: Password,
    val token: Token,
    val username: Username,
    val bio: Bio?,
    val image: Image?
)

data class NewUser(
    val username: Username,
    val password: Password,
    val email: Email
)

open class HttpException(val status: Status, message: String) : RuntimeException(message)