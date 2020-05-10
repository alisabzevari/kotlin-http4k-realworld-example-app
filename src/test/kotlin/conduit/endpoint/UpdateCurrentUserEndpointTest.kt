package conduit.endpoint

import conduit.Router
import conduit.handler.UserDto
import conduit.model.*
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class UpdateCurrentUserEndpointTest : StringSpec() {
    lateinit var router: Router

    override fun beforeTest(testCase: TestCase) {
        router = getRouterToTest()
    }

    init {
        "should update current user information" {
            val user = UserDto(
                Email("newemail@site.com"),
                Token("jwt.token.here"),
                Username("new username"),
                Bio("new bio"),
                Image("new image")
            )
            every { router.updateCurrentUser(any(), any()) } returns user

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
                .header("Authorization", "Token ${generateTestToken().value}")
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
            resp.status.shouldBe(Status.OK)
            resp.expectJsonResponse(expectedResponseBody)
        }
    }
}
