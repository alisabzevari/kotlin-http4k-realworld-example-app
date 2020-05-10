package conduit.endpoint

import conduit.Router
import conduit.handler.UserDto
import conduit.model.Bio
import conduit.model.Email
import conduit.model.Token
import conduit.model.Username
import conduit.util.HttpException
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class RegistrationEndpointTest : StringSpec() {
    lateinit var router: Router

    override fun beforeTest(testCase: TestCase) {
        router = getRouterToTest()
    }

    init {
        "should return User on successful registration" {
            every { router.registerUser(any()) } returns UserDto(
                Email("jake@jake.jake"),
                Token("jwt.token.here"),
                Username("jake"),
                Bio("I work at statefarm"),
                null
            )

            @Language("JSON")
            val requestBody = """
                {
                  "user":{
                    "username": "Jacob",
                    "email": "jake@jake.jake",
                    "password": "jakejake"
                  }
                }
            """.trimIndent()
            val request = Request(Method.POST, "/api/users").body(requestBody)

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
            resp.status.shouldBe(Status.CREATED)
            resp.expectJsonResponse(expectedResponseBody)
        }

        "should return CONFLICT if user already exist" {
            every { router.registerUser(any()) } throws HttpException(
                Status.CONFLICT,
                "The specified user already exists."
            )

            @Language("JSON")
            val requestBody = """
                {
                  "user":{
                    "username": "Jacob",
                    "email": "jake@jake.jake",
                    "password": "jakejake"
                  }
                }
            """.trimIndent()
            val request = Request(Method.POST, "/api/users").body(requestBody)

            val resp = router()(request)

            @Language("JSON")
            val expectedResponseBody = """
                {
                  "errors": {
                    "body": [
                      "The specified user already exists."
                    ]
                  }
                }
            """.trimIndent()
            resp.expectJsonResponse(expectedResponseBody)
            resp.status.shouldBe(Status.CONFLICT)
        }
    }
}
