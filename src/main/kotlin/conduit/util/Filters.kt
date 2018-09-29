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
                createErrorResponse(e.status, listOf(e.message ?: "Oops!"))
            } catch (e: Exception) {
                createErrorResponse(Status(422, "Unprocessable Entity"), listOf("Unexpected error"))
            }
        }
    }
}

fun createErrorResponse(status: Status, errorMessages: List<String>) =
    Response(status)
        .header("Content-Type", "application/json; charset=utf-8")
        .body(
            GenericErrorModel(
                GenericErrorModelBody(errorMessages)
            ).stringifyAsJson()
        )
