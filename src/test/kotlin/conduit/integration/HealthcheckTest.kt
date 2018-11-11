package conduit.integration

import conduit.IntegrationTest
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import org.http4k.client.ApacheClient
import org.http4k.core.Method
import org.http4k.core.Request
import org.http4k.core.Status

class HealthcheckTest: StringSpec() {
    init {
        val baseUrl = "http://localhost:${IntegrationTest.app.config.port}"
        val client = ApacheClient()

        "healthcheck should work" {
            val response = client(
                Request(Method.GET, "$baseUrl/healthcheck")
            )

            response.status.shouldBe(Status.OK)
        }
    }
}