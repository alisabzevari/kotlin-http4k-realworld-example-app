package conduit.util

import conduit.model.Email
import conduit.model.Token
import conduit.util.ConduitJackson.auto
import io.jsonwebtoken.Claims
import org.http4k.core.*
import org.http4k.lens.Header
import org.http4k.lens.RequestContextLens
import org.slf4j.LoggerFactory

open class HttpException(val status: Status, message: String = status.description) : RuntimeException(message)
data class GenericErrorModelBody(val body: List<String>)
data class GenericErrorModel(val errors: GenericErrorModelBody)

object CatchHttpExceptions {
    private val logger = LoggerFactory.getLogger(CatchHttpExceptions::class.java)

    operator fun invoke() = Filter { next ->
        {
            try {
                next(it)
            } catch (e: HttpException) {
                logger.error("Uncaught error: ", e)
                createErrorResponse(e.status, listOf(e.message ?: "Oops!"))
            } catch (e: Throwable) {
                logger.error("Uncaught error: ", e)
                createErrorResponse(Status(422, "Unprocessable Entity"), listOf("Unexpected error"))
            }
        }
    }
}

fun extract(headerValue: String, jwt: JWT) : TokenAuth.TokenInfo {
    if (headerValue.substring(0..5).toLowerCase() != "token ") throw Exception()
    val token = Token(headerValue.substring(6))
    return TokenAuth.TokenInfo(token, jwt.parse(token))
}

class TokenAuth(private val jwt: JWT) {

    data class TokenInfo(val token: Token, val claims: Claims)

    private val tokenInfoLens = Header
        .map { extract(headerValue = it, jwt = jwt) }
        .required("Authorization")

    operator fun invoke(tokenInfoKey: RequestContextLens<TokenInfo>) = Filter { next ->
        {
            val tokenInfo = try {
                tokenInfoLens(it)
            } catch (ex: Exception) {
                throw HttpException(Status.UNAUTHORIZED)
            }
            next(it.with(tokenInfoKey of tokenInfo))
        }
    }
}

private val error = Body.auto<GenericErrorModel>().toLens()

fun createErrorResponse(status: Status, errorMessages: List<String>) =
    Response(status).with(error of GenericErrorModel(GenericErrorModelBody(errorMessages)))

fun TokenAuth.TokenInfo.extractEmail() = Email(claims["email"].toString())
