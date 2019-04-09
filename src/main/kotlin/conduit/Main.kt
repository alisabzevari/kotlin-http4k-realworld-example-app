package conduit

import conduit.config.AppConfig
import conduit.handler.*
import conduit.repository.*
import org.apache.logging.log4j.core.config.Configurator
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.slf4j.LoggerFactory

fun main() {
    val server = startApp(conduit.config.local)
    server.block()
}

fun startApp(config: AppConfig): Http4kServer {
    Configurator.initialize(null, config.logConfig)

    val logger = LoggerFactory.getLogger("main")

    val db = createDb(config.db.url, driver = config.db.driver)
    val repository = ConduitRepositoryImpl()
    val database = ConduitDatabaseImpl(db, repository)

    val registerUserHandler = RegisterUserHandlerImpl(database)
    val loginHandler = LoginHandlerImpl(database)
    val getCurrentUserHandler = GetCurrentUserHandlerImpl(database)
    val updateCurrentUserHandler = UpdateCurrentUserHandlerImpl(database)
    val getProfileHandler = GetProfileHandlerImpl(database)
    val followUserHandler = FollowUserHandlerImpl(database)
    val unfollowUserHandler = UnfollowUserHandlerImpl(database)
    val getArticlesFeed = GetArticlesFeedHandlerImpl(database)
    val createArticle = CreateArticleHandlerImpl(database)
    val createArticleComment = CreateArticleCommentHandlerImpl(database)
    val getArticles = GetArticlesHandlerImpl(database)
    val getArticleComments = GetArticleCommentsHandlerImpl(database)
    val deleteArticleComment = DeleteArticleCommentHandlerImpl(database)
    val createArticleFavorite = CreateArticleFavoriteHandlerImpl(database)
    val deleteArticleFavorite = DeleteArticleFavoriteHandlerImpl(database)
    val deleteArticle = DeleteArticleHandlerImpl(database)
    val getArticle = GetArticleHandlerImpl(database)
    val updateArticle = UpdateArticleHandlerImpl(database)
    val getTags = GetTagsHandlerImpl(database)

    val app = Router(
        loginHandler,
        registerUserHandler,
        getCurrentUserHandler,
        updateCurrentUserHandler,
        getProfileHandler,
        followUserHandler,
        unfollowUserHandler,
        createArticle,
        getArticles,
        createArticleComment,
        getArticleComments,
        deleteArticleComment,
        createArticleFavorite,
        deleteArticleFavorite,
        deleteArticle,
        getArticlesFeed,
        getArticle,
        updateArticle,
        getTags
    )()

    logger.info("Starting server...")
    val server = app.asServer(Jetty(config.port)).start()
    logger.info("Server started on port ${config.port}")
    return server
}