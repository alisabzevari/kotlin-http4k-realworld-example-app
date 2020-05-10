package conduit.integration

import com.fasterxml.jackson.databind.JsonNode
import conduit.IntegrationTest
import conduit.util.toJsonTree
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Response
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

fun registerUser(email: String, username: String, password: String): Response {
    @Language("JSON")
    val registerReqBody = """
                {
                  "user": {
                    "email": "$email",
                    "password": "$password",
                    "username": "$username"
                  }
                }
            """.trimIndent()
    val send = ApacheClient()
    val resp = send(
        Request(
            Method.POST,
            "http://localhost:${IntegrationTest.app.config.port}/api/users"
        ).body(registerReqBody)
    )
    resp.status.shouldBe(Status.CREATED)
    return resp
}

fun login(email: String, password: String): String {
    @Language("JSON")
    val loginReqBody = """
              {
                "user": {
                  "email": "$email",
                  "password": "$password"
                }
              }
            """.trimIndent()
    val send = ApacheClient()
    val response = send(
        Request(Method.POST, "http://localhost:${IntegrationTest.app.config.port}/api/users/login").body(loginReqBody)
    )
    return response.bodyString().toJsonTree()["user"]["token"].asText()
}

fun createArticle(
    token: String,
    title: String = "article title",
    tags: List<String> = listOf("tag-1", "tag-2")
): JsonNode {
    @Language("JSON")
    val requestBody = """
              {
                "article": {
                  "title": "$title",
                  "description": "article description",
                  "body": "article body",
                  "tagList": [${tags.joinToString(",") { "\"$it\"" }}]
                }
              }
            """.trimIndent()
    val request = Request(Method.POST, "http://localhost:${IntegrationTest.app.config.port}/api/articles/")
        .header("Content-Type", "application/json")
        .header("X-Requested-With", "XMLHttpRequest")
        .header("Authorization", "Token $token")
        .body(requestBody)

    val send = ApacheClient()
    val response = send(request)
    response.status.shouldBe(Status.CREATED)
    return response.bodyString().toJsonTree()
}
