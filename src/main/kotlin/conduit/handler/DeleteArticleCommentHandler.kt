package conduit.handler

import conduit.repository.ConduitTxManager

interface DeleteArticleCommentHandler {
    operator fun invoke(commentId: Int)
}

class DeleteArticleCommentHandlerImpl(val txManager: ConduitTxManager) : DeleteArticleCommentHandler {
    override fun invoke(commentId: Int) = txManager.tx {
        deleteArticleComment(commentId)
    }
}