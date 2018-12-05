package conduit.handler

import conduit.model.Article
import conduit.model.ArticleSlug
import conduit.model.UpdateArticle
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface UpdateArticleHandler {
    operator fun invoke(slug: ArticleSlug, updateArticleDto: UpdateArticle, tokenInfo: TokenAuth.TokenInfo): Article
}

class UpdateArticleHandlerImpl(val repository: ConduitRepository) : UpdateArticleHandler {
    override fun invoke(slug: ArticleSlug, updateArticleDto: UpdateArticle, tokenInfo: TokenAuth.TokenInfo): Article =
        repository.updateArticle(slug, updateArticleDto, tokenInfo.extractEmail())
}