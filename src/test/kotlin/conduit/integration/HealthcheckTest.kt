package conduit.integration

import conduit.IntegrationTest
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status

class HealthcheckTest: StringSpec() {
    init {
        val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
        val send = ApacheClient()

        "healthcheck should work" {
            val response = send(Request(Method.GET, "$baseUrl/healthcheck"))

            response.status.shouldBe(Status.OK)
        }
    }
}
