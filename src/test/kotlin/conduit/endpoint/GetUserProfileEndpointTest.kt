package conduit.endpoint

import conduit.Router
import conduit.handler.UserDto
import conduit.model.*
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetUserProfileEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should return users profile`() {
        every { router.getProfile(any(), any()) } returns Profile(
            Username("jake"),
            Bio("I work at statefarm"),
            Image("Image"),
            true
        )

        val request = Request(Method.GET, "/api/profiles/user1").header("Authorization", "Token: ${generateTestToken().value}")

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "profile": {
                "username": "jake",
                "bio": "I work at statefarm",
                "image": "Image",
                "following": true
              }
            }
        """.trimIndent()
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }

    @Test
    fun `should return users profile even when the request does not have auth header`() {
        every { router.getProfile(any(), any()) } returns Profile(
            Username("jake"),
            Bio("I work at statefarm"),
            Image("Image"),
            true
        )

        val request = Request(Method.GET, "/api/profiles/user1")

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "profile": {
                "username": "jake",
                "bio": "I work at statefarm",
                "image": "Image",
                "following": true
              }
            }
        """.trimIndent()
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }
}