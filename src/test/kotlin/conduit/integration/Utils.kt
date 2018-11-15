package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import io.kotlintest.shouldBe
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
    val resp = send(Request(Method.POST, "http://localhost:${IntegrationTest.app.config.port}/api/users").body(registerReqBody))
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
    val response = send(Request(Method.POST, "http://localhost:${IntegrationTest.app.config.port}/api/users/login").body(loginReqBody))
    return response.bodyString().toJsonTree()["user"]["token"].asText()
}
