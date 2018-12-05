package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface DeleteArticleHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Unit
}

class DeleteArticleHandlerImpl(val repository: ConduitRepository): DeleteArticleHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo) {
        repository.deleteArticle(slug, tokenInfo.extractEmail())
    }

}