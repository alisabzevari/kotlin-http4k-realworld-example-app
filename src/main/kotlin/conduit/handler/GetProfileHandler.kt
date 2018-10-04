package conduit.handler

import conduit.model.Profile
import conduit.model.Username
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface GetProfileHandler {
    operator fun invoke(username: Username, token: TokenAuth.TokenInfo?): Profile
}

class GetProfileHandlerImpl(val repository: ConduitRepository) : GetProfileHandler {
    override fun invoke(username: Username, token: TokenAuth.TokenInfo?) =
        repository.getProfile(username, token?.extractEmail())
}