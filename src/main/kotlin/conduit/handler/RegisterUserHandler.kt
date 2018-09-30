package conduit.handler

import conduit.model.Email
import conduit.model.NewUser
import conduit.model.Password
import conduit.model.Username
import conduit.repository.ConduitRepository
import conduit.util.HttpException
import conduit.util.generateToken
import conduit.util.hash
import org.http4k.core.Status

interface RegisterUserHandler {
    operator fun invoke(newUserDto: NewUserDto): UserDto
}

class RegisterUserHandlerImpl(val repository: ConduitRepository) : RegisterUserHandler {
    override fun invoke(newUserDto: NewUserDto): UserDto {
        repository.insertUser(
            newUserDto.let {
                NewUser(it.username, it.password.hash(), it.email)
            })

        return UserDto(
            newUserDto.email,
            generateToken(newUserDto.username, newUserDto.email),
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

class UserAlreadyExistsException() : HttpException(Status.CONFLICT, "The specified user already exists.")
