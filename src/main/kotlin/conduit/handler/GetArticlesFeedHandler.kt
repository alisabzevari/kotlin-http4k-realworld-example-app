package conduit.handler

import conduit.model.ArticleDto
import conduit.model.toProfile
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface GetArticlesFeedHandler {
    operator fun invoke(tokenInfo: TokenAuth.TokenInfo, offset: Int, limit: Int): MultipleArticles
}

// TODO: Write integration tests for articles feed
class GetArticlesFeedHandlerImpl(val txManager: ConduitTxManager) :
    GetArticlesFeedHandler {
    override fun invoke(tokenInfo: TokenAuth.TokenInfo, offset: Int, limit: Int) =
        txManager.tx {
            val email = tokenInfo.extractEmail()
            val user =
                getUser(email) ?: throw HttpException(Status.NOT_FOUND, "User with email ${email.value} not found.")
            val followedUserIds = getFollowedUserIds(user.id)
            val articlesCount = getArticlesOfAuthorsCount(followedUserIds)
            val articles = getArticlesOfAuthors(followedUserIds, offset, limit)
            val articleTags = articles.map { article -> article.id to getTagsOfArticle(article.id) }.toMap()
            val articleAuthors = articles.mapNotNull { article ->
                val author = getUser(article.authorId)
                if (author != null) article.id to author else null
            }.toMap()
            val favoritesCountsMap =
                articles.map { article -> article.id to getArticleFavoritesCount(article.id) }.toMap()
            val favorited = articles.map { article -> article.id to isArticleFavorited(article.id, user.id) }.toMap()

            val resultArticles = articles.map {
                val authorFollowed = followedUserIds.any { id -> id == it.authorId }

                ArticleDto(
                    it.slug,
                    it.title,
                    it.description,
                    it.body,
                    articleTags[it.id] ?: emptyList(),
                    it.createdAt,
                    it.updatedAt,
                    favorited[it.id] ?: false,
                    favoritesCountsMap[it.id] ?: 0,
                    articleAuthors[it.id]?.toProfile(authorFollowed)
                        ?: throw Error("Article ${it.id} doesn't have an author.")
                )
            }

            MultipleArticles(resultArticles, articlesCount)
        }
}

data class MultipleArticles(val articles: List<ArticleDto>, val articlesCount: Int)