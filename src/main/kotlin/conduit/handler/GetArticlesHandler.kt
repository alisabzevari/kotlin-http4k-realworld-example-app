package conduit.handler

import conduit.model.*
import conduit.repository.ConduitDatabase
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status

interface GetArticlesHandler {
    operator fun invoke(
        tokenInfo: TokenAuth.TokenInfo?,
        offset: Int,
        limit: Int,
        tag: ArticleTag?,
        author: Username?,
        favoritedByUser: Username?
    ): MultipleArticles
}

class GetArticlesHandlerImpl(val database: ConduitDatabase) : GetArticlesHandler {
    override fun invoke(
        tokenInfo: TokenAuth.TokenInfo?,
        offset: Int,
        limit: Int,
        tag: ArticleTag?,
        author: Username?,
        favoritedByUser: Username?
    ) = database.tx {
        val taggedArticleIds = tag?.let { getArticleIdsByTag(it) }
        val authorUserId = author?.let { getUser(it) }?.id
        val favoritedArticleIds = favoritedByUser?.let {
            val user = getUser(it) ?: throw HttpException(Status.NOT_FOUND, "User $favoritedByUser not found.")
            getArticleIdsFavoritedBy(user.id)
        }

        // TODO: fix this: this route should work without authentication. It should not throw exception
        val email = tokenInfo?.extractEmail() ?: throw HttpException(Status.INTERNAL_SERVER_ERROR, "Cannot extract email from the token.")
        val currentUser = getUser(email) ?: throw HttpException(Status.NOT_FOUND, "User with email $email not found.")

        val articleIdsFavoritedByCurrentUser = getArticleIdsFavoritedBy(currentUser.id)

        val articlesCount = queryArticlesCount(authorUserId, taggedArticleIds, favoritedArticleIds)
        val articles = queryArticles(limit, offset, authorUserId, taggedArticleIds, favoritedArticleIds)
            .map { article ->
                val tags = getArticleTags(article.id)
                val favorited = article.id in articleIdsFavoritedByCurrentUser
                val favoritesCount = getArticleFavoritesCount(article.id)
                val articleAuthor = getUser(article.authorId) ?: throw HttpException(
                    Status.BAD_REQUEST,
                    "Author for article with id ${article.id} not found."
                )
                val authorProfile = Profile(
                    articleAuthor.username,
                    articleAuthor.bio,
                    articleAuthor.image,
                    getFollowing(currentUser.id, articleAuthor.id)
                )

                ArticleDto(
                    article.slug,
                    article.title,
                    article.description,
                    article.body,
                    tags,
                    article.createdAt,
                    article.updatedAt,
                    favorited,
                    favoritesCount,
                    authorProfile
                )
            }


        MultipleArticles(
            articles,
            articlesCount
        )
    }
}