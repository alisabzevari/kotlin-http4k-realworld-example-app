package conduit

import conduit.handler.*
import conduit.model.Profile
import conduit.model.UpdateUser
import conduit.model.Username
import conduit.util.CatchHttpExceptions
import conduit.util.TokenAuth
import conduit.util.createErrorResponse
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.Header
import org.http4k.lens.Path
import org.http4k.lens.RequestContextKey
import org.http4k.lens.nonEmptyString
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes


class Router(
    val login: LoginHandler,
    val registerUser: RegisterUserHandler,
    val getCurrentUser: GetCurrentUserHandler,
    val updateCurrentUser: UpdateCurrentUserHandler,
    val getProfile: GetProfileHandler
) {
    private val contexts = RequestContexts()
    private val tokenInfoKey = RequestContextKey.required<TokenAuth.TokenInfo>(contexts)

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
                            Method.GET to TokenAuth(tokenInfoKey).then(getCurrentUser()),
                            Method.PUT to TokenAuth(tokenInfoKey).then(updateCurrentUser())
                        )
                    ),
                    "/api/profiles/{username}" bind routes(
                        "/" bind Method.GET to getProfile()
                    )
                )
            )

    private val loginLens = Body.auto<LoginUserRequest>().toLens()
    private val userLens = Body.auto<UserResponse>().toLens()

    private fun login() = { req: Request ->
        val result = login(loginLens(req).user)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private val registerLens = Body.auto<NewUserRequest>().toLens()

    private fun registerUser() = { req: Request ->
        val result = registerUser(registerLens(req).user)
        userLens(UserResponse(result), Response(Status.CREATED))
    }

    private fun getCurrentUser() = { req: Request ->
        val tokenInfo = tokenInfoKey(req)
        val result = getCurrentUser(tokenInfo)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private val updateLens = Body.auto<UpdateUserRequest>().toLens()

    private fun updateCurrentUser() = { req: Request ->
        val tokenInfo = tokenInfoKey(req)
        val updateUser = updateLens(req).user
        val result = updateCurrentUser(tokenInfo, updateUser)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private val usernameLens = Path.nonEmptyString().map(::Username).of("username")
    private val profileLens = Body.auto<ProfileResponse>().toLens()
    private val tokenInfoLens = Header.map(TokenAuth::extract).optional("Authorization")

    private fun getProfile() = { req: Request ->
        val username = usernameLens(req)
        val tokenInfo = tokenInfoLens.extract(req)
        val result = getProfile(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }
}

data class LoginUserRequest(val user: LoginUserDto)

data class UserResponse(val user: UserDto)

data class NewUserRequest(val user: NewUserDto)

data class UpdateUserRequest(val user: UpdateUser)

data class ProfileResponse(val profile: Profile)