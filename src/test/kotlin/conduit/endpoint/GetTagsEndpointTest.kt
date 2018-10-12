package conduit.endpoint

import conduit.Router
import conduit.TagsResponse
import conduit.handler.MultipleArticles
import conduit.model.ArticleTag
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GetTagsEndpointTest {
    lateinit var router: Router

    @BeforeEach
    fun beforeEach() {
        router = getRouterToTest()
    }

    @Test
    fun `should return all tags`() {
        every { router.getTags() } returns listOf(ArticleTag("tag-1"), ArticleTag("tag-2"))

        val request = Request(Method.GET, "/api/tags")

        val resp = router()(request)

        @Language("JSON")
        val expectedResponseBody = """
            {
              "tags": ["tag-1", "tag-2"]
            }
        """.trimIndent()
        assertEquals(Status.OK, resp.status)
        resp.expectJsonResponse(expectedResponseBody)
    }
}