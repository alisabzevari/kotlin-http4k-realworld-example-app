package conduit.endpoint

import conduit.Router
import conduit.model.ArticleTag
import io.kotest.core.spec.style.StringSpec
import io.kotest.core.test.TestCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class GetTagsEndpointTest : StringSpec() {
    lateinit var router: Router

    override fun beforeTest(testCase: TestCase) {
        router = getRouterToTest()
    }

    init {
        "should return all tags" {
            every { router.getTags() } returns listOf(ArticleTag("tag-1"), ArticleTag("tag-2"))

            val request = Request(Method.GET, "/api/tags")

            val resp = router()(request)

            @Language("JSON")
            val expectedResponseBody = """
                {
                  "tags": ["tag-1", "tag-2"]
                }
            """.trimIndent()
            resp.status.shouldBe(Status.OK)
            resp.expectJsonResponse(expectedResponseBody)
        }
    }
}
