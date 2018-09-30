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

class UpdateCurrentUserEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should update current user information`() {
        val user = UserDto(
            Email("newemail@site.com"),
            Token("jwt.token.here"),
            Username("new username"),
            Bio("new bio"),
            Image("new image")
        )
        every { router.updateCurrentUserHandler(any(), any()) } returns user

        @Language("JSON")
        val requestBody = """
            {
              "user": {
                "email": "newemail@site.com",
                "token": "cannot change token",
                "username": "new username",
                "bio": "new bio",
                "image": "new image"
              }
            }
        """.trimIndent()
        val request = Request(Method.PUT, "/api/users")
            .header("Authorization", "Token: ${generateTestToken().value}")
            .body(requestBody)

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "user": {
                "email": "${user.email.value}",
                "token": "${user.token.value}",
                "username": "${user.username.value}",
                "bio": "${user.bio?.value}",
                "image": "${user.image?.value}"
              }
            }
        """.trimIndent()
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }
}