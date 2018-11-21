package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.Comment
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface GetArticleCommentsHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): List<Comment>
}

class GetArticleCommentsHandlerImpl(val repository: ConduitRepository) : GetArticleCommentsHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): List<Comment> =
        repository.getArticleComments(slug, tokenInfo?.extractEmail())
}