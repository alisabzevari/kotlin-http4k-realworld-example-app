package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class AuthTest : StringSpec() {
    private val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
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

        "Current User" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val request = Request(Method.GET, "$baseUrl/api/user")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")

            val response = send(request)

            response.status.shouldBe(Status.OK)
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
            responseBody.shouldContainJsonNode(expectedResponse)
            responseBody["user"]["token"].isValueNode.shouldBeTrue()
        }

        "Update User" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val newEmail = "jjacob2@gmail.com"

            @Language("JSON")
            val reqBody = """
              {
                "user": {
                  "email": "$newEmail"
                }
              }
            """.trimIndent()
            val request = Request(Method.PUT, "$baseUrl/api/user")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
                .body(reqBody)

            val response = send(request)
            response.status.shouldBe(Status.OK)
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "user": {
                  "email": "$newEmail",
                  "username": "johnjacob",
                  "bio": "",
                  "image": null
                }
              }
            """.trimIndent().toJsonTree()
            responseBody.shouldContainJsonNode(expectedResponse)
        }
    }
}
