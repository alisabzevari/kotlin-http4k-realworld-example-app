package conduit.model

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import org.joda.time.DateTime
import com.fasterxml.jackson.annotation.JsonCreator.Mode.DELEGATING as m

data class Email @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Password @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Token @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Username @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Bio @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Image @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class User(
    val id: Int,
    val email: Email,
    val password: Password,
    val token: Token?,
    val username: Username,
    val bio: Bio?,
    val image: Image?
)

fun User.toProfile(following: Boolean) = Profile(
    username = username,
    bio = bio,
    image = image,
    following = following
)

data class Profile(
    val username: Username,
    val bio: Bio?,
    val image: Image?,
    val following: Boolean
)

data class ArticleSlug @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class ArticleTitle @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class ArticleDescription @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class ArticleBody @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class ArticleTag @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Article(
    val id: Int,
    val slug: ArticleSlug,
    val title: ArticleTitle,
    val description: ArticleDescription,
    val body: ArticleBody,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val authorId: Int
)

data class CommentBody @JsonCreator(mode = m) constructor(@JsonValue val value: String) {
    override fun toString(): String = this.value
}

data class Comment(
    val id: Int,
    val createdAt: DateTime,
    val updatedAt: DateTime,
    val body: CommentBody,
    val authorId: Int,
    val articleId: Int
)

data class UpdateArticle(
    val title: ArticleTitle?,
    val description: ArticleDescription?,
    val body: ArticleBody?
)

data class NewArticle(
    val title: ArticleTitle,
    val description: ArticleDescription,
    val body: ArticleBody,
    val tagList: List<ArticleTag>,
    val authorId: Int,
    val slug: ArticleSlug,
    val createdAt: DateTime,
    val updatedAt: DateTime
)

data class NewUser(
    val username: Username,
    val password: Password,
    val email: Email
)

data class UpdateUser(
    val email: Email?,
    val username: Username?,
    val bio: Bio?,
    val image: Image?
)