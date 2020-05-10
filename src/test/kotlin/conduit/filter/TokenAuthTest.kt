package conduit.filter

import conduit.endpoint.generateTestToken
import conduit.endpoint.jwt
import conduit.util.HttpException
import conduit.util.TokenAuth
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldMatch
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.routing.bind
import org.http4k.routing.routes

class TokenAuthTest : StringSpec() {
    private val contexts = RequestContexts()
    private val tokenAuth = TokenAuth(jwt, contexts)

    private val router = ServerFilters.InitialiseRequestContext(contexts)
        .then(
            routes(
                "/auth" bind Method.GET to tokenAuth.required()
                    .then { Response(Status.OK).body(tokenAuth.getToken(it).claims.entries.toString()) },
                "/optional-auth" bind Method.GET to tokenAuth.optional()
                    .then { Response(Status.OK).body(tokenAuth.getOptionalToken(it)?.claims?.entries.toString()) }
            )
        )

    init {
        "empty header" {
            val exception = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth"))
            }
            exception.status.shouldBe(Status.UNAUTHORIZED)

            val response = router(Request(Method.GET, "/optional-auth"))
            response.status.shouldBe(Status.OK)
        }

        "empty authorization header" {
            val exception1 = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", ""))
            }
            exception1.status.shouldBe(Status.UNAUTHORIZED)

            val exception2 = shouldThrow<HttpException> {
                router(Request(Method.GET, "/optional-auth").header("Authorization", ""))
            }
            exception2.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with incorrect format - 1" {
            val exception1 = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", "Basic test"))
            }
            exception1.status.shouldBe(Status.UNAUTHORIZED)

            val exception2 = shouldThrow<HttpException> {
                router(Request(Method.GET, "/optional-auth").header("Authorization", "Basic test"))
            }
            exception2.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with incorrect format - 2" {
            val exception1 = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", "Tokenssdsd"))
            }
            exception1.status.shouldBe(Status.UNAUTHORIZED)

            val exception2 = shouldThrow<HttpException> {
                router(Request(Method.GET, "/auth").header("Authorization", "Tokenssdsd"))
            }
            exception2.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with invalid token" {
            val exception1 = shouldThrow<HttpException> {
                router(
                    Request(Method.GET, "/auth").header(
                        "Authorization",
                        "Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFsaSIsImVtYWlsIjoiYWxpc2FiemV2YXJzaUBnbWFpbC5jb20iLCJleHAiOjE1MzgyOTUyMzh9.jQlVD0b9Q2R0HYkiC6LHXgIm6VBcvBq9mOFGQVUgYNg"
                    )
                )
            }
            exception1.status.shouldBe(Status.UNAUTHORIZED)

            val exception2 = shouldThrow<HttpException> {
                router(
                    Request(Method.GET, "/optional-auth").header(
                        "Authorization",
                        "Token eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFsaSIsImVtYWlsIjoiYWxpc2FiemV2YXJzaUBnbWFpbC5jb20iLCJleHAiOjE1MzgyOTUyMzh9.jQlVD0b9Q2R0HYkiC6LHXgIm6VBcvBq9mOFGQVUgYNg"
                    )
                )
            }
            exception2.status.shouldBe(Status.UNAUTHORIZED)
        }

        "authorization header with valid token" {
            val token = generateTestToken()
            val res1 = router(Request(Method.GET, "/auth").header("Authorization", "Token ${token.value}"))
            res1.status.shouldBe(Status.OK)
            res1.bodyString().shouldMatch(Regex("\\[username=ali, email=alisabzevari@gmail.com, exp=\\d*]"))

            val res2 = router(Request(Method.GET, "/optional-auth").header("Authorization", "Token ${token.value}"))
            res2.status.shouldBe(Status.OK)
            res2.bodyString().shouldMatch(Regex("\\[username=ali, email=alisabzevari@gmail.com, exp=\\d*]"))
        }
    }
}
