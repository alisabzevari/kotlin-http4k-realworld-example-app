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
    loginHandler = mockk(relaxed = true),
    registerUserHandler = mockk(relaxed = true),
    getCurrentUserHandler = mockk(relaxed = true),
    updateCurrentUserHandler = mockk(relaxed = true)
)

fun Response.expectJsonResponse(expectedBody: String? = null) {
    assertEquals("application/json; charset=utf-8", this.header("Content-Type"))
    if (expectedBody != null) {
        assertEquals(expectedBody.toJsonTree(), this.bodyString().toJsonTree())
    }
}

fun generateTestToken() = generateToken(Username("ali"), Email("alisabzevari@gmail.com"))