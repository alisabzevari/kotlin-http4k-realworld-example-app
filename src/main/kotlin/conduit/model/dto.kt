package conduit.model

import org.joda.time.DateTime

data class ArticleDto(
    val slug: ArticleSlug,
    val title: ArticleTitle,
    val description: ArticleDescription,
    val body: ArticleBody,
    val tagList: List<ArticleTag>,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val favorited: Boolean,
    val favoritesCount: Int,
    val author: Profile
)

data class CommentDto(
    val id: Int,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val body: CommentBody,
    val author: Profile
)