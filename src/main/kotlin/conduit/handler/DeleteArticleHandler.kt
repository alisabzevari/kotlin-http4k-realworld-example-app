package conduit.handler

import conduit.model.ArticleSlug
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface DeleteArticleHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo)
}

class DeleteArticleHandlerImpl(val txManager: ConduitTxManager) : DeleteArticleHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo) {
        txManager.tx {
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