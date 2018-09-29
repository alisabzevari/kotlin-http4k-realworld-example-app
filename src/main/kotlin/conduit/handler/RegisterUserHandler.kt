package conduit.handler

import conduit.model.*

interface RegisterUserHandler {
    operator fun invoke(newUserDto: NewUserDto): UserDto
}

class RegisterUserHandlerImpl: RegisterUserHandler {
    override fun invoke(newUserDto: NewUserDto): UserDto {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}

data class NewUserDto(
    val username: Username,
    val password: Password,
    val email: Email
)
