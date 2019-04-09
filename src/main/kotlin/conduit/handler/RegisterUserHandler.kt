package conduit.handler

import conduit.model.Email
import conduit.model.NewUser
import conduit.model.Password
import conduit.model.Username
import conduit.repository.ConduitDatabase
import conduit.repository.OldRepo
import conduit.util.generateToken
import conduit.util.hash

interface RegisterUserHandler {
    operator fun invoke(newUserDto: NewUserDto): UserDto
}

class RegisterUserHandlerImpl(val database: ConduitDatabase) : RegisterUserHandler {
    override fun invoke(newUserDto: NewUserDto): UserDto {
        database.tx {
            insertUser(newUserDto.let {
                NewUser(it.username, it.password.hash(), it.email)
            })
        }
        return UserDto(
            newUserDto.email,
            generateToken(newUserDto.username, newUserDto.email),
            newUserDto.username,
            null,
            null
        )
    }
}

data class NewUserDto(
    val username: Username,
    val password: Password,
    val email: Email
)

