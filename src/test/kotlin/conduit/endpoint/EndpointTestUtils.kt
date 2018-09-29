package conduit.endpoint

import conduit.Router
import conduit.util.toJsonTree
import io.mockk.mockk
import org.http4k.core.Response
import kotlin.test.assertEquals

fun getRouterToTest() = Router(
    loginHandler = mockk(relaxed = true),
    registerUserHandler = mockk(relaxed = true)
)

fun Response.expectJsonResponse(expectedBody: String? = null) {
    assertEquals("application/json; charset=utf-8", this.header("Content-Type"))
    if (expectedBody != null) {
        assertEquals(expectedBody.toJsonTree(), this.bodyString().toJsonTree())
    }
}