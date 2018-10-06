package conduit.handler

import conduit.model.Profile
import conduit.model.Username
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface FollowUserHandler {
    operator fun invoke(username: Username, token: TokenAuth.TokenInfo): Profile
}

class FollowUserHandlerImpl(val repository: ConduitRepository) : FollowUserHandler {
    override fun invoke(username: Username, token: TokenAuth.TokenInfo) =
        repository.followUser(username, token.extractEmail())
}