package conduit

import conduit.handlers.*
import conduit.utils.CatchHttpExceptions
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.routing.bind
import org.http4k.routing.routes


class Router(
    val loginHandler: LoginHandler,
    val registerUserHandler: RegisterUserHandler
) {
    operator fun invoke() =
        ServerFilters.CatchAll()
            .then(CatchHttpExceptions())
            .then(ServerFilters.CatchLensFailure())
            .then(
                routes(
                    "/api/users/login" bind Method.POST to login(loginHandler),
                    "/api/users" bind Method.POST to registerUser(registerUserHandler)
                )
            )

    fun login(loginHandler: LoginHandler) = { req: Request ->
        val reqLens = Body.auto<LoginRequest>().toLens()

        val result = loginHandler(reqLens.extract(req).user)

        val resLens = Body.auto<LoginResponse>().toLens()

        resLens.inject(LoginResponse(result), Response(Status.OK))
    }

    fun registerUser(registerUserHandler: RegisterUserHandler) = { req: Request ->
        val reqLens = Body.auto<RegisterUserRequest>().toLens()

        val result = registerUserHandler(reqLens.extract(req).user)

        val resLens = Body.auto<RegisterUserResponse>().toLens()

        resLens.inject(RegisterUserResponse(result), Response(Status.OK))
    }
}

data class LoginRequest(val user: LoginInfo)
data class LoginResponse(val user: LoggedInUserInfo)

data class RegisterUserRequest(val user: NewUserInfo)
data class RegisterUserResponse(val user: RegisteredUserInfo)