package conduit.handler

import conduit.model.UpdateUser
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface UpdateCurrentUserHandler {
    operator fun invoke(tokenInfo: TokenAuth.TokenInfo, updateUser: UpdateUser): UserDto
}

class UpdateCurrentUserHandlerImpl(val txManager: ConduitTxManager) : UpdateCurrentUserHandler {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo, updateUser: UpdateUser): UserDto {
        val email = tokenInfo.extractEmail()

        val user = txManager.tx {
            val dbUser = getUser(email) ?: throw HttpException(
                Status.NOT_FOUND,
                "User with email ${email.value} not found."
            )
            updateUser(dbUser.id, updateUser)
            getUser(dbUser.id) ?: throw HttpException(
                Status.NOT_FOUND,
                "User with email ${email.value} not found."
            )
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