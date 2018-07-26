package conduit.handlers

import conduit.model.*
import conduit.repository.ConduitRepository
import conduit.utils.generateToken
import conduit.utils.hash
import org.http4k.core.Status

class LoginHandler(val repository: ConduitRepository) {
    operator fun invoke(loginInfo: LoginInfo): LoggedInUserInfo {

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