package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status

interface DeleteArticleHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo)
}

class DeleteArticleHandlerImpl(val database: ConduitDatabase) : DeleteArticleHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo) {
        database.tx {
            val currentUserEmail = tokenInfo.extractEmail()
            val currentUser = getUser(currentUserEmail) ?: throw HttpException(
                Status.NOT_FOUND,
                "User with email $currentUserEmail not found."
            )

            val article =
                getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")

            if (article.authorId == currentUser.id) {
                deleteArticle(article.id)
            }
            // TODO: Should I implement the else case?
        }
    }
}