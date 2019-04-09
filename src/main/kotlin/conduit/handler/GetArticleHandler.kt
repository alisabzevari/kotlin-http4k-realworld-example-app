package conduit.handler

import conduit.model.ArticleDto
import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.toProfile
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status

interface GetArticleHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): ArticleDto
}

class GetArticleHandlerImpl(val database: ConduitDatabase) : GetArticleHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): ArticleDto = database.tx {
        val article =
            getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")

        val tags = getArticleTags(article.id)
        val favoritesCount = getArticleFavoritesCount(article.id)
        val authorUser = getUser(article.authorId) ?: throw HttpException(
            Status.INTERNAL_SERVER_ERROR,
            "Cannot find user for the article with slug ${slug.value}"
        )

        val currentUser = tokenInfo?.extractEmail()?.let { getUser(it) }
        val authorFollowedByCurrentUser = currentUser?.let { getFollowing(it.id, authorUser.id) } ?: false
        val favorited = currentUser?.let { isArticleFavorited(article.id, it.id) } ?: false

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
            authorUser.toProfile(authorFollowedByCurrentUser)
        )
    }
}