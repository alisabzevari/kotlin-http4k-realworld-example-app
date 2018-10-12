package conduit.endpoint

import conduit.Router
import conduit.model.Email
import conduit.model.Username
import conduit.util.generateToken
import conduit.util.toJsonTree
import io.mockk.mockk
import org.http4k.core.Response
import kotlin.test.assertEquals

fun getRouterToTest() = Router(
    login = mockk(relaxed = true),
    registerUser = mockk(relaxed = true),
    getCurrentUser = mockk(relaxed = true),
    updateCurrentUser = mockk(relaxed = true),
    getProfile = mockk(relaxed = true),
    followUser = mockk(relaxed = true),
    unfollowUser = mockk(relaxed = true),
    getArticlesFeed = mockk(relaxed = true)
)

fun Response.expectJsonResponse(expectedBody: String? = null) {
    assertEquals("application/json; charset=utf-8", this.header("Content-Type"))
    if (expectedBody != null) {
        assertEquals(expectedBody.toJsonTree(), this.bodyString().toJsonTree())
    }
}

fun generateTestToken() = generateToken(Username("ali"), Email("alisabzevari@gmail.com"))