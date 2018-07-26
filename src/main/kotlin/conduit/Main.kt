package conduit

import conduit.handlers.LoginHandler
import conduit.model.LoginRequest
import conduit.model.LoginResponse
import conduit.repository.ConduitRepository
import org.http4k.core.Body
import org.http4k.core.Method
import org.http4k.core.Response
import org.http4k.core.Status.Companion.OK
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jetbrains.exposed.sql.Database

// TODO: Create filter for authentication
// TODO: Add error handling filter

// TODO: Fix content type of request
// TODO: Configure json serializer correctly
// TODO: Define a better place for request lenses
// TODO: Add log4j


fun main(args: Array<String>) {

    val database = Database.connect("jdbc:h2:~/conduit", driver = "org.h2.Driver")

    val repository = ConduitRepository(database)

    val loginHandler = LoginHandler(repository)

    val app = routes(
        "/healthcheck" bind Method.GET to { _ -> Response(OK).body("OK") },
        "/api/users/login" bind Method.POST to { req ->
            val reqLens = Body.auto<LoginRequest>().toLens()

            val result = loginHandler(reqLens.extract(req).user)

            val resLens = Body.auto<LoginResponse>().toLens()

            resLens.inject(LoginResponse(result), Response(OK))
        }
    )

    app.asServer(Jetty(9000)).start().block()
}