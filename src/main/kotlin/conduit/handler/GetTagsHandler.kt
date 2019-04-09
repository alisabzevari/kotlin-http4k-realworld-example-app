package conduit.handler

import conduit.model.ArticleTag
import conduit.repository.ConduitDatabase

interface GetTagsHandler {
    operator fun invoke(): List<ArticleTag>
}

class GetTagsHandlerImpl(val database: ConduitDatabase) : GetTagsHandler {
    override fun invoke(): List<ArticleTag> = database.tx {
        getAllTags()
    }
}