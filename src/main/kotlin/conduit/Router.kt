package conduit

import conduit.handler.*
import conduit.util.CatchHttpExceptions
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
        CatchHttpExceptions()
            .then(ServerFilters.CatchLensFailure())
            .then(
                routes(
                    "/api/users/login" bind Method.POST to login(loginHandler),
                    "/api/users" bind Method.POST to registerUser(registerUserHandler)
                )
            )

    fun login(loginHandler: LoginHandler) = { req: Request ->
        val reqLens = Body.auto<LoginUserRequest>().toLens()

        val result = loginHandler(reqLens.extract(req).user)

        val resLens = Body.auto<UserResponse>().toLens()

        resLens.inject(UserResponse(result), Response(Status.OK))
    }

    fun registerUser(registerUserHandler: RegisterUserHandler) = { req: Request ->
        val reqLens = Body.auto<NewUserRequest>().toLens()

        val result = registerUserHandler(reqLens.extract(req).user)

        val resLens = Body.auto<UserResponse>().toLens()

        resLens.inject(UserResponse(result), Response(Status.OK))
    }
}

data class LoginUserRequest(val user: LoginUserDto)
data class UserResponse(val user: UserDto)

data class NewUserRequest(val user: NewUserDto)
