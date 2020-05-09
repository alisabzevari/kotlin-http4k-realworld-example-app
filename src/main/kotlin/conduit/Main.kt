package conduit

import conduit.config.AppConfig
import conduit.handler.*
import conduit.repository.ConduitRepositoryImpl
import conduit.repository.ConduitTransactionManagerImpl
import conduit.repository.createDb
import conduit.util.JWT
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
    val txManager = ConduitTransactionManagerImpl(db, repository)
    val jwt = with(config.jwtConfig) { JWT(secret, algorithm, issuer, expirationMillis) }

    val registerUserHandler = RegisterUserHandlerImpl(txManager, jwt)
    val loginHandler = LoginHandlerImpl(txManager, jwt)
    val getCurrentUserHandler = GetCurrentUserHandlerImpl(txManager)
    val updateCurrentUserHandler = UpdateCurrentUserHandlerImpl(txManager)
    val getProfileHandler = GetProfileHandlerImpl(txManager)
    val followUserHandler = FollowUserHandlerImpl(txManager)
    val unfollowUserHandler = UnfollowUserHandlerImpl(txManager)
    val getArticlesFeed = GetArticlesFeedHandlerImpl(txManager)
    val createArticle = CreateArticleHandlerImpl(txManager)
    val createArticleComment = CreateArticleCommentHandlerImpl(txManager)
    val getArticles = GetArticlesHandlerImpl(txManager)
    val getArticleComments = GetArticleCommentsHandlerImpl(txManager)
    val deleteArticleComment = DeleteArticleCommentHandlerImpl(txManager)
    val createArticleFavorite = CreateArticleFavoriteHandlerImpl(txManager)
    val deleteArticleFavorite = DeleteArticleFavoriteHandlerImpl(txManager)
    val deleteArticle = DeleteArticleHandlerImpl(txManager)
    val getArticle = GetArticleHandlerImpl(txManager)
    val updateArticle = UpdateArticleHandlerImpl(txManager)
    val getTags = GetTagsHandlerImpl(txManager)

    val app = Router(
        config.corsPolicy,
        jwt,
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
