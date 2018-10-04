package conduit.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import conduit.util.TokenAuth
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as m

data class Email @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Password @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Token @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Username @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Bio @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Image @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

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

data class Profile(
    val username: Username,
    val bio: Bio?,
    val image: Image?,
    val following: Boolean
)

fun TokenAuth.TokenInfo.extractEmail() = Email(claims["email"].toString())