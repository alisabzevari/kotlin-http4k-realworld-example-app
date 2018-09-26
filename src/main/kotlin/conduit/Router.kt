package conduit

import conduit.handlers.LoginHandler
import conduit.model.LoginRequest
import conduit.model.LoginResponse
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes

class Router(
    val loginHandler: LoginHandler
) {
    operator fun invoke() =
        ServerFilters.CatchLensFailure.then(
            routes(
                "/healthcheck" bind Method.GET to healthCheck(),
                "/api/users/login" bind Method.POST to login(loginHandler)
            )
        )

    fun healthCheck() = { _: Request -> Response(Status.OK).body("OK") }

    fun login(loginHandler: LoginHandler) = { req: Request ->
        val reqLens = Body.auto<LoginRequest>().toLens()

        val result = loginHandler(reqLens.extract(req).user)

        val resLens = Body.auto<LoginResponse>().toLens()

        resLens.inject(LoginResponse(result), Response(Status.OK))
    }
}

