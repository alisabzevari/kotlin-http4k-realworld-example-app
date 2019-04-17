package conduit.handler

import conduit.model.Profile
import conduit.model.Username
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface GetProfileHandler {
    operator fun invoke(username: Username, token: TokenAuth.TokenInfo?): Profile
}

class GetProfileHandlerImpl(val txManager: ConduitTxManager) : GetProfileHandler {
    override fun invoke(username: Username, token: TokenAuth.TokenInfo?) =
        txManager.tx {
            val user = getUser(username) ?: throw HttpException(
                Status.NOT_FOUND,
                "User with username ${username.value} not found."
            )
            val currentUserEmail = token?.extractEmail()
            val following = if (currentUserEmail == null) {
                false
            } else {
                val currentUser = getUser(currentUserEmail) ?: throw HttpException(
                    Status.NOT_FOUND,
                    "User with email ${currentUserEmail.value} not found."
                )
                getFollowing(currentUser.id, user.id)
            }

            Profile(
                user.username,
                user.bio,
                user.image,
                following
            )
        }
}