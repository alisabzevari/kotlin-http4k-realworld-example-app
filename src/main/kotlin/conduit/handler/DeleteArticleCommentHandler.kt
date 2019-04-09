package conduit.handler

import conduit.repository.ConduitDatabase

interface DeleteArticleCommentHandler {
    operator fun invoke(commentId: Int)
}

class DeleteArticleCommentHandlerImpl(val database: ConduitDatabase) : DeleteArticleCommentHandler {
    override fun invoke(commentId: Int) = database.tx {
        deleteArticleComment(commentId)
    }
}