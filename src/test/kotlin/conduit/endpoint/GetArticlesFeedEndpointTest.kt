package conduit.endpoint

import conduit.Router
import conduit.handler.MultipleArticles
import io.mockk.every
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import kotlin.test.assertEquals

class GetArticlesFeedEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should return most recent articles feed for user`() {
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
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }
}