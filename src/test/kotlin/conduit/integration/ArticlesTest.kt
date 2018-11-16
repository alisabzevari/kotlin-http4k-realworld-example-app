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

class ArticlesTest: StringSpec() {
    val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "POST Article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")

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
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "article": {
                  "slug": "article-title",
                  "title": "article title",
                  "description": "article description",
                  "body": "article body",
                  "tagList": ["tag-1", "tag-2"],
                  "createdAt": "",
                  "updatedAt": "",
                  "favorited": false,
                  "favoritesCount": 0,
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
        }
    }
}