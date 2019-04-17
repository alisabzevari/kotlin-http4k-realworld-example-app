package conduit.handler

import conduit.model.ArticleTag
import conduit.repository.ConduitTxManager

interface GetTagsHandler {
    operator fun invoke(): List<ArticleTag>
}

class GetTagsHandlerImpl(val txManager: ConduitTxManager) : GetTagsHandler {
    override fun invoke(): List<ArticleTag> = txManager.tx {
        getAllTags()
    }
}