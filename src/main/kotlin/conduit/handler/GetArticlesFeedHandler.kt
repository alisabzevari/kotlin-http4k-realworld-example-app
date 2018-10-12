package conduit.handler

import conduit.model.Article
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface GetArticlesFeedHandler {
    operator fun invoke(tokenInfo: TokenAuth.TokenInfo, offset: Int, limit: Int): MultipleArticles
}

class GetArticlesFeedHandlerImpl(val repository: ConduitRepository) : GetArticlesFeedHandler {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo, offset: Int, limit: Int) =
        repository.getArticlesFeed(tokenInfo.extractEmail(), offset, limit)
}

data class MultipleArticles(val articles: List<Article>, val articlesCount: Int)
