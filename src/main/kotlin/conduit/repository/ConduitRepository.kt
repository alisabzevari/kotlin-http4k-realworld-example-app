package conduit.repository

import conduit.handler.MultipleArticles
import conduit.model.*
import conduit.util.HttpException
import org.http4k.core.Status
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface ConduitRepository {
    fun findUserByEmail(email: Email): User?
    fun insertUser(newUser: NewUser)
    fun updateUser(email: Email, user: UpdateUser): User
    fun getProfile(username: Username, currentUserEmail: Email?): Profile
    fun followUser(userToFollow: Username, followerEmail: Email): Profile
    fun unfollowUser(userToFollow: Username, followerEmail: Email): Profile
    fun getArticlesFeed(email: Email, offset: Int, limit: Int): MultipleArticles
}

class ConduitRepositoryImpl(private val database: Database) : ConduitRepository {
    init {
        transaction(database) {
            SchemaUtils.create(Users)
            SchemaUtils.create(Following)
            SchemaUtils.create(Articles)
            SchemaUtils.create(Tags)
            SchemaUtils.create(Favorites)
            SchemaUtils.create(Comments)
        }
    }

    private fun byEmail(email: Email) = Users.email eq email.value
    private fun byUsername(username: Username): Op<Boolean> = Users.username eq username.value
    private fun byUserId(id: Int) = Users.id eq id

    private fun getUser(selector: Op<Boolean>) = Users.select { selector }.firstOrNull()?.toUser()

    override fun findUserByEmail(email: Email): User? = transaction(database) { getUser(byEmail(email)) }

    override fun insertUser(newUser: NewUser) {
        transaction(database) {
            val alreadyExists = Users.select { Users.email eq newUser.email.value }.firstOrNull() != null
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
            val dbUser = getUser(byEmail(email)) ?: throw UserNotFoundException(email.value)

            Users.update({ Users.id eq dbUser.id }) {
                if (user.bio != null) it[this.bio] = user.bio.value
                if (user.email != null) it[this.email] = user.email.value
                if (user.image != null) it[this.image] = user.image.value
                if (user.username != null) it[this.username] = user.username.value
            }

            Users.select { Users.id eq dbUser.id }.first().toUser()
        }

    override fun getProfile(username: Username, currentUserEmail: Email?) =
        getProfileBy(byUsername(username), currentUserEmail) ?: throw UserNotFoundException(username.value)

    private fun getProfileBy(userSelector: Op<Boolean>, currentUserEmail: Email?) =
        transaction(database) {
            val userProfile = Users.select { userSelector }.firstOrNull()?.toUser()
            if (userProfile == null) {
                null
            } else {
                val following = if (currentUserEmail == null) {
                    false
                } else {
                    val targetUser = (Users.select { Users.email eq currentUserEmail.value }.firstOrNull()
                            ?: throw UserNotFoundException(currentUserEmail.value)).toUser()
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

    override fun followUser(userToFollow: Username, followerEmail: Email) =
        transaction(database) {
            val targetUser = getUser(byUsername(userToFollow)) ?: throw UserNotFoundException(userToFollow.value)
            val sourceUser = getUser(byEmail(followerEmail)) ?: throw UserNotFoundException(followerEmail.value)

            val isFollowing =
                Following.select { (Following.sourceId eq sourceUser.id) and (Following.targetId eq targetUser.id) }
                    .any()
            if (!isFollowing) {
                Following.insert {
                    it[sourceId] = sourceUser.id
                    it[targetId] = targetUser.id
                }
            }

            Profile(
                targetUser.username,
                targetUser.bio,
                targetUser.image,
                true
            )
        }

    override fun unfollowUser(userToFollow: Username, followerEmail: Email) =
        transaction(database) {
            val targetUser = getUser(byUsername(userToFollow)) ?: throw UserNotFoundException(userToFollow.value)
            val sourceUser = getUser(byEmail(followerEmail)) ?: throw UserNotFoundException(followerEmail.value)

            val isFollowing =
                Following.select { (Following.sourceId eq sourceUser.id) and (Following.targetId eq targetUser.id) }
                    .any()
            if (isFollowing) {
                Following.deleteWhere { (Following.sourceId eq sourceUser.id) and (Following.targetId eq targetUser.id) }
            }

            Profile(
                targetUser.username,
                targetUser.bio,
                targetUser.image,
                false
            )
        }

    override fun getArticlesFeed(email: Email, offset: Int, limit: Int): MultipleArticles = transaction(database) {
        val user = (Users.select { Users.email eq email.value }.firstOrNull()
                ?: throw UserNotFoundException(email.value)).toUser()
        val followedUsers = Following.select { Following.sourceId eq user.id }.map { it.toUser() }
        val articlesCount = Articles.select { Articles.authorId inList followedUsers.map { it.id } }.count()
        val articles = Articles.select { Articles.authorId inList followedUsers.map { it.id } }
            .orderBy(Articles.createdAt, false)
            .limit(limit, offset)
            .toList()
        val articleTags =
            articles
                .map { article ->
                    article[Articles.id] to Tags.select {
                        Tags.articleId eq article[Articles.id]
                    }.map { ArticleTag(it[Tags.tag]) }
                }
                .toMap()
        val articleAuthorProfiles = articles.map { article ->
            article[Articles.id] to getProfileBy(
                byUserId(article[Articles.authorId]),
                email
            )
        }.toMap()

        val favoritesCount = articles.map { article ->
            article[Articles.id] to Favorites.select { Favorites.articleId eq article[Articles.id] }.count()
        }.toMap()
        val favorited = articles.map { article ->
            article[Articles.id] to (Favorites.select { (Favorites.articleId eq article[Articles.id]) and (Favorites.userId eq user.id) }.count() == 1)
        }.toMap()

        val resultArticles = articles.map {
            Article(
                ArticleSlug(it[Articles.slug]),
                ArticleTitle(it[Articles.title]),
                ArticleDescription(it[Articles.description]),
                ArticleBody(it[Articles.body]),
                articleTags[it[Articles.id]] ?: emptyList(),
                it[Articles.createdAt].toInstant(),
                it[Articles.updatedAt].toInstant(),
                favorited[it[Articles.id]] ?: false,
                favoritesCount[it[Articles.id]] ?: 0,
                articleAuthorProfiles[it[Articles.id]] ?: throw Error("Article ${it[Articles.id]} doesn't have Author.")
            )
        }

        MultipleArticles(resultArticles, articlesCount)
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

class UserAlreadyExistsException : HttpException(Status.CONFLICT, "The specified user already exists.")
class UserNotFoundException(usernameOrEmail: String) :
    HttpException(Status.BAD_REQUEST, "User $usernameOrEmail does not exist.")