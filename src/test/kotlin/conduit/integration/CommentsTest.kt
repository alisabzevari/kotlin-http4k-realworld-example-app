package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class CommentsTest: StringSpec() {
    val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "Create a comment for an article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            createArticle(token)

            @Language("JSON")
            val requestBody = """
              {
                "comment": {
                  "body": "test comment body"
                }
              }
            """.trimIndent()
            val request = Request(Method.POST, "$baseUrl/api/articles/article-title/comments")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
                .body(requestBody)

            val response = send(request)
            response.status.shouldBe(Status.OK)
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "comment": {
                  "body": "test comment body",
                  "author": {
                    "username": "johnjacob",
                    "bio": "",
                    "image": null,
                    "following": false
                  }
                }
              }
            """.trimIndent().toJsonTree()

            responseBody.shouldContainJsonNode(expectedResponse)
            responseBody["comment"]["createdAt"].isTextual.shouldBeTrue()
            responseBody["comment"]["updatedAt"].isTextual.shouldBeTrue()
            responseBody["comment"]["id"].isIntegralNumber.shouldBeTrue()
        }
    }

    fun createArticle(token: String) {
        @Language("JSON")
        val requestBody = """
              {
                "article": {
                  "title": "article title",
                  "description": "article description",
                  "body": "article body",
                  "tagList": ["tag-1", "tag-2"]
                }
              }
            """.trimIndent()
        val request = Request(Method.POST, "$baseUrl/api/articles/")
            .header("Content-Type", "application/json")
            .header("X-Requested-With", "XMLHttpRequest")
            .header("Authorization", "Token $token")
            .body(requestBody)

        val response = send(request)
        response.status.shouldBe(Status.CREATED)
    }
}