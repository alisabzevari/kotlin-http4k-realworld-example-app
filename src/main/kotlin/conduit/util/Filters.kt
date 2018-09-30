package conduit.util

import conduit.model.Token
import io.jsonwebtoken.Claims
import org.http4k.core.*
import org.http4k.format.Jackson.auto
import org.http4k.lens.Header
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

    private val tokenInfo = Header
        .map {
            if (it.substring(0..6).toLowerCase() != "token: ") throw Exception()
            val token = Token(it.substring(7))
            TokenInfo(token, token.parse())
        }
        .required("Authorization")

    operator fun invoke(tokenInfoKey: RequestContextLens<TokenInfo>) = Filter { next ->
        {
            try {
                next(it.with(tokenInfoKey of tokenInfo(it)))
            } catch (ex: Exception) {
                Response(Status.UNAUTHORIZED)
            }
        }
    }
}

private val error = Body.auto<GenericErrorModel>().toLens()

fun createErrorResponse(status: Status, errorMessages: List<String>) =
    Response(status).with(error of GenericErrorModel(GenericErrorModelBody(errorMessages)))
