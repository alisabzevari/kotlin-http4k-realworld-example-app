package conduit.handler

import conduit.model.Profile
import conduit.model.Username
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.UserNotFoundException
import conduit.util.TokenAuth

interface GetProfileHandler {
    operator fun invoke(username: Username, token: TokenAuth.TokenInfo?): Profile
}

class GetProfileHandlerImpl(val database: ConduitDatabase) : GetProfileHandler {
    override fun invoke(username: Username, token: TokenAuth.TokenInfo?) =
        database.tx {
            val user = getUser(username) ?: throw UserNotFoundException(username.value)
            val currentUserEmail = token?.extractEmail()
            val following = if (currentUserEmail == null) {
                false
            } else {
                val currentUser = getUser(currentUserEmail) ?: throw UserNotFoundException(currentUserEmail.value)
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