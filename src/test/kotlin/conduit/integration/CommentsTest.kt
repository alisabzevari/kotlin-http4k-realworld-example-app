package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class CommentsTest: StringSpec() {
    private val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
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

        "Get article comments" {
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
            val createRequest = Request(Method.POST, "$baseUrl/api/articles/article-title/comments")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
                .body(requestBody)

            send(createRequest).status.shouldBe(Status.OK)

            val request = Request(Method.GET, "$baseUrl/api/articles/article-title/comments")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "comments": [{
                  "body": "test comment body",
                  "author": {
                    "username": "johnjacob",
                    "bio": "",
                    "image": null,
                    "following": false
                  }
                }]
              }
            """.trimIndent().toJsonTree()

            responseBody.shouldContainJsonNode(expectedResponse)
        }

        "Delete a comment" {
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
            val createRequest = Request(Method.POST, "$baseUrl/api/articles/article-title/comments")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
                .body(requestBody)
            val createResponse = send(createRequest)
            createResponse.status.shouldBe(Status.OK)
            val commentId = createResponse.bodyString().toJsonTree()["comment"]["id"]

            val getRequest = Request(Method.GET, "$baseUrl/api/articles/article-title/comments")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")

            val getResponse1 = send(getRequest)
            val response1Body = getResponse1.bodyString().toJsonTree()["comments"]
            @Language("JSON")
            val expectedResponse1 = """
              [{
                  "body": "test comment body"
              }]
            """.trimIndent().toJsonTree()
            response1Body.shouldContainJsonNode(expectedResponse1)

            val request = Request(Method.DELETE, "$baseUrl/api/articles/article-title/comments/$commentId")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
            val response = send(request)
            response.status.shouldBe(Status.OK)

            val getResponse2 = send(getRequest)
            val responseBody = getResponse2.bodyString().toJsonTree()["comments"]
            @Language("JSON")
            val expectedResponse = """[]""".trimIndent().toJsonTree()
            responseBody.shouldContainJsonNode(expectedResponse)
        }
    }
}
