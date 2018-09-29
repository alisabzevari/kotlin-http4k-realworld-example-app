package conduit.handler

import conduit.model.*
import conduit.repository.ConduitRepository
import conduit.util.HttpException
import conduit.util.generateToken
import conduit.util.hash
import org.http4k.core.Status

interface LoginHandler {
    operator fun invoke(loginUserDto: LoginUserDto): UserDto
}

class LoginHandlerImpl(val repository: ConduitRepository) : LoginHandler {
    override operator fun invoke(loginUserDto: LoginUserDto): UserDto {

        val user = repository.findUserByEmail(loginUserDto.email) ?: throw UserNotFoundException(loginUserDto.email.value)

        if (loginUserDto.password.hash() != user.password) {
            throw InvalidUserPassException()
        }

        val token = generateToken(user)

        return UserDto(
            user.email,
            token,
            user.username,
            user.bio,
            user.image
        )
    }
}

class UserNotFoundException(username: String) : HttpException(Status.UNAUTHORIZED, "User $username not found.")
class InvalidUserPassException() : HttpException(Status.UNAUTHORIZED, "Invalid username or password.")

data class LoginUserDto(val email: Email, val password: Password)
data class UserDto(val email: Email, val token: Token, val username: Username, val bio: Bio?, val image: Image?)
