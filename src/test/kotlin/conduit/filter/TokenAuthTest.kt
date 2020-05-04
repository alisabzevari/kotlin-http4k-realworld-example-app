package conduit.filter

import conduit.endpoint.generateTestToken
import conduit.endpoint.jwtTestSigningKey
import conduit.util.HttpException
import conduit.util.TokenAuth
import io.kotlintest.matchers.string.shouldMatch
import io.kotlintest.shouldBe
import io.kotlintest.shouldThrow
import io.kotlintest.specs.StringSpec
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.routing.bind
import org.http4k.routing.routes

class TokenAuthTest: StringSpec() {
    private val contexts = RequestContexts()
    private val key = RequestContextKey.required<TokenAuth.TokenInfo>(contexts)
    private val tokenAuth = TokenAuth(jwtTestSigningKey)

    private val router = ServerFilters.InitialiseRequestContext(contexts)
        .then(tokenAuth(tokenInfoKey = key))
        .then(
            routes("/auth" bind Method.GET to { req -> Response(Status.OK).body(key(req).claims.entries.toString()) })
        )

    init {
        "empty header" {
            val exception = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth"))
            }

            exception.status.shouldBe(Status.UNAUTHORIZED)
        }

        "empty authorization header" {
            val exception = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", ""))
            }

            exception.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with incorrect format - 1" {
            val exception = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", "Basic test"))
            }

            exception.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with incorrect format - 2" {
            val exception = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", "Tokenssdsd"))
            }

            exception.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with invalid token" {
            val exception = shouldThrow<HttpException> {
                router(
                    Request(Method.GET, "/auth").header(
                        "Authorization",
                        "Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFsaSIsImVtYWlsIjoiYWxpc2FiemV2YXJzaUBnbWFpbC5jb20iLCJleHAiOjE1MzgyOTUyMzh9.jQlVD0b9Q2R0HYkiC6LHXgIm6VBcvBq9mOFGQVUgYNg"
                    )
                )
            }

            exception.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with valid token" {
            val token = generateTestToken()

            val res = router(Request(Method.GET, "/auth").header("Authorization", "Token ${token.value}"))

            res.status.shouldBe(Status.OK)
            res.bodyString().shouldMatch(Regex("\\[username=ali, email=alisabzevari@gmail.com, exp=\\d*]"))
        }
    }
}
