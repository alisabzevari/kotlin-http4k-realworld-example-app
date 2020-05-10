package conduit.endpoint

import conduit.Router
import conduit.handler.MultipleArticles
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class GetArticlesFeedEndpointTest : StringSpec() {
    lateinit var router: Router

    override fun beforeTest(testCase: TestCase) {
        router = getRouterToTest()
    }

    init {
        "should return most recent articles feed for user" {
            every { router.getArticlesFeed(any(), any(), any()) } returns MultipleArticles(
                emptyList(),
                10
            )

            val request = Request(Method.GET, "/api/articles/feed")
                .header("Authorization", "Token ${generateTestToken().value}")

            val resp = router()(request)

            @Language("JSON")
            val expectedResponseBody = """
            {
              "articles": [],
              "articlesCount": 10
            }
        """.trimIndent()
            resp.status.shouldBe(Status.OK)
            resp.expectJsonResponse(expectedResponseBody)
        }
    }
}
