package conduit.handler

import conduit.model.*

interface RegisterUserHandler {
    operator fun invoke(newUserInfo: NewUserInfo): RegisteredUserInfo
}

// TODO: RegisterUserHandlerImpl

data class NewUserInfo(
    val username: Username,
    val password: Password,
    val email: Email
)

data class RegisteredUserInfo(
    val email: Email,
    val token: Token,
    val username: Username,
    val bio: Bio?,
    val image: Image?
)
