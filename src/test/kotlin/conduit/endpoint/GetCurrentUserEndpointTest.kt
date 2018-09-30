package conduit.endpoint

import conduit.Router
import conduit.handler.UserDto
import conduit.model.*
import conduit.util.generateToken
import conduit.util.toJsonTree
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetCurrentUserEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should return current user information`() {
        every { router.getCurrentUserHandler(any()) } returns UserDto(
            Email("jake@jake.jake"),
            Token("jwt.token.here"),
            Username("jake"),
            Bio("I work at statefarm"),
            null
        )

        val request = Request(Method.GET, "/api/users").header("Authorization", "Token: ${generateTestToken().value}")

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "user": {
                "email": "jake@jake.jake",
                "token": "jwt.token.here",
                "username": "jake",
                "bio": "I work at statefarm",
                "image": null
              }
            }
        """.trimIndent()
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }
}