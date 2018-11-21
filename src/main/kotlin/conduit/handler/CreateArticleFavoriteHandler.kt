package conduit.handler

import conduit.model.Article
import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface CreateArticleFavoriteHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Article
}

class CreateArticleFavoriteHandlerImpl(val repository: ConduitRepository): CreateArticleFavoriteHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Article =
            repository.createArticleFavorite(slug, tokenInfo.extractEmail())
}