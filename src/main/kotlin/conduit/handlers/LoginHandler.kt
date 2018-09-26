package conduit.handlers

import conduit.model.*
import conduit.repository.ConduitRepository
import conduit.utils.generateToken
import conduit.utils.hash
import org.http4k.core.Status

interface LoginHandler {
    operator fun invoke(loginInfo: LoginInfo): LoggedInUserInfo
}

class LoginHandlerImpl(val repository: ConduitRepository) : LoginHandler {
    override operator fun invoke(loginInfo: LoginInfo): LoggedInUserInfo {

        val user = repository.findUserByEmail(loginInfo.email) ?: throw UserNotFoundException(loginInfo.email.value)

        // TODO: Try to use code from ktor
        if (loginInfo.password.hash() != user.password) {
            throw InvalidUserPassException()
        }

        val token = generateToken(user)

        return LoggedInUserInfo(
            user.email,
            token,
            user.username,
            user.bio,
            user.image
        )
    }
}

class UserNotFoundException(username: String) : HttpException(Status.NOT_FOUND, "User $username not found.")
class InvalidUserPassException() : HttpException(Status.BAD_REQUEST, "Invalid username or password.")

data class LoginInfo(val email: Email, val password: Password)
data class LoggedInUserInfo(val email: Email, val token: Token, val username: Username, val bio: Bio?, val image: Image?)
