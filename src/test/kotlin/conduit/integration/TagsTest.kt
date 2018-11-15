package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class TagsTest: StringSpec() {
    val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "All Tags" {
            val request = Request(Method.GET, "$baseUrl/api/tags")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")

            val response = send(request)
            response.status.shouldBe(Status.OK)
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "tags": []
              }
            """.trimIndent().toJsonTree()
            responseBody.shouldContainJsonNode(expectedResponse)
        }
    }
}