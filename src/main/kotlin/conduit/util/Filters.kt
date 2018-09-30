package conduit.util

import conduit.model.Token
import io.jsonwebtoken.Claims
import org.http4k.core.*
import org.http4k.lens.RequestContextLens

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

object TokenAuth {
    data class TokenInfo(val token: Token, val claims: Claims)

    operator fun invoke(tokenInfoKey: RequestContextLens<TokenInfo>) = Filter { next ->
        {
            try {
                val authHeader = it.header("Authorization")!!

                if (authHeader.substring(0..6).toLowerCase() != "token: ") {
                    throw Exception("")
                }

                val token = Token(authHeader.substring(7))
                val tokenInfo = TokenInfo(token, token.parse())
                val req = it.with(tokenInfoKey of tokenInfo)

                next(req)
            } catch (ex: Exception) {
                Response(Status.UNAUTHORIZED)
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
