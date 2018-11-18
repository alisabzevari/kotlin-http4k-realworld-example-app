package conduit.handler

import conduit.model.ArticleSlug
import conduit.model.Comment
import conduit.model.CommentBody
import conduit.model.extractEmail
import conduit.repository.ConduitRepository
import conduit.util.TokenAuth

interface CreateArticleCommentHandler {
    operator fun invoke(newComment: NewComment, slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Comment
}

class CreateArticleCommentHandlerImpl(val repository: ConduitRepository): CreateArticleCommentHandler {
    override fun invoke(newComment: NewComment, slug: ArticleSlug, tokenInfo: TokenAuth.TokenInfo): Comment =
            repository.createArticleComment(newComment, slug, tokenInfo.extractEmail())
}

data class NewComment(val body: CommentBody)