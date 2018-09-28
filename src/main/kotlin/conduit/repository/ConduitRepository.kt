package conduit.repository

import conduit.model.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

interface ConduitRepository {
    fun findUserByEmail(email: Email): User?
}

class ConduitRepositoryImpl(val database: Database) : ConduitRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
        }
    }

    override fun findUserByEmail(email: Email): User? =
        transaction(database) {
            Users
                .select { Users.email.eq(email.value) }
                .firstOrNull()
                ?.toUser()
        }

//    fun insertUser(newUser: NewUser) = transaction {
//        Users.insert {
//            it[email] = newUser.email.value
//            it[username] = newUser.username.value
//            it[password] = newUser.password.value
//        }
//    }
}

fun ResultRow.toUser() = User(
    id = this[Users.id],
    email = Email(this[Users.email]),
    password = Password(this[Users.password]),
    token = null,
    username = Username(this[Users.username]),
    bio = this[Users.bio]?.let(::Bio),
    image = this[Users.image]?.let { Image(it) }
)