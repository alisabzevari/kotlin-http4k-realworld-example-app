package conduit.filter

import conduit.Router
import conduit.model.Email
import conduit.model.Username
import conduit.util.TokenAuth
import conduit.util.generateToken
import io.jsonwebtoken.Claims
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.lens.RequestContextKey
import org.http4k.lens.RequestContextLens
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TokenAuthTest {
    lateinit var contexts: RequestContexts
    lateinit var router: RoutingHttpHandler

    @BeforeEach
    fun beforeEach() {
        contexts = RequestContexts()
        val key = RequestContextKey.required<TokenAuth.TokenInfo>(contexts)
        router = ServerFilters.InitialiseRequestContext(contexts)
            .then(TokenAuth(key))
            .then(
                routes("/auth" bind Method.GET to { req -> Response(Status.OK).body(key.extract(req).claims.entries.toString()) })
            )
    }

    @Test
    fun `empty header`() {
        val res = router(Request(Method.GET, "/auth"))

        assertEquals(Status.UNAUTHORIZED, res.status)
    }

    @Test
    fun `empty authorization header`() {
        val res = router(Request(Method.GET, "/auth").header("Authorization", ""))

        assertEquals(Status.UNAUTHORIZED, res.status)
    }

    @Test
    fun `authorization header with incorrect format - 1`() {
        val res = router(Request(Method.GET, "/auth").header("Authorization", "Basic test"))

        assertEquals(Status.UNAUTHORIZED, res.status)
    }

    @Test
    fun `authorization header with incorrect format - 2`() {
        val res = router(Request(Method.GET, "/auth").header("Authorization", "Token:ssdsd"))

        assertEquals(Status.UNAUTHORIZED, res.status)
    }

    @Test
    fun `authorization header with invalid token`() {
        val res = router(
            Request(Method.GET, "/auth").header(
                "Authorization",
                "Token: eyJhbGciOiJIUzI1NiJ9.eyJ1c2VybmFtZSI6ImFsaSIsImVtYWlsIjoiYWxpc2FiemV2YXJzaUBnbWFpbC5jb20iLCJleHAiOjE1MzgyOTUyMzh9.jQlVD0b9Q2R0HYkiC6LHXgIm6VBcvBq9mOFGQVUgYNg"
            )
        )

        assertEquals(Status.UNAUTHORIZED, res.status)
    }

    @Test
    fun `authorization header with valid token`() {
        val token = generateToken(Username("ali"), Email("alisabzevari@gmail.com"))

        val res = router(Request(Method.GET, "/auth").header("Authorization", "Token: ${token.value}"))

        assertEquals(Status.OK, res.status)
        assertTrue(res.bodyString().matches(Regex("\\[username=ali, email=alisabzevari@gmail.com, exp=\\d*]")))
    }
}