package conduit.repository

import conduit.handler.MultipleArticles
import conduit.handler.NewArticle
import conduit.handler.NewComment
import conduit.model.*
import conduit.util.HttpException
import org.http4k.core.Status
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

interface ConduitRepository {
    fun findUserByEmail(email: Email): User?
    fun insertUser(newUser: NewUser)
    fun updateUser(email: Email, user: UpdateUser): User
    fun getProfile(username: Username, currentUserEmail: Email?): Profile
    fun followUser(userToFollow: Username, followerEmail: Email): Profile
    fun unfollowUser(userToFollow: Username, followerEmail: Email): Profile
    fun createArticle(newArticle: NewArticle, authorEmail: Email): Article
    fun getArticlesFeed(email: Email, offset: Int, limit: Int): MultipleArticles
    fun getTags(): List<ArticleTag>
    fun createArticleComment(newComment: NewComment, slug: ArticleSlug, currentUserEmail: Email): Comment
    fun createArticleFavorite(slug: ArticleSlug, currentUserEmail: Email): Article
    fun deleteArticleFavorite(slug: ArticleSlug, currentUserEmail: Email): Article
}

class ConduitRepositoryImpl(private val database: Database) : ConduitRepository {
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

    override fun createArticle(newArticle: NewArticle, authorEmail: Email): Article =
        transaction(database) {
            val authorUser = getUser(byEmail(authorEmail)) ?: throw UserNotFoundException(authorEmail.value)

            val id = Articles.insert {
                it[authorId] = authorUser.id
                it[body] = newArticle.body.value
                it[createdAt] = DateTime.now()
                it[description] = newArticle.description.value
                it[slug] = newArticle.title.value.replace(" ", "-")
                it[title] = newArticle.title.value
                it[updatedAt] = DateTime.now()
            }.get(Articles.id)!!

            newArticle.tagList.forEach { tag ->
                Tags.insert {
                    it[Tags.articleId] = id
                    it[Tags.tag] = tag.value
                }
            }

            val articleRow = Articles.select { Articles.id eq id }.single()
            val authorProfile = Profile(authorUser.username, authorUser.bio, authorUser.image, false)

            articleRow.toArticle(
                authorProfile,
                newArticle.tagList,
                false,
                0
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
                it[Articles.createdAt],
                it[Articles.updatedAt],
                favorited[it[Articles.id]] ?: false,
                favoritesCount[it[Articles.id]] ?: 0,
                articleAuthorProfiles[it[Articles.id]] ?: throw Error("Article ${it[Articles.id]} doesn't have Author.")
            )
        }

        MultipleArticles(resultArticles, articlesCount)
    }

    override fun createArticleComment(newComment: NewComment, slug: ArticleSlug, currentUserEmail: Email): Comment =
        transaction(database) {
            val authorUser = getUser(byEmail(currentUserEmail)) ?: throw UserNotFoundException(currentUserEmail.value)

            val article = Articles.select { Articles.slug eq slug.value }.singleOrNull() ?: throw HttpException(
                Status.NOT_FOUND,
                "Article with slug [$slug] not found."
            )

            val commentId = Comments.insert {
                it[authorId] = authorUser.id
                it[body] = newComment.body.value
                it[createdAt] = DateTime.now()
                it[updatedAt] = DateTime.now()
                it[articleId] = article[Articles.id]
            }.get(Comments.id)!!

            Comments.select { Comments.id eq commentId }.single()
                .toComment(Profile(authorUser.username, authorUser.bio, authorUser.image, false))
        }

    override fun getTags() = transaction(database) {
        Tags.selectAll().map { ArticleTag(it[Tags.tag]) }
    }

    override fun createArticleFavorite(slug: ArticleSlug, currentUserEmail: Email): Article =
        transaction(database) {
            val currentUser = getUser(byEmail(currentUserEmail)) ?: throw UserNotFoundException(currentUserEmail.value)
            val article = Articles.select { Articles.slug eq slug.value }.firstOrNull() ?: throw HttpException(
                Status.NOT_FOUND,
                "Article with slug [$slug] not found."
            )
            val favoriteExist =
                Favorites.select { (Favorites.articleId eq article[Articles.id]) and (Favorites.userId eq currentUser.id) }
                    .any()

            if (!favoriteExist) {
                Favorites.insert {
                    it[articleId] = article[Articles.id]
                    it[userId] = currentUser.id
                }
            }

            val favoritesCount = Favorites.select { Favorites.articleId eq article[Articles.id] }.count()
            val articleAuthor = getProfileBy(Users.id eq article[Articles.authorId], currentUserEmail)
                ?: throw Exception("Author of article not found.")
            val tags = Tags.select { Tags.articleId eq article[Articles.id] }.map { it.toTag() }

            article.toArticle(
                articleAuthor,
                tags,
                true,
                favoritesCount
            )
        }

    override fun deleteArticleFavorite(slug: ArticleSlug, currentUserEmail: Email): Article =
        transaction(database) {
            val currentUser = getUser(byEmail(currentUserEmail)) ?: throw UserNotFoundException(currentUserEmail.value)
            val article = Articles.select { Articles.slug eq slug.value }.firstOrNull() ?: throw HttpException(
                Status.NOT_FOUND,
                "Article with slug [$slug] not found."
            )

            Favorites.deleteWhere {
                (Favorites.articleId eq article[Articles.id]) and (Favorites.userId eq currentUser.id)
            }

            val favoritesCount = Favorites.select { Favorites.articleId eq article[Articles.id] }.count()
            val articleAuthor = getProfileBy(Users.id eq article[Articles.authorId], currentUserEmail)
                ?: throw Exception("Author of article not found.")
            val tags = Tags.select { Tags.articleId eq article[Articles.id] }.map { it.toTag() }

            article.toArticle(
                articleAuthor,
                tags,
                false,
                favoritesCount
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

fun ResultRow.toArticle(author: Profile, tags: List<ArticleTag>, favorited: Boolean, favoritesCount: Int) = Article(
    slug = ArticleSlug(this[Articles.slug]),
    author = author,
    body = ArticleBody(this[Articles.body]),
    createdAt = this[Articles.createdAt],
    description = ArticleDescription(this[Articles.description]),
    favorited = favorited,
    favoritesCount = favoritesCount,
    tagList = tags,
    title = ArticleTitle(this[Articles.title]),
    updatedAt = this[Articles.updatedAt]
)

fun ResultRow.toComment(author: Profile) = Comment(
    id = this[Comments.id],
    body = CommentBody(this[Comments.body]),
    createdAt = this[Comments.createdAt],
    updatedAt = this[Comments.updatedAt],
    author = author
)

fun ResultRow.toTag() = ArticleTag(this[Tags.tag])

class UserAlreadyExistsException : HttpException(Status.CONFLICT, "The specified user already exists.")
class UserNotFoundException(usernameOrEmail: String) :
    HttpException(Status.BAD_REQUEST, "User $usernameOrEmail does not exist.")