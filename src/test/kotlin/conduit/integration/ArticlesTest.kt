package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotlintest.matchers.boolean.shouldBeFalse
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class ArticlesTest : StringSpec() {
    private val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "POST article" {
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
            responseBody["article"]["createdAt"].isTextual.shouldBeTrue()
            responseBody["article"]["updatedAt"].isTextual.shouldBeTrue()
        }

        "favorite an article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article = createArticle(token)

            val request = Request(Method.POST, "$baseUrl/api/articles/${article["article"]["slug"].asText()}/favorite")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)
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
                  "favorited": true,
                  "favoritesCount": 1,
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
            responseBody["article"]["createdAt"].isTextual.shouldBeTrue()
            responseBody["article"]["updatedAt"].isTextual.shouldBeTrue()
        }

        "unfavorite an article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article = createArticle(token)

            val favoriteRequest =
                Request(Method.POST, "$baseUrl/api/articles/${article["article"]["slug"].asText()}/favorite")
                    .header("Content-Type", "application/json")
                    .header("X-Requested-With", "XMLHttpRequest")
                    .header("Authorization", "Token $token")
            send(favoriteRequest).status.shouldBe(Status.OK)

            val request =
                Request(Method.DELETE, "$baseUrl/api/articles/${article["article"]["slug"].asText()}/favorite")
                    .header("Authorization", "Token $token")
            val response = send(request)
            response.status.shouldBe(Status.OK)
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
            responseBody["article"]["createdAt"].isTextual.shouldBeTrue()
            responseBody["article"]["updatedAt"].isTextual.shouldBeTrue()
        }

        "Delete an article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article = createArticle(token)

            articleExists(article["article"]["slug"].asText()).shouldBeTrue()

            val request = Request(Method.DELETE, "$baseUrl/api/articles/${article["article"]["slug"].asText()}/")
                .header("Authorization", "Token $token")
            val response = send(request)
            response.status.shouldBe(Status.OK)

            articleExists(article["article"]["slug"].asText()).shouldBeFalse()
        }

        "Get an article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article = createArticle(token)

            val request = Request(Method.GET, "$baseUrl/api/articles/${article["article"]["slug"].asText()}")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)

            response.bodyString().toJsonTree().shouldContainJsonNode(article)
        }

        "Update an article" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val article = createArticle(token)
            val articleSlug = article["article"]["slug"].asText()

            @Language("JSON")
            val updateArticle = """
                {
                  "article": {
                    "title": "title 2",
                    "description": "description 2",
                    "body": "body 2"
                  }
                }
            """.trimIndent()

            val request = Request(Method.PUT, "$baseUrl/api/articles/$articleSlug")
                .header("Authorization", "Token $token")
                .body(updateArticle)

            val response = send(request)
            response.status.shouldBe(Status.OK)

            val result = IntegrationTest.app.db.connector().createStatement().executeQuery(
                "SELECT * FROM Articles WHERE slug = '$articleSlug'"
            )
            result.next()

            result.getString("title").shouldBe("title 2")
            result.getString("description").shouldBe("description 2")
            result.getString("body").shouldBe("body 2")
        }
    }

    private fun articleExists(slug: String): Boolean {
        val result = IntegrationTest.app.db.connector().createStatement().executeQuery(
            "SELECT COUNT(1) FROM Articles WHERE slug = '$slug'"
        )
        result.next()

        return result.getInt(1) == 1
    }
}