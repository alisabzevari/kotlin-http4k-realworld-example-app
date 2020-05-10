package conduit.endpoint

import conduit.Router
import conduit.model.Bio
import conduit.model.Image
import conduit.model.Profile
import conduit.model.Username
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class UnfollowUserEndpointTest : StringSpec() {
    lateinit var router: Router

    override fun beforeTest(testCase: TestCase) {
        router = getRouterToTest()
    }

    init {
        "should unfollow the user" {
            every { router.unfollowUser(any(), any()) } returns Profile(
                Username("jake"),
                Bio("I work at statefarm"),
                Image("Image"),
                false
            )

            val request = Request(Method.DELETE, "/api/profiles/user1/follow")
                .header("Authorization", "Token ${generateTestToken().value}")

            val resp = router()(request)

            @Language("JSON")
            val expectedResponseBody = """
                {
                  "profile": {
                    "username": "jake",
                    "bio": "I work at statefarm",
                    "image": "Image",
                    "following": false
                  }
                }
            """.trimIndent()
            resp.status.shouldBe(Status.OK)
            resp.expectJsonResponse(expectedResponseBody)
        }
    }
}
