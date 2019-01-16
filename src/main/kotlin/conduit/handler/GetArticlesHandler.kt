package conduit.handler

import conduit.model.ArticleTag
import conduit.model.Username
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface GetArticlesHandler {
    operator fun invoke(
        tokenInfo: TokenAuth.TokenInfo?,
        offset: Int,
        limit: Int,
        tag: ArticleTag?,
        author: Username?,
        favoritedByUser: Username?
    ): MultipleArticles
}

class GetArticlesHandlerImpl(val repository: ConduitRepository) : GetArticlesHandler {
    override fun invoke(
        tokenInfo: TokenAuth.TokenInfo?,
        offset: Int,
        limit: Int,
        tag: ArticleTag?,
        author: Username?,
        favoritedByUser: Username?
    ) = repository.getArticles(tokenInfo?.extractEmail(), offset, limit, tag, author, favoritedByUser)
}