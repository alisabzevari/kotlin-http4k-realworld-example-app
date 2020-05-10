package conduit

import conduit.handler.*
import conduit.model.*
import conduit.util.*
import conduit.util.ConduitJackson.auto
import org.http4k.core.*
import org.http4k.filter.CorsPolicy
import org.http4k.filter.ServerFilters
import org.http4k.lens.*
import org.http4k.routing.RoutingHttpHandler
import org.http4k.routing.bind
import org.http4k.routing.routes

class Router(
    val corsPolicy: CorsPolicy,
    val jwt: JWT,
    val login: LoginHandler,
    val registerUser: RegisterUserHandler,
    val getCurrentUser: GetCurrentUserHandler,
    val updateCurrentUser: UpdateCurrentUserHandler,
    val getProfile: GetProfileHandler,
    val followUser: FollowUserHandler,
    val unfollowUser: UnfollowUserHandler,
    val createArticle: CreateArticleHandler,
    val getArticles: GetArticlesHandler,
    val createArticleComment: CreateArticleCommentHandler,
    val getArticleComments: GetArticleCommentsHandler,
    val deleteArticleComment: DeleteArticleCommentHandler,
    val createArticleFavorite: CreateArticleFavoriteHandler,
    val deleteArticleFavorite: DeleteArticleFavoriteHandler,
    val deleteArticle: DeleteArticleHandler,
    val getArticlesFeed: GetArticlesFeedHandler,
    val getArticle: GetArticleHandler,
    val updateArticle: UpdateArticleHandler,
    val getTags: GetTagsHandler
) {
    private val contexts = RequestContexts()
    private val tokenAuth = TokenAuth(jwt, contexts)

    operator fun invoke(): RoutingHttpHandler =
        CatchHttpExceptions()
            .then(ServerFilters.Cors(corsPolicy))
            .then(ServerFilters.CatchLensFailure { error ->
                createErrorResponse(
                    Status.BAD_REQUEST,
                    if (error.cause != null) listOf(error.cause?.message!!) else error.failures.map { it.toString() } // TODO: improve error message creation logic
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
                            Method.GET to tokenAuth.required().then(getCurrentUser()),
                            Method.PUT to tokenAuth.required().then(updateCurrentUser())
                        )
                    ),
                    "/api/user" bind routes( // postman tests calls this endpoint
                        Method.GET to tokenAuth.required().then(getCurrentUser()),
                        Method.PUT to tokenAuth.required().then(updateCurrentUser())
                    ),
                    "/api/profiles/{username}" bind routes(
                        "/" bind Method.GET to tokenAuth.optional().then(getProfile()),
                        "/follow" bind routes(
                            "/" bind Method.POST to tokenAuth.required().then(followUser()),
                            "/" bind Method.DELETE to tokenAuth.required().then(unfollowUser())
                        )
                    ),
                    "/api/articles" bind routes(
                        "/" bind Method.POST to tokenAuth.required().then(createArticle()),
                        "/" bind Method.GET to tokenAuth.optional().then(getArticles()),
                        "/feed" bind Method.GET to tokenAuth.required().then(getArticlesFeed()),
                        "{slug}" bind routes(
                            "/" bind Method.DELETE to tokenAuth.required().then(deleteArticle()),
                            "/" bind Method.GET to tokenAuth.optional().then(getArticle()),
                            "/" bind Method.PUT to tokenAuth.required().then(updateArticle()),
                            "/comments" bind Method.POST to tokenAuth.required().then(createArticleComment()),
                            "/comments" bind Method.GET to tokenAuth.optional().then(getArticleComments()),
                            "/comments/{commentId}" bind Method.DELETE to tokenAuth.required().then(
                                deleteArticleComment()
                            ),
                            "/favorite" bind Method.POST to tokenAuth.required().then(createArticleFavorite()),
                            "/favorite" bind Method.DELETE to tokenAuth.required().then(deleteArticleFavorite())
                        )
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
        val tokenInfo = tokenAuth.getToken(req)
        val result = getCurrentUser(tokenInfo)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private val updateLens = Body.auto<UpdateUserRequest>().toLens()

    private fun updateCurrentUser() = { req: Request ->
        val tokenInfo = tokenAuth.getToken(req)
        val updateUser = updateLens(req).user
        val result = updateCurrentUser(tokenInfo, updateUser)
        userLens(UserResponse(result), Response(Status.OK))
    }

    private val usernameLens = Path.nonEmptyString().map(::Username).of("username")
    private val profileLens = Body.auto<ProfileResponse>().toLens()

    private fun getProfile() = { req: Request ->
        val username = usernameLens(req)
        val tokenInfo = tokenAuth.getOptionalToken(req)
        val result = getProfile(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }

    private fun followUser() = { req: Request ->
        val username = usernameLens(req)
        val tokenInfo = tokenAuth.getToken(req)
        val result = followUser(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }

    private fun unfollowUser() = { req: Request ->
        val username = usernameLens(req)
        val tokenInfo = tokenAuth.getToken(req)
        val result = unfollowUser(username, tokenInfo)
        profileLens(ProfileResponse(result), Response(Status.OK))
    }

    private val limitLens = Query.int().defaulted("limit", 20)
    private val offsetLens = Query.int().defaulted("offset", 0)
    private val multipleArticlesResponseLens = Body.auto<MultipleArticlesResponse>().toLens()

    private fun getArticlesFeed() = { req: Request ->
        val tokenInfo = tokenAuth.getToken(req)
        val offset = offsetLens(req)
        val limit = limitLens(req)
        val result = getArticlesFeed(tokenInfo, offset, limit)
        multipleArticlesResponseLens(
            MultipleArticlesResponse(result.articles, result.articlesCount),
            Response(Status.OK)
        )
    }

    private val optionalTagReqLens = Query.string().optional("tag")
    private val optionalAuthorReqLens = Query.string().optional("author")
    private val optionalFavoritedReqLens = Query.string().optional("favorited")

    private fun getArticles() = { req: Request ->
        val tokenInfo = tokenAuth.getOptionalToken(req)
        val offset = offsetLens(req)
        val limit = limitLens(req)
        val tag = optionalTagReqLens(req)?.let(::ArticleTag)
        val author = optionalAuthorReqLens(req)?.let(::Username)
        val favorited = optionalFavoritedReqLens(req)?.let(::Username)

        val result = getArticles(tokenInfo, offset, limit, tag, author, favorited)
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
        val tokenInfo = tokenAuth.getToken(req)
        val newArticle = newArticleRequestLens(req).article
        singleArticleResponseLens(
            SingleArticleResponse(createArticle(newArticle, tokenInfo)),
            Response(Status.CREATED)
        )
    }

    private val newCommentRequestLens = Body.auto<NewCommentRequest>().toLens()
    private val singleCommentResponseLens = Body.auto<SingleCommentResponse>().toLens()
    private val articleSlugLens = Path.nonEmptyString().map(::ArticleSlug).of("slug")

    private fun createArticleComment() = { req: Request ->
        val tokenInfo = tokenAuth.getToken(req)
        val newComment = newCommentRequestLens(req)
        val slug = articleSlugLens(req)
        singleCommentResponseLens(
            SingleCommentResponse(createArticleComment(newComment.comment, slug, tokenInfo)),
            Response(Status.OK)
        )
    }

    private val multipleCommentsResponseLens = Body.auto<MultipleCommentsResponse>().toLens()

    private fun getArticleComments() = { req: Request ->
        val tokenInfo = tokenAuth.getOptionalToken(req)
        val slug = articleSlugLens(req)
        multipleCommentsResponseLens(
            MultipleCommentsResponse(getArticleComments(slug, tokenInfo)),
            Response(Status.OK)
        )
    }

    private val articleCommentIdLens = Path.nonEmptyString().map(String::toInt).of("commentId")
    private fun deleteArticleComment() = { req: Request ->
        val commentId = articleCommentIdLens(req)
        deleteArticleComment(commentId)
        Response(Status.OK)
    }

    private fun createArticleFavorite() = { req: Request ->
        val tokenInfo = tokenAuth.getToken(req)
        val slug = articleSlugLens(req)
        singleArticleResponseLens(
            SingleArticleResponse(createArticleFavorite(slug, tokenInfo)),
            Response(Status.OK)
        )
    }

    private fun deleteArticleFavorite() = { req: Request ->
        val tokenInfo = tokenAuth.getToken(req)
        val slug = articleSlugLens(req)
        singleArticleResponseLens(
            SingleArticleResponse(deleteArticleFavorite(slug, tokenInfo)),
            Response(Status.OK)
        )
    }

    private fun deleteArticle() = { req: Request ->
        val tokenInfo = tokenAuth.getToken(req)
        val slug = articleSlugLens(req)
        deleteArticle(slug, tokenInfo)
        Response(Status.OK)
    }

    private fun getArticle() = { req: Request ->
        val slug = articleSlugLens(req)
        val tokenInfo = tokenAuth.getOptionalToken(req)
        singleArticleResponseLens(
            SingleArticleResponse(getArticle(slug, tokenInfo)),
            Response(Status.OK)
        )
    }

    private val updateArticleRequestLens = Body.auto<UpdateArticleRequest>().toLens()

    private fun updateArticle() = { req: Request ->
        val updateArticleDto = updateArticleRequestLens(req).article
        val tokenInfo = tokenAuth.getToken(req)
        val articleSlug = articleSlugLens(req)

        singleArticleResponseLens(
            SingleArticleResponse(updateArticle(articleSlug, updateArticleDto, tokenInfo)),
            Response(Status.OK)
        )
    }
}

data class LoginUserRequest(val user: LoginUserDto)

data class UserResponse(val user: UserDto)

data class NewUserRequest(val user: NewUserDto)

data class UpdateUserRequest(val user: UpdateUser)

data class ProfileResponse(val profile: Profile)

data class MultipleArticlesResponse(val articles: List<ArticleDto>, val articlesCount: Int)

data class TagsResponse(val tags: List<ArticleTag>)

data class NewArticleRequest(val article: NewArticleDto)

data class SingleArticleResponse(val article: ArticleDto)

data class NewCommentRequest(val comment: NewComment)

data class SingleCommentResponse(val comment: CommentDto)

data class MultipleCommentsResponse(val comments: List<CommentDto>)

data class UpdateArticleRequest(val article: UpdateArticle)
