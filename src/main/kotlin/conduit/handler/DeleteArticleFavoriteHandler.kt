package conduit.handler

import conduit.model.ArticleDto
import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.toProfile
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status

interface DeleteArticleFavoriteHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): ArticleDto
}

class DeleteArticleFavoriteHandlerImpl(val database: ConduitDatabase): DeleteArticleFavoriteHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): ArticleDto = database.tx {
        val currentUserEmail = tokenInfo.extractEmail()
        val currentUser = getUser(currentUserEmail) ?: throw HttpException(
            Status.NOT_FOUND,
            "User with email $currentUserEmail not found."
        )
        val article =
            getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")

        deleteFavorite(article.id, currentUser.id)

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
            false,
            favoritesCount,
            authorUser.toProfile(authorFollowedByCurrentUser)
        )
    }
}