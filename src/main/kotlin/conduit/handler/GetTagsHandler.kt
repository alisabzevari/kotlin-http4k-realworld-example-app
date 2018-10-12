package conduit.handler

import conduit.model.ArticleTag
import conduit.repository.ConduitRepository

interface GetTagsHandler {
    operator fun invoke(): List<ArticleTag>
}

class GetTagsHandlerImpl(val repository: ConduitRepository) : GetTagsHandler {
    override fun invoke(): List<ArticleTag> = repository.getTags()
}