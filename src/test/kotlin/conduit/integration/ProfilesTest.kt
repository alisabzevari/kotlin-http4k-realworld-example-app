package conduit.integration

import conduit.IntegrationTest
import conduit.util.toJsonTree
import conduit.utils.shouldContainJsonNode
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status
import org.intellij.lang.annotations.Language

class ProfilesTest : StringSpec() {
    private val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
    val send = ApacheClient()

    init {
        "Profile" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")
            val request = Request(Method.GET, "$baseUrl/api/profiles/johnjacob")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "profile": {
                  "username": "johnjacob",
                  "bio": "",
                  "image": null,
                  "following": false
                }
              }
            """.trimIndent().toJsonTree()
            responseBody.shouldContainJsonNode(expectedResponse)
        }

        "Follow Profile" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")

            registerUser("rick@gmail.com", "rick", "rck")

            val request = Request(Method.POST, "$baseUrl/api/profiles/rick/follow")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")

            val response = send(request)
            response.status.shouldBe(Status.OK)
            val responseBody = response.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "profile": {
                  "username": "rick",
                  "bio": "",
                  "image": null,
                  "following": true
                }
              }
            """.trimIndent().toJsonTree()
            responseBody.shouldContainJsonNode(expectedResponse)
        }

        "Unfollow Profile" {
            IntegrationTest.app.resetDb()
            registerUser("jjacob@gmail.com", "johnjacob", "jjcb")
            val token = login("jjacob@gmail.com", "jjcb")

            registerUser("rick@gmail.com", "rick", "rck")

            val request = Request(Method.POST, "$baseUrl/api/profiles/rick/follow")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
            val response = send(request)
            response.status.shouldBe(Status.OK)

            val unfollowReq = Request(Method.DELETE, "$baseUrl/api/profiles/rick/follow")
                .header("Content-Type", "application/json")
                .header("X-Requested-With", "XMLHttpRequest")
                .header("Authorization", "Token $token")
            val unfollowRes = send(unfollowReq)
            unfollowRes.status.shouldBe(Status.OK)
            val responseBody = unfollowRes.bodyString().toJsonTree()

            @Language("JSON")
            val expectedResponse = """
              {
                "profile": {
                  "username": "rick",
                  "bio": "",
                  "image": null,
                  "following": false
                }
              }
            """.trimIndent().toJsonTree()
            responseBody.shouldContainJsonNode(expectedResponse)
        }
    }
}
