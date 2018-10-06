package conduit.handler

import conduit.model.Profile
import conduit.model.Username
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface UnfollowUserHandler {
    operator fun invoke(username: Username, token: TokenAuth.TokenInfo): Profile
}

class UnfollowUserHandlerImpl(val repository: ConduitRepository) : UnfollowUserHandler {
    override fun invoke(username: Username, token: TokenAuth.TokenInfo) =
        repository.unfollowUser(username, token.extractEmail())
}