package conduit.repository

import conduit.handler.UserAlreadyExistsException
import conduit.handler.UserNotFoundException
import conduit.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

interface ConduitRepository {
    fun findUserByEmail(email: Email): User?
    fun insertUser(newUser: NewUser)
    fun updateUser(email: Email, user: UpdateUser): User
    fun getProfile(username: Username, currentUserEmail: Email?): Profile
}

class ConduitRepositoryImpl(private val database: Database) : ConduitRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(Following)
        }
    }

    override fun findUserByEmail(email: Email): User? =
        transaction(database) {
            Users
                .select { Users.email eq email.value }
                .firstOrNull()
                ?.toUser()
        }

    override fun insertUser(newUser: NewUser) {
        transaction(database) {
            val alreadyExists =
                Users.select { Users.email eq newUser.email.value }
                    .firstOrNull() != null
            if (alreadyExists) throw UserAlreadyExistsException()

            Users.insert {
                it[email] = newUser.email.value
                it[username] = newUser.username.value
                it[password] = newUser.password.value
                it[bio] = ""
                it[image] = null
            }
        }
    }

    override fun updateUser(email: Email, user: UpdateUser): User =
        transaction(database) {
            val dbUser =
                (Users.select { Users.email eq email.value }.firstOrNull() ?: throw UserNotFoundException(email.value))
                    .toUser()

            Users.update({ Users.id eq dbUser.id }) {
                if (user.bio != null) it[this.bio] = user.bio.value
                if (user.email != null) it[this.email] = user.email.value
                if (user.image != null) it[this.image] = user.image.value
                if (user.username != null) it[this.username] = user.username.value
            }

            Users.select { Users.id eq dbUser.id }.first().toUser()
        }

    override fun getProfile(username: Username, currentUserEmail: Email?) =
        transaction(database) {
            val userProfile = (Users.select { Users.username eq username.value }.firstOrNull()
                    ?: throw UserNotFoundException(username.value)).toUser()
            val following = if (currentUserEmail == null) {
                false
            } else {
                val targetUser = (Users.select { Users.email eq currentUserEmail.value }.firstOrNull()
                        ?: throw UserNotFoundException(username.value)).toUser()
                Following.select { (Following.sourceId eq userProfile.id) and (Following.targetId eq targetUser.id) }
                    .any()
            }

            Profile(
                userProfile.username,
                userProfile.bio,
                userProfile.image,
                following
            )
        }
}

fun ResultRow.toUser() = User(
    id = this[Users.id],
    email = Email(this[Users.email]),
    password = Password(this[Users.password]),
    token = null,
    username = Username(this[Users.username]),
    bio = this[Users.bio]?.let(::Bio),
    image = this[Users.image]?.let(::Image)
)

