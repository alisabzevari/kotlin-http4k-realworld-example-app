package conduit.handler

import conduit.model.UpdateUser
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.UserNotFoundException
import conduit.util.TokenAuth

interface UpdateCurrentUserHandler {
    operator fun invoke(tokenInfo: TokenAuth.TokenInfo, updateUser: UpdateUser): UserDto
}

class UpdateCurrentUserHandlerImpl(val database: ConduitDatabase) : UpdateCurrentUserHandler {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo, updateUser: UpdateUser): UserDto {
        val email = tokenInfo.extractEmail()

        val user = database.tx {
            val dbUser = getUser(email) ?: throw UserNotFoundException(email.value)
            updateUser(dbUser.id, updateUser)
            getUser(dbUser.id) ?: throw UserNotFoundException(email.value)
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