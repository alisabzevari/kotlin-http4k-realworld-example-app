package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.CommentDto
import conduit.model.extractEmail
import conduit.repository.ConduitDatabase
import conduit.repository.toProfile
import conduit.util.HttpException
import conduit.util.TokenAuth
import org.http4k.core.Status

interface GetArticleCommentsHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): List<CommentDto>
}

class GetArticleCommentsHandlerImpl(val database: ConduitDatabase) : GetArticleCommentsHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): List<CommentDto> = database.tx {
        val article =
            getArticle(slug) ?: throw HttpException(Status.NOT_FOUND, "Article with slug ${slug.value} not found.")
        val comments = getArticleComments(article.id)

        val currentUserEmail = tokenInfo?.extractEmail()
        val currentUser = if (currentUserEmail != null) getUser(currentUserEmail) else null

        comments.map { comment ->
            val authorUser = getUser(comment.authorId) ?: throw HttpException(
                Status.INTERNAL_SERVER_ERROR,
                "User for comment not fount."
            )
            val following = if (currentUser != null) getFollowing(currentUser.id, authorUser.id) else false

            CommentDto(
                comment.id,
                comment.createdAt,
                comment.updatedAt,
                comment.body,
                authorUser.toProfile(following)
            )
        }
    }
}