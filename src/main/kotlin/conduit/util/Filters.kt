package conduit.util

import conduit.model.Email
import conduit.model.Token
import conduit.util.ConduitJackson.auto
import io.jsonwebtoken.Claims
import org.http4k.core.*
import org.http4k.lens.Header
import org.http4k.lens.RequestContextKey
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


class TokenAuth(private val jwt: JWT, contexts: RequestContexts) {
    data class TokenInfo(val token: Token, val claims: Claims)

    val getToken = RequestContextKey.required<TokenInfo>(contexts)
    val getOptionalToken = RequestContextKey.optional<TokenInfo>(contexts)

    fun required() = Filter { next ->
        { req ->
            val tokenInfo = try {
                tokenInfoLens(req)
            } catch (ex: Exception) {
                throw HttpException(Status.UNAUTHORIZED)
            }
            next(req.with(getToken of tokenInfo))
        }
    }

    fun optional() = Filter { next ->
        { req ->
            val tokenInfo = try {
                tokenInfoLensOptional(req)
            } catch (ex: Exception) {
                throw HttpException(Status.UNAUTHORIZED)
            }
            next(req.with(getOptionalToken of tokenInfo))
        }
    }

    private val tokenInfoLens = Header
        .map { extractTokenFromHeader(headerValue = it) }
        .required("Authorization")
    private val tokenInfoLensOptional = Header
        .map { extractTokenFromHeader(headerValue = it) }
        .optional("Authorization")

    private fun extractTokenFromHeader(headerValue: String): TokenInfo {
        if (headerValue.substring(0..5).toLowerCase() != "token ") throw Exception()
        val token = Token(headerValue.substring(6))
        return TokenInfo(token, jwt.parse(token))
    }


}

private val error = Body.auto<GenericErrorModel>().toLens()

fun createErrorResponse(status: Status, errorMessages: List<String>) =
    Response(status).with(error of GenericErrorModel(GenericErrorModelBody(errorMessages)))

fun TokenAuth.TokenInfo.extractEmail() = Email(claims["email"].toString())
