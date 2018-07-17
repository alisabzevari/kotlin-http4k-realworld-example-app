package conduit.model

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as m

data class Email @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Password @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Token @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Username @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Bio @JsonCreator(mode = m) constructor(@JsonValue val value: String)
data class Image @JsonCreator(mode = m) constructor(@JsonValue val value: String)

data class LoginInfo(val email: Email, val password: Password)

data class LoggedinUserInfo(val email: Email, val token: Token, val username: Username, val bio: Bio, val image: Image)

data class LoginRequest(val user: LoginInfo)

data class LoginResponse(val user: LoggedinUserInfo)