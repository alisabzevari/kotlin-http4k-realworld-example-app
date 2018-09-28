package conduit.util

import org.http4k.core.Filter
import org.http4k.core.Response
import org.http4k.core.Status

open class HttpException(val status: Status, message: String) : RuntimeException(message)

data class GenericErrorModelBody(val body: List<String>)
data class GenericErrorModel(val errors: GenericErrorModelBody)

object CatchHttpExceptions {
    operator fun invoke() = Filter { next ->
        {
            try {
                next(it)
            } catch (e: HttpException) {
                createResponse(e.status, e.message)
            } catch (e: Exception) {
                createResponse(Status(422, "Unprocessable Entity"), "Unexpected error")
            }
        }
    }

    private fun createResponse(status: Status, message: String?) =
        Response(status).body(
            GenericErrorModel(
                GenericErrorModelBody(if (message != null) listOf(message) else emptyList())
            ).stringifyAsJson()
        )
}