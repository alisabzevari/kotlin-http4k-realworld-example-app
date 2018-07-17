package conduit

import conduit.handlers.LoginHandler
import conduit.model.LoginRequest
import conduit.utils.toJson
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer


fun main(args: Array<String>) {

    val loginHandler = LoginHandler()

    val app = routes(
        "/healthcheck" bind Method.GET to { _ -> Response(OK).body("OK") },
        "/api/users/login" bind Method.POST to { req ->
            val reqLens = Body.auto<LoginRequest>().toLens()
            val result = loginHandler.handle(reqLens.extract(req).user)

            Response(OK).body(result.toJson())
        }
    )

    app.asServer(Jetty(9000)).start().block()
}