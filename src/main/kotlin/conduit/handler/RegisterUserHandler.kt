package conduit.handler

import conduit.model.Email
import conduit.model.NewUser
import conduit.model.Password
import conduit.model.Username
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.JWT
import conduit.util.hash
import org.http4k.core.Status

interface RegisterUserHandler {
    operator fun invoke(newUserDto: NewUserDto): UserDto
}

class RegisterUserHandlerImpl(
    val txManager: ConduitTxManager,
    private val jwt: JWT
) : RegisterUserHandler {
    override fun invoke(newUserDto: NewUserDto): UserDto {
        txManager.tx {
            val user = getUser(newUserDto.username) ?: getUser(newUserDto.email)
            if (user != null) {
                throw HttpException(Status.CONFLICT, "The specified user already exists.")
            }
            insertUser(newUserDto.let {
                NewUser(it.username, it.password.hash(), it.email)
            })
        }
        return UserDto(
            newUserDto.email,
            jwt.generate(newUserDto.username, newUserDto.email),
            newUserDto.username,
            null,
            null
        )
    }
}

data class NewUserDto(
    val username: Username,
    val password: Password,
    val email: Email
)

