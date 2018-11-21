package conduit.handler

import conduit.model.Article
import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface DeleteArticleFavoriteHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Article
}

class DeleteArticleFavoriteHandlerImpl(val repository: ConduitRepository): DeleteArticleFavoriteHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Article =
            repository.deleteArticleFavorite(slug, tokenInfo.extractEmail())
}