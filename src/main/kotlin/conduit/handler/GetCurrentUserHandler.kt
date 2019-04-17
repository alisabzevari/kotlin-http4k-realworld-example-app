package conduit.handler

import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface GetCurrentUserHandler {
    operator fun invoke(tokenInfo: TokenAuth.TokenInfo): UserDto
}

class GetCurrentUserHandlerImpl(val txManager: ConduitTxManager) : GetCurrentUserHandler {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo): UserDto {
        val email = tokenInfo.extractEmail()
        val user = txManager.tx {
            getUser(email) ?: throw HttpException(Status.NOT_FOUND, "User with email ${email.value} not found.")
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