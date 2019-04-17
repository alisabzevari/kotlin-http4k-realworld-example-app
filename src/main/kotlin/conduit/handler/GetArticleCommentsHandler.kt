package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.CommentDto
import conduit.model.toProfile
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface GetArticleCommentsHandler {
    operator fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): List<CommentDto>
}

class GetArticleCommentsHandlerImpl(val txManager: ConduitTxManager) : GetArticleCommentsHandler {
    override fun invoke(slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo?): List<CommentDto> = txManager.tx {
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