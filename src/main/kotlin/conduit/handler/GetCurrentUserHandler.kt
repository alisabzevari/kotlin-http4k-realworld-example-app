package conduit.handler

import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.UserNotFoundException
import conduit.util.TokenAuth

interface GetCurrentUserHandler {
    operator fun invoke(tokenInfo: TokenAuth.TokenInfo): UserDto
}

class GetCurrentUserHandlerImpl(val database: ConduitDatabase) : GetCurrentUserHandler {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo): UserDto {
        val email = tokenInfo.extractEmail()
        val user = database.tx {
            getUser(email) ?: throw UserNotFoundException(email.value)
        }

        return UserDto(
            user.email,
            tokenInfo.token,
            user.username,
            user.bio,
            user.image
        )
    }
}