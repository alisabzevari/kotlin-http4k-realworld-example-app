package conduit.endpoint

import conduit.Router
import conduit.config.JwtConfig
import conduit.model.Email
import conduit.model.Username
import conduit.util.JWT
import conduit.util.toJsonTree
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.http4k.core.Response

fun getRouterToTest() = Router(
    corsPolicy = mockk(relaxed = true),
    jwt = jwt,
    login = mockk(relaxed = true),
    registerUser = mockk(relaxed = true),
    getCurrentUser = mockk(relaxed = true),
    updateCurrentUser = mockk(relaxed = true),
    getProfile = mockk(relaxed = true),
    followUser = mockk(relaxed = true),
    unfollowUser = mockk(relaxed = true),
    getArticlesFeed = mockk(relaxed = true),
    getTags = mockk(relaxed = true),
    createArticle = mockk(relaxed = true),
    createArticleComment = mockk(relaxed = true),
    createArticleFavorite = mockk(relaxed = true),
    deleteArticleFavorite = mockk(relaxed = true),
    getArticleComments = mockk(relaxed = true),
    deleteArticleComment = mockk(relaxed = true),
    deleteArticle = mockk(relaxed = true),
    getArticle = mockk(relaxed = true),
    updateArticle = mockk(relaxed = true),
    getArticles = mockk(relaxed = true)
)

fun Response.expectJsonResponse(expectedBody: String? = null) {
    this.header("Content-Type").shouldBe("application/json; charset=utf-8")
    if (expectedBody != null) {
        this.bodyString().toJsonTree().shouldBe(expectedBody.toJsonTree())
    }
}

val jwtTestConfig = JwtConfig(secret = "Top Secret", expirationMillis = 36_000_000, issuer = "foo")

val jwt = JWT(jwtTestConfig.secret, jwtTestConfig.algorithm, jwtTestConfig.issuer, jwtTestConfig.expirationMillis)

fun generateTestToken() = jwt.generate(
    username = Username("ali"),
    email = Email("alisabzevari@gmail.com")
)
