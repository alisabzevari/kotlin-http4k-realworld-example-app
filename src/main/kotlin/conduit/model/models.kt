package conduit.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import conduit.util.TokenAuth
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
    val token: Token?,
    val username: Username,
    val bio: Bio?,
    val image: Image?
)

data class NewUser(
    val username: Username,
    val password: Password,
    val email: Email
)

data class UpdateUser(
    val email: Email?,
    val username: Username?,
    val bio: Bio?,
    val image: Image?
)

fun TokenAuth.TokenInfo.extractEmail() = Email(this.claims["email"].toString())