package conduit.repository

import conduit.model.*
import org.jetbrains.exposed.dao.EntityID
import org.jetbrains.exposed.sql.*
import org.joda.time.DateTime

interface ConduitRepository {
    fun getUser(email: Email): User?
    fun getUser(username: Username): User?
    fun getUser(userId: Int): User?
    fun insertUser(newUser: NewUser)
    fun updateUser(userId: Int, user: UpdateUser)

    fun getFollowing(sourceUserId: Int, targetUserId: Int): Boolean
    fun getFollowedUserIds(sourceUserId: Int): List<Int>
    fun insertFollowing(sourceUserId: Int, targetUserId: Int)
    fun deleteFollowing(sourceUserId: Int, targetUserId: Int)

    fun getArticleFavoritesCount(articleId: Int): Int
    fun isArticleFavorited(articleId: Int, userId: Int): Boolean
    fun insertFavorite(articleId: Int, userId: Int)
    fun deleteFavorite(articleId: Int, userId: Int)
    fun updateArticle(articleId: Int, body: ArticleBody?, description: ArticleDescription?, title: ArticleTitle?)

    fun getArticlesOfAuthorsCount(authorUserIds: List<Int>): Int
    fun getArticlesOfAuthors(authorUserIds: List<Int>, offset: Int, limit: Int): List<Article>
    fun getArticle(slug: ArticleSlug): Article?
    fun getArticleIdsByTag(tag: ArticleTag): List<Int>
    fun getArticleIdsFavoritedBy(userId: Int): List<Int>
    fun getArticlesCount(authorUserId: Int?, taggedWithTagIds: List<Int>?, includingIds: List<Int>?): Int
    fun getArticles(
        limit: Int,
        offset: Int,
        authorUserId: Int?,
        taggedWithTagIds: List<Int>?,
        includingIds: List<Int>?
    ): List<Article>
    fun insertArticle(newArticle: NewArticle)
    fun deleteArticle(articleId: Int)

    fun insertComment(body: CommentBody, authorId: Int, articleId: Int, createdAt: DateTime, updatedAt: DateTime): Int
    fun getArticleComments(articleId: Int): List<Comment>
    fun deleteArticleComment(commentId: Int)

    fun getTagsOfArticle(articleId: Int): List<ArticleTag>
    fun getAllTags(): List<ArticleTag>
}

class ConduitRepositoryImpl : ConduitRepository {
    override fun getUser(email: Email) = Users.select { Users.email eq email.value }.firstOrNull()?.toUser()
    override fun getUser(username: Username) =
        Users.select { Users.username eq username.value }.firstOrNull()?.toUser()

    override fun getUser(userId: Int) = Users.select { Users.id eq userId }.singleOrNull()?.toUser()

    override fun insertUser(newUser: NewUser) {
        Users.insert {
            it[email] = newUser.email.value
            it[username] = newUser.username.value
            it[password] = newUser.password.value
            it[bio] = ""
            it[image] = null
        }
    }

    override fun updateUser(userId: Int, user: UpdateUser) {
        Users.update({ Users.id eq userId }) {
            if (user.bio != null) it[this.bio] = user.bio.value
            if (user.email != null) it[this.email] = user.email.value
            if (user.image != null) it[this.image] = user.image.value
            if (user.username != null) it[this.username] = user.username.value
        }
    }

    override fun getFollowing(sourceUserId: Int, targetUserId: Int): Boolean =
        Following.select { (Following.sourceId eq sourceUserId) and (Following.targetId eq targetUserId) }.any()

    override fun getFollowedUserIds(sourceUserId: Int): List<Int> =
        Following.select { Following.sourceId eq sourceUserId }.map { it[Following.targetId].value }.toList()

    override fun insertFollowing(sourceUserId: Int, targetUserId: Int) {
        Following.insert {
            it[sourceId] = EntityID(sourceUserId, Users)
            it[targetId] = EntityID(targetUserId, Users)
        }
    }

    override fun deleteFollowing(sourceUserId: Int, targetUserId: Int) {
        Following.deleteWhere {
            (Following.sourceId eq sourceUserId) and (Following.targetId eq targetUserId)
        }
    }

    override fun getArticlesOfAuthorsCount(authorUserIds: List<Int>): Int =
        Articles.select { Articles.authorId inList authorUserIds }.count()

    override fun getArticlesOfAuthors(authorUserIds: List<Int>, offset: Int, limit: Int): List<Article> =
        Articles.select { Articles.authorId inList authorUserIds }
            .orderBy(Articles.createdAt, SortOrder.DESC)
            .limit(limit, offset)
            .toList()
            .map { it.toArticle() }

    override fun getTagsOfArticle(articleId: Int): List<ArticleTag> =
        Tags.select { Tags.articleId eq EntityID(articleId, Articles) }
            .map { ArticleTag(it[Tags.tag]) }
            .toList()

    override fun getArticleFavoritesCount(articleId: Int): Int =
        Favorites.select { Favorites.articleId eq EntityID(articleId, Articles) }.count()

    override fun isArticleFavorited(articleId: Int, userId: Int) =
        Favorites
            .select {
                (Favorites.articleId eq EntityID(articleId, Articles)) and (Favorites.userId eq EntityID(userId, Users))
            }.count() == 1

