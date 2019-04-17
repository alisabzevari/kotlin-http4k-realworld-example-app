package conduit.handler

import conduit.model.*
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status
import org.joda.time.DateTime

interface CreateArticleHandler {
    operator fun invoke(newArticle: NewArticleDto, tokenInfo: TokenAuth.TokenInfo): ArticleDto
}

class CreateArticleHandlerImpl(val txManager: ConduitTxManager) : CreateArticleHandler {
    override fun invoke(newArticle: NewArticleDto, tokenInfo: TokenAuth.TokenInfo): ArticleDto =
        txManager.tx {
            val email = tokenInfo.extractEmail()
            val authorUser =
                getUser(email) ?: throw HttpException(Status.NOT_FOUND, "User with email $email not found.")
            val now = DateTime.now()
            val newDbArticle = newArticle.let {
                NewArticle(
                    it.title,
                    it.description,
                    it.body,
                    it.tagList,
                    authorUser.id,
                    ArticleSlug(it.title.value.replace(" ", "-")),
                    now,
                    now
                )
            }
            insertArticle(newDbArticle)

            ArticleDto(
                newDbArticle.slug,
                newDbArticle.title,
                newDbArticle.description,
                newDbArticle.body,
                newArticle.tagList,
                newDbArticle.createdAt,
                newDbArticle.updatedAt,
                false,
                0,
                authorUser.toProfile(false)
            )
        }
}

data class NewArticleDto(
    val title: ArticleTitle,
    val description: ArticleDescription,
    val body: ArticleBody,
    val tagList: List<ArticleTag>
)