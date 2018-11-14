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
import org.http4k.core.Response
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class AuthTest : StringSpec() {
    val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "Register" {
            IntegrationTest.app.resetDb()

            val response = registerUser("jjacob@gmail.com", "johnjacob", "jjcb")

            @Language("JSON")
            val expectedResponse = """
            {
              "user": {
                "email": "jjacob@gmail.com",
                "username": "johnjacob",
                "bio": null,
                "image": null
              }
            }""".trimIndent().toJsonTree()
            val responseBody = response.bodyString().toJsonTree()

            responseBody.shouldContainJsonNode(expectedResponse)
            responseBody["user"]["token"].isValueNode.shouldBeTrue()
        }

        "Login" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")

            @Language("JSON")
            val loginReqBody = """
              {
                "user": {
                  "email": "jjacob@gmail.com",
                  "password": "jjcb"
                }
              }
            """.trimIndent()

            val response = send(Request(Method.POST, "$baseUrl/api/users/login").body(loginReqBody))
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "user": {
                  "email": "jjacob@gmail.com",
                  "username": "johnjacob",
                  "bio": "",
                  "image": null
                }
              }
            """.trimIndent().toJsonTree()
            response.status.shouldBe(Status.OK)
            responseBody.shouldContainJsonNode(expectedResponse)
            responseBody["user"]["token"].isValueNode.shouldBeTrue()
        }
    }

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
        val resp = send(Request(Method.POST, "$baseUrl/api/users").body(registerReqBody))
        resp.status.shouldBe(Status.CREATED)
        return resp
    }
}
