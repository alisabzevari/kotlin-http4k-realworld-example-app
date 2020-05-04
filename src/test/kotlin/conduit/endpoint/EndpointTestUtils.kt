package conduit.endpoint

import conduit.Router
import conduit.config.JwtConfig
import conduit.model.Email
import conduit.model.Username
import conduit.util.generateToken
import conduit.util.toJsonTree
import io.jsonwebtoken.SignatureAlgorithm
import io.kotlintest.shouldBe
import io.mockk.mockk
import org.http4k.core.Response
import javax.crypto.spec.SecretKeySpec

fun getRouterToTest() = Router(
    corsPolicy = mockk(relaxed = true),
    jwtSigningKey = jwtTestSigningKey,
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

val jwtTestSigningKey = SecretKeySpec("Top Secret".toByteArray(), SignatureAlgorithm.HS256.jcaName)

val jwtTestConfig = JwtConfig(secret = "Top Secret", expirationMillis = 36_000_000, issuer = "foo")

fun generateTestToken() = generateToken(
    signingKey = jwtTestConfig.signingKey,
    issuer = jwtTestConfig.issuer,
    expirationMillis = jwtTestConfig.expirationMillis,
    username = Username("ali"),
    email = Email("alisabzevari@gmail.com")
)
