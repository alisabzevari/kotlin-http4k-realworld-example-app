package conduit.handler

import conduit.model.*
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface CreateArticleHandler {
    operator fun invoke(newArticle: NewArticle, tokenInfo: TokenAuth.TokenInfo): Article
}

class CreateArticleHandlerImpl(val repository: ConduitRepository) : CreateArticleHandler {
    override fun invoke(newArticle: NewArticle, tokenInfo: TokenAuth.TokenInfo): Article =
        repository.createArticle(newArticle, tokenInfo.extractEmail())


}

data class NewArticle(
    val title: ArticleTitle,
    val description: ArticleDescription,
    val body: ArticleBody,
    val tagList: List<ArticleTag>
)