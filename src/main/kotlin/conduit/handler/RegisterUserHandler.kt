package conduit.handler

import conduit.model.*

interface RegisterUserHandler {
    operator fun invoke(newUserInfo: NewUserInfo): RegisteredUserInfo
}

class RegisterUserHandlerImpl: RegisterUserHandler {
    override fun invoke(newUserInfo: NewUserInfo): RegisteredUserInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

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
