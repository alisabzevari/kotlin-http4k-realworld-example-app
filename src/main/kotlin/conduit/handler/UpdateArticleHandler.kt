package conduit.handler

import conduit.model.ArticleDto
import conduit.model.ArticleSlug
import conduit.model.UpdateArticle
import conduit.model.toProfile
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface UpdateArticleHandler {
    operator fun invoke(slug: ArticleSlug, updateArticleDto: UpdateArticle, tokenInfo: TokenAuth.TokenInfo): ArticleDto
}

class UpdateArticleHandlerImpl(val txManager: ConduitTxManager) : UpdateArticleHandler {
    override fun invoke(
        slug: ArticleSlug,
        updateArticleDto: UpdateArticle,
        tokenInfo: TokenAuth.TokenInfo
    ): ArticleDto = txManager.tx {
        val currentUser = getUser(tokenInfo.extractEmail()) ?: throw HttpException(Status.NOT_FOUND, "User not found.")

        val article =
            getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")

        if (article.authorId == currentUser.id) {
            updateArticle(
                article.id,
                updateArticleDto.body,
                updateArticleDto.description,
                updateArticleDto.title
            )
        }
        // TODO: Should I implement else case?

        val authorFollowedByCurrentUser = getFollowing(currentUser.id, currentUser.id)
        val favorited = isArticleFavorited(article.id, currentUser.id)

        val updatedArticle =
            getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")
        val tags = getTagsOfArticle(article.id)
        val favoritesCount = getArticleFavoritesCount(article.id)
        ArticleDto(
            updatedArticle.slug,
            updatedArticle.title,
            updatedArticle.description,
            updatedArticle.body,
            tags,
            updatedArticle.createdAt,
            updatedArticle.updatedAt,
            favorited,
            favoritesCount,
            currentUser.toProfile(authorFollowedByCurrentUser)
        )
    }
}