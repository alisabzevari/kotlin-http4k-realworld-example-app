package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.CommentDto
import conduit.model.CommentBody
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.toProfile
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status
import org.joda.time.DateTime

interface CreateArticleCommentHandler {
    operator fun invoke(newComment: NewComment, slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): CommentDto
}

class CreateArticleCommentHandlerImpl(val database: ConduitDatabase) : CreateArticleCommentHandler {
    override fun invoke(newComment: NewComment, slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): CommentDto =
        database.tx {
            val email = tokenInfo.extractEmail()
            val authorUser =
                getUser(email) ?: throw HttpException(Status.NOT_FOUND, "User with email $email not found.")
            val article = getArticle(slug) ?: throw HttpException(
                Status.NOT_FOUND,
                "Article with slug [$slug] not found."
            )
            val createdAt = DateTime.now()

            val id = insertComment(
                newComment.body,
                authorUser.id,
                article.id,
                createdAt,
                createdAt
            ) // TODO: Create a model type for this

            CommentDto(
                id,
                createdAt,
                createdAt,
                newComment.body,
                authorUser.toProfile(false)
            )
        }
}

data class NewComment(val body: CommentBody)