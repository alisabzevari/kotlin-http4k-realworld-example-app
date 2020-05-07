package conduit.handler

import conduit.model.ArticleDto
import conduit.model.ArticleTag
import conduit.model.Profile
import conduit.model.Username
import conduit.repository.ConduitTxManager
import conduit.util.HttpException
import conduit.util.TokenAuth
import conduit.util.extractEmail
import org.http4k.core.Status

interface GetArticlesHandler {
    operator fun invoke(
        tokenInfo: TokenAuth.TokenInfo?,
        offset: Int,
        limit: Int,
        tag: ArticleTag?,
        author: Username?,
        favoritedByUser: Username?
    ): MultipleArticles
}

class GetArticlesHandlerImpl(val txManager: ConduitTxManager) : GetArticlesHandler {
    override fun invoke(
        tokenInfo: TokenAuth.TokenInfo?,
        offset: Int,
        limit: Int,
        tag: ArticleTag?,
        author: Username?,
        favoritedByUser: Username?
    ) = txManager.tx {
        val taggedArticleIds = tag?.let { getArticleIdsByTag(it) }
        val authorUserId = author?.let { getUser(it)?.id ?: -1 }
        val favoritedArticleIds = favoritedByUser?.let {
            val user = getUser(it) ?: throw HttpException(Status.NOT_FOUND, "User $favoritedByUser not found.")
            getArticleIdsFavoritedBy(user.id)
        }

        val email = tokenInfo?.extractEmail()
        val currentUser = email?.let { getUser(it) }

        val articleIdsFavoritedByCurrentUser = currentUser?.let { getArticleIdsFavoritedBy(it.id) } ?: emptyList()

        val articlesCount = getArticlesCount(authorUserId, taggedArticleIds, favoritedArticleIds)
        val articles = getArticles(limit, offset, authorUserId, taggedArticleIds, favoritedArticleIds)
            .map { article ->
                val tags = getTagsOfArticle(article.id)
                val favorited = article.id in articleIdsFavoritedByCurrentUser
                val favoritesCount = getArticleFavoritesCount(article.id)
                val articleAuthor = getUser(article.authorId) ?: throw HttpException(
                    Status.BAD_REQUEST,
                    "Author for article with id ${article.id} not found."
                )
                val authorProfile = Profile(
                    articleAuthor.username,
                    articleAuthor.bio,
                    articleAuthor.image,
                    currentUser?.let { getFollowing(currentUser.id, articleAuthor.id) } ?: false
                )

                ArticleDto(
                    article.slug,
                    article.title,
                    article.description,
                    article.body,
                    tags,
                    article.createdAt,
                    article.updatedAt,
                    favorited,
                    favoritesCount,
                    authorProfile
                )
            }


        MultipleArticles(
            articles,
            articlesCount
        )
    }
}
