package conduit.handler

import conduit.model.ArticleDto
import conduit.model.ArticleSlug
import conduit.model.toProfile
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface CreateArticleFavoriteHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): ArticleDto
}

class CreateArticleFavoriteHandlerImpl(val txManager: ConduitTxManager) : CreateArticleFavoriteHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): ArticleDto = txManager.tx {
        val currentUserEmail = tokenInfo.extractEmail()
        val currentUser = getUser(currentUserEmail) ?: throw HttpException(
            Status.NOT_FOUND,
            "User with email $currentUserEmail not found."
        )
        val article =
            getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")
        val favoritedByCurrentUser = isArticleFavorited(article.id, currentUser.id)
        if (!favoritedByCurrentUser) {
            insertFavorite(article.id, currentUser.id)
        }

        val tags = getTagsOfArticle(article.id)
        val favoritesCount = getArticleFavoritesCount(article.id)
        val authorUser = getUser(article.authorId) ?: throw HttpException(
            Status.INTERNAL_SERVER_ERROR,
            "Cannot find user for the article with slug ${slug.value}"
        )
        val authorFollowedByCurrentUser = getFollowing(currentUser.id, authorUser.id)

        ArticleDto(
            article.slug,
            article.title,
            article.description,
            article.body,
            tags,
            article.createdAt,
            article.updatedAt,
            true,
            favoritesCount,
            authorUser.toProfile(authorFollowedByCurrentUser)
        )
    }
}