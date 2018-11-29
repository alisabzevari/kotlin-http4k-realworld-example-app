package conduit.handler

import conduit.repository.ConduitRepository

interface DeleteArticleCommentHandler {
    operator fun invoke(commentId: Int)
}

class DeleteArticleCommentHandlerImpl(val repository: ConduitRepository) : DeleteArticleCommentHandler {
    override fun invoke(commentId: Int) = repository.deleteArticleComment(commentId)
}