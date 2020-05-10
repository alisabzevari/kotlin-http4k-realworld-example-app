package conduit.integration

import conduit.IntegrationTest
import conduit.util.mapper
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class GetArticlesTest : StringSpec() {
    private val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "With default parameters" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article = createArticle(token)

            val request = Request(Method.GET, "$baseUrl/api/articles")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)

            val result = response.bodyString().toJsonTree()

            result["articles"][0].shouldContainJsonNode(article["article"])
            result["articlesCount"].intValue().shouldBe(1)
        }

        "filter by tag" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article1 = createArticle(token)
            val article2 = createArticle(token, "article 2", listOf("tag-2"))
            createArticle(token, "article 3", listOf("tag-3"))

            val request = Request(Method.GET, "$baseUrl/api/articles?tag=tag-2")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)

            val result = response.bodyString().toJsonTree()

            val expectedArticles = mapper.createArrayNode()
            expectedArticles.add(article2["article"])
            expectedArticles.add(article1["article"])

            result["articlesCount"].intValue().shouldBe(2)
            result["articles"].shouldContainJsonNode(expectedArticles)
        }

        "Filter by author" {
            IntegrationTest.app.resetDb()
            registerUser("user1@gmail.com", "user1", "pass1")
            createArticle(login("user1@gmail.com", "pass1"), "article user 1")

            registerUser("user2@gmail.com", "user2", "pass2")
            val token = login("user2@gmail.com", "pass2")
            val article2 = createArticle(token, "article user 2 - 1")
            val article3 = createArticle(token, "article user 2 - 2", listOf("tag-3"))

            val request = Request(Method.GET, "$baseUrl/api/articles?author=user2")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)

            val result = response.bodyString().toJsonTree()

            val expectedArticles = mapper.createArrayNode()
            expectedArticles.add(article3["article"])
            expectedArticles.add(article2["article"])

            result["articlesCount"].intValue().shouldBe(2)
            result["articles"].shouldContainJsonNode(expectedArticles)
        }

        "should return empty articles when author not found" {
            IntegrationTest.app.resetDb()
            registerUser("user1@gmail.com", "user1", "pass1")
            val token = login("user1@gmail.com", "pass1")
            createArticle(token, "article user 1")

            val request = Request(Method.GET, "$baseUrl/api/articles?author=user2")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)

            val result = response.bodyString().toJsonTree()

            val expectedArticles = mapper.createArrayNode()

            result["articlesCount"].intValue().shouldBe(0)
            result["articles"].shouldContainJsonNode(expectedArticles)
        }

        "Filter by favorited" {
            IntegrationTest.app.resetDb()
            registerUser("user1@gmail.com", "user1", "pass1")
            val user1Token = login("user1@gmail.com", "pass1")
            val article1 = createArticle(user1Token, "article user 1-1")
            createArticle(user1Token, "article user 1-2")

            registerUser("user2@gmail.com", "user2", "pass2")
            val token = login("user2@gmail.com", "pass2")

            val favRequest =
                Request(Method.POST, "$baseUrl/api/articles/${article1["article"]["slug"].asText()}/favorite")
                    .header("Content-Type", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Authorization", "Token $token")
            val favResponse = send(favRequest)
            favResponse.status.shouldBe(Status.OK)

            val request = Request(Method.GET, "$baseUrl/api/articles?favorited=user2")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)

            val result = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedArticles = """
                [{
                  "slug": "article-user-1-1",
                  "title": "article user 1-1",
                  "description": "article description",
                  "body": "article body",
                  "tagList": ["tag-1", "tag-2"],
                  "favorited": true,
                  "favoritesCount": 1,
                  "author": {
                    "username": "user1",
                    "bio": "",
                    "image": null,
                    "following": false
                  }
                }]
            """.trimIndent().toJsonTree()

            result["articlesCount"].intValue().shouldBe(1)
            result["articles"].shouldContainJsonNode(expectedArticles)
        }
    }
}
