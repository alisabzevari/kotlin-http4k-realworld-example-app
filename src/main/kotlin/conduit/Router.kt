package conduit

import conduit.handler.*
import conduit.model.*
import conduit.util.CatchHttpExceptions
import conduit.util.TokenAuth
import conduit.util.createErrorResponse
import org.http4k.core.*
import org.http4k.filter.ServerFilters
import org.http4k.format.Jackson.auto
import org.http4k.lens.*
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes


class Router(
    val login: LoginHandler,
    val registerUser: RegisterUserHandler,
    val getCurrentUser: GetCurrentUserHandler,
    val updateCurrentUser: UpdateCurrentUserHandler,
    val getProfile: GetProfileHandler,
    val followUser: FollowUserHandler,
    val unfollowUser: UnfollowUserHandler,
    val createArticle: CreateArticleHandler,
    val getArticlesFeed: GetArticlesFeedHandler,
    val getTags: GetTagsHandler
) {
    private val contexts = RequestContexts()
    private val tokenInfoKey = RequestContextKey.required<TokenAuth.TokenInfo>(contexts)

    operator fun invoke(): RoutingHttpHandler =
        CatchHttpExceptions()
            .then(ServerFilters.CatchLensFailure {
                createErrorResponse(
                    Status.BAD_REQUEST,
                    if (it.cause != null) listOf(it.cause?.message!!) else it.failures.map { it.toString() } // TODO: improve error message creation logic
                )
            })
            .then(ServerFilters.InitialiseRequestContext(contexts))
            .then(
                routes(
                    "/healthcheck" bind Method.GET to { Response(Status.OK) },
                    "/api/users" bind routes(
                        "/login" bind Method.POST to login(),
                        "/" bind routes(
                            Method.POST to registerUser(),
                            Method.GET to TokenAuth(tokenInfoKey).then(getCurrentUser()),
                            Method.PUT to TokenAuth(tokenInfoKey).then(updateCurrentUser())
                        )
                    ),
                    "/api/user" bind routes( // postman tests calls this endpoint
                        Method.GET to TokenAuth(tokenInfoKey).then(getCurrentUser()),
                        Method.PUT to TokenAuth(tokenInfoKey).then(updateCurrentUser())
                    ),
                    "/api/profiles/{username}" bind routes(
                        "/" bind Method.GET to getProfile(),
                        "/follow" bind routes(
                            "/" bind Method.POST to TokenAuth(tokenInfoKey).then(followUser()),
                            "/" bind Method.DELETE to TokenAuth(tokenInfoKey).then(unfollowUser())
                        )
                    ),
                    "/api/articles" bind routes(
                        "/" bind Method.POST to TokenAuth(tokenInfoKey).then(createArticle()),
                        "/feed" bind Method.GET to TokenAuth(tokenInfoKey).then(getArticlesFeed())
                    ),
                    "/api/tags" bind Method.GET to getTagsHandler()
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
        val tokenInfo = tokenInfoLens(req)
        val result = getProfile(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }

    private fun followUser() = { req: Request ->
        val username = usernameLens(req)
        val tokenInfo = tokenInfoKey(req)
        val result = followUser(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }

    private fun unfollowUser() = { req: Request ->
        val username = usernameLens(req)
        val tokenInfo = tokenInfoKey(req)
        val result = unfollowUser(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }

    private val limitLens = Query.int().defaulted("limit", 20)
    private val offsetLens = Query.int().defaulted("offset", 0)
    private val multipleArticlesResponseLens = Body.auto<MultipleArticlesResponse>().toLens()

    private fun getArticlesFeed() = { req: Request ->
        val tokenInfo = tokenInfoKey(req)
        val offset = offsetLens(req)
        val limit = limitLens(req)
        val result = getArticlesFeed(tokenInfo, offset, limit)
        multipleArticlesResponseLens(
            MultipleArticlesResponse(result.articles, result.articlesCount),
            Response(Status.OK)
        )
    }

    private val tagsResponseLens = Body.auto<TagsResponse>().toLens()

    private fun getTagsHandler() = { _: Request ->
        val result = getTags()
        tagsResponseLens(
            TagsResponse(result),
            Response(Status.OK)
        )
    }

    private val newArticleRequestLens = Body.auto<NewArticleRequest>().toLens()
    private val singleArticleResponseLens = Body.auto<SingleArticleResponse>().toLens()

    private fun createArticle() = { req: Request ->
        val tokenInfo = tokenInfoKey(req)
        val newArticle = newArticleRequestLens(req).article
        singleArticleResponseLens(
            SingleArticleResponse(createArticle(newArticle, tokenInfo)),
            Response(Status.CREATED)
        )
    }
}

data class LoginUserRequest(val user: LoginUserDto)

data class UserResponse(val user: UserDto)

data class NewUserRequest(val user: NewUserDto)

data class UpdateUserRequest(val user: UpdateUser)

data class ProfileResponse(val profile: Profile)

data class MultipleArticlesResponse(val articles: List<Article>, val articlesCount: Int)

data class TagsResponse(val tags: List<ArticleTag>)

data class NewArticleRequest(val article: NewArticle)

data class SingleArticleResponse(val article: Article)