    override fun insertFavorite(articleId: Int, userId: Int) {
        Favorites.insert {
            it[Favorites.articleId] = EntityID(articleId, Articles)
            it[Favorites.userId] = EntityID(userId, Users)
        }
    }

    override fun deleteFavorite(articleId: Int, userId: Int) {
        Favorites.deleteWhere {
            (Favorites.articleId eq articleId) and (Favorites.userId eq userId)
        }
    }

    override fun updateArticle(
        articleId: Int,
        body: ArticleBody?,
        description: ArticleDescription?,
        title: ArticleTitle?
    ) {
        Articles.update({ Articles.id eq articleId }) {
            if (title != null) it[Articles.title] = title.value
            if (description != null) it[Articles.description] = description.value
            if (body != null) it[Articles.body] = body.value
        }
    }

    override fun insertArticle(newArticle: NewArticle) {
        val articleId = Articles.insert {
            it[authorId] = EntityID(newArticle.authorId, Users)
            it[body] = newArticle.body.value
            it[description] = newArticle.description.value
            it[slug] = newArticle.slug.value
            it[title] = newArticle.title.value
            it[createdAt] = newArticle.createdAt
            it[updatedAt] = newArticle.updatedAt
        }[Articles.id]

        newArticle.tagList.forEach { tag ->
            Tags.insert {
                it[Tags.articleId] = articleId
                it[Tags.tag] = tag.value
            }
        }
    }

    override fun getArticle(slug: ArticleSlug): Article? =
        Articles.select { Articles.slug eq slug.value }.singleOrNull()?.toArticle()

    override fun getArticleIdsByTag(tag: ArticleTag): List<Int> =
        Tags.select { Tags.tag eq tag.value }.map { it[Tags.articleId].value }

    override fun getArticleIdsFavoritedBy(userId: Int): List<Int> =
        Favorites.select { Favorites.userId eq userId }.map { it[Favorites.articleId].value }

    override fun getArticlesCount(authorUserId: Int?, taggedWithTagIds: List<Int>?, includingIds: List<Int>?): Int {
        val query = Articles.selectAll()

        authorUserId?.also { userId -> query.andWhere { Articles.authorId eq userId } }
        taggedWithTagIds?.also { tagIds -> query.andWhere { Articles.id inList tagIds } }
        includingIds?.also { ids -> query.andWhere { Articles.id inList ids } }

        return query.count()
    }

    override fun getArticles(
        limit: Int,
        offset: Int,
        authorUserId: Int?,
        taggedWithTagIds: List<Int>?,
        includingIds: List<Int>?
    ): List<Article> {
        val query = Articles.selectAll()

        authorUserId?.also { userId -> query.andWhere { Articles.authorId eq userId } }
        taggedWithTagIds?.also { tagIds -> query.andWhere { Articles.id inList tagIds } }
        includingIds?.also { ids -> query.andWhere { Articles.id inList ids } }

        return query.orderBy(Articles.createdAt, SortOrder.DESC).limit(limit, offset).map { it.toArticle() }
    }

    override fun deleteArticle(articleId: Int) {
        Articles.deleteWhere { Articles.id eq articleId }
    }

    override fun insertComment(
        body: CommentBody,
        authorId: Int,
        articleId: Int,
        createdAt: DateTime,
        updatedAt: DateTime
    ) = Comments.insert {
        it[Comments.authorId] = EntityID(authorId, Users)
        it[Comments.body] = body.value
        it[Comments.createdAt] = createdAt
        it[Comments.updatedAt] = updatedAt
        it[Comments.articleId] = EntityID(articleId, Articles)
    }[Comments.id].value

    override fun getArticleComments(articleId: Int): List<Comment> =
        Comments.select { Comments.articleId eq articleId }.map { it.toComment() }

    override fun deleteArticleComment(commentId: Int) {
        Comments.deleteWhere { Comments.id eq commentId }
    }

    override fun getAllTags(): List<ArticleTag> = Tags
        .slice(Tags.tag)
        .selectAll()
        .withDistinct()
        .map { ArticleTag(it[Tags.tag]) }
}

fun ResultRow.toUser() = User(
    id = this[Users.id].value,
    email = Email(this[Users.email]),
    password = Password(this[Users.password]),
    token = null,
    username = Username(this[Users.username]),
    bio = this[Users.bio]?.let(::Bio),
    image = this[Users.image]?.let(::Image)
)

fun ResultRow.toArticle() = Article(
    id = this[Articles.id].value,
    slug = ArticleSlug(this[Articles.slug]),
    body = ArticleBody(this[Articles.body]),
    createdAt = this[Articles.createdAt],
    description = ArticleDescription(this[Articles.description]),
    title = ArticleTitle(this[Articles.title]),
    updatedAt = this[Articles.updatedAt],
    authorId = this[Articles.authorId].value
)

fun ResultRow.toComment() = Comment(
    id = this[Comments.id].value,
    body = CommentBody(this[Comments.body]),
    createdAt = this[Comments.createdAt],
    updatedAt = this[Comments.updatedAt],
    articleId = this[Comments.articleId].value,
    authorId = this[Comments.authorId].value
)
