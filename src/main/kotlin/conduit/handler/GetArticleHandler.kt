package conduit.handler

import conduit.model.Article
import conduit.model.ArticleSlug
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface GetArticleHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): Article
}

class GetArticleHandlerImpl(val repository: ConduitRepository): GetArticleHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): Article =
            repository.getArticle(slug, tokenInfo?.extractEmail())
}