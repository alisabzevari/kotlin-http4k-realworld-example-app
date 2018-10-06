package conduit.endpoint

import conduit.Router
import conduit.model.Bio
import conduit.model.Image
import conduit.model.Profile
import conduit.model.Username
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FollowUserEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should follow the user`() {
        every { router.followUser(any(), any()) } returns Profile(
            Username("jake"),
            Bio("I work at statefarm"),
            Image("Image"),
            true
        )

        val request = Request(Method.POST, "/api/profiles/user1/follow")
            .header("Authorization", "Token ${generateTestToken().value}")

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