package conduit

import conduit.handler.*
import conduit.model.UpdateUser
import conduit.util.CatchHttpExceptions
import conduit.util.TokenAuth
import conduit.util.createErrorResponse
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.RequestContextKey
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes


class Router(
    val loginHandler: LoginHandler,
    val registerUserHandler: RegisterUserHandler,
    val getCurrentUserHandler: GetCurrentUserHandler,
    val updateCurrentUserHandler: UpdateCurrentUserHandler
) {
    val contexts = RequestContexts()
    val tokenInfoKey = RequestContextKey.required<TokenAuth.TokenInfo>(contexts)

    operator fun invoke(): RoutingHttpHandler =
        CatchHttpExceptions()
            .then(ServerFilters.CatchLensFailure {
                createErrorResponse(
                    Status.BAD_REQUEST,
                    it.failures.map { it.toString() })
            })
            .then(ServerFilters.InitialiseRequestContext(contexts))
            .then(
                routes(
                    "/api/users" bind routes(
                        "/login" bind Method.POST to login(),
                        "/" bind routes(
                            Method.POST to registerUser(),
                            Method.GET to TokenAuth(tokenInfoKey)(getCurrentUser()),
                            Method.PUT to TokenAuth(tokenInfoKey)(updateCurrentUser())
                        )
                    )
                )
            )

    fun login() = { req: Request ->
        val reqLens = Body.auto<LoginUserRequest>().toLens()

        val result = loginHandler(reqLens(req).user)

        val resLens = Body.auto<UserResponse>().toLens()
        resLens(UserResponse(result), Response(Status.OK))
    }

    fun registerUser() = { req: Request ->
        val reqLens = Body.auto<NewUserRequest>().toLens()

        val result = registerUserHandler(reqLens(req).user)

        val resLens = Body.auto<UserResponse>().toLens()

        resLens(UserResponse(result), Response(Status.CREATED))
    }

    fun getCurrentUser() = { req: Request ->

        val tokenInfo = tokenInfoKey(req)

        val result = getCurrentUserHandler(tokenInfo)

        val resLens = Body.auto<UserResponse>().toLens()
        resLens(UserResponse(result), Response(Status.OK))
    }

    fun updateCurrentUser() = { req: Request ->
        val reqLens = Body.auto<UpdateUserRequest>().toLens()

        val tokenInfo = tokenInfoKey(req)
        val updateUser = reqLens(req).user

        val result = updateCurrentUserHandler(tokenInfo, updateUser)

        val resLens = Body.auto<UserResponse>().toLens()
        resLens(UserResponse(result), Response(Status.OK))
    }
}

data class LoginUserRequest(val user: LoginUserDto)
data class UserResponse(val user: UserDto)

data class NewUserRequest(val user: NewUserDto)

data class UpdateUserRequest(val user: UpdateUser)