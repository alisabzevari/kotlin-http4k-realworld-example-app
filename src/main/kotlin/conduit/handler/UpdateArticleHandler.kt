package conduit.handler

import conduit.model.ArticleDto
import conduit.model.ArticleSlug
import conduit.model.UpdateArticle
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.OldRepo
import conduit.repository.toProfile
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status
import java.lang.Exception
import javax.xml.ws.http.HTTPBinding

interface UpdateArticleHandler {
    operator fun invoke(slug: ArticleSlug, updateArticleDto: UpdateArticle, tokenInfo: TokenAuth.TokenInfo): ArticleDto
}

class UpdateArticleHandlerImpl(val database: ConduitDatabase) : UpdateArticleHandler {
    override fun invoke(
        slug: ArticleSlug,
        updateArticleDto: UpdateArticle,
        tokenInfo: TokenAuth.TokenInfo
    ): ArticleDto = database.tx {
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
        val tags = getArticleTags(article.id)
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