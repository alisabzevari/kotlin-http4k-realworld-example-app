package conduit.util

import conduit.model.Token
import conduit.util.ConduitJackson.auto
import io.jsonwebtoken.Claims
import org.http4k.core.*
import org.http4k.lens.Header
import org.http4k.lens.RequestContextLens
import org.slf4j.LoggerFactory

open class HttpException(val status: Status, message: String) : RuntimeException(message)
data class GenericErrorModelBody(val body: List<String>)
data class GenericErrorModel(val errors: GenericErrorModelBody)

object CatchHttpExceptions {
    val logger = LoggerFactory.getLogger(CatchHttpExceptions::class.java)

    operator fun invoke() = Filter { next ->
        {
            try {
                next(it)
            } catch (e: HttpException) {
                logger.error("Uncaught error: ", e)
                createErrorResponse(e.status, listOf(e.message ?: "Oops!"))
            } catch (e: Exception) {
                logger.error("Uncaught error: ", e)
                createErrorResponse(Status(422, "Unprocessable Entity"), listOf("Unexpected error"))
            }
        }
    }
}

object TokenAuth {
    data class TokenInfo(val token: Token, val claims: Claims)

    fun extract(headerValue: String) : TokenInfo {
        if (headerValue.substring(0..5).toLowerCase() != "token ") throw Exception()
        val token = Token(headerValue.substring(6))
        return TokenInfo(token, token.parse())
    }

    private val tokenInfoLens = Header
        .map(TokenAuth::extract)
        .required("Authorization")

    operator fun invoke(tokenInfoKey: RequestContextLens<TokenInfo>) = Filter { next ->
        {
            try {
                next(it.with(tokenInfoKey of tokenInfoLens(it)))
            } catch (ex: Exception) {
                Response(Status.UNAUTHORIZED)
            }
        }
    }
}

private val error = Body.auto<GenericErrorModel>().toLens()

fun createErrorResponse(status: Status, errorMessages: List<String>) =
    Response(status).with(error of GenericErrorModel(GenericErrorModelBody(errorMessages)))
