package conduit.endpoint

import conduit.Router
import conduit.handler.UserDto
import conduit.model.Bio
import conduit.model.Email
import conduit.model.Token
import conduit.model.Username
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class GetCurrentUserEndpointTest : StringSpec() {
    lateinit var router: Router

    override fun beforeTest(testCase: TestCase) {
        router = getRouterToTest()
    }

    init {
        "should return current user information" {
            every { router.getCurrentUser(any()) } returns UserDto(
                Email("jake@jake.jake"),
                Token("jwt.token.here"),
                Username("jake"),
                Bio("I work at statefarm"),
                null
            )

            val request =
                Request(Method.GET, "/api/users").header("Authorization", "Token ${generateTestToken().value}")

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
            resp.status.shouldBe(Status.OK)
            resp.expectJsonResponse(expectedResponseBody)
        }
    }
}
