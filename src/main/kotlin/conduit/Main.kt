package conduit

import conduit.config.AppConfig
import conduit.handler.*
import conduit.repository.ConduitRepositoryImpl
import conduit.repository.createDb
import org.apache.logging.log4j.core.config.Configurator
import org.http4k.server.Http4kServer
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.slf4j.LoggerFactory

fun main(args: Array<String>) {
    val server = startApp(conduit.config.local)
    server.block()
}

fun startApp(config: AppConfig): Http4kServer {
    Configurator.initialize(null, config.logConfig)

    val logger = LoggerFactory.getLogger("main")

    val database = createDb(config.db.url, driver = config.db.driver)
    val repository = ConduitRepositoryImpl(database)

    val loginHandler = LoginHandlerImpl(repository)
    val registerUserHandler = RegisterUserHandlerImpl(repository)
    val getCurrentUserHandler = GetCurrentUserHandlerImpl(repository)
    val updateCurrentUserHandler = UpdateCurrentUserHandlerImpl(repository)
    val getProfileHandler = GetProfileHandlerImpl(repository)
    val followUserHandler = FollowUserHandlerImpl(repository)
    val unfollowUserHandler = UnfollowUserHandlerImpl(repository)
    val getArticlesFeed = GetArticlesFeedHandlerImpl(repository)
    val createArticle = CreateArticleHandlerImpl(repository)
    val getTags = GetTagsHandlerImpl(repository)

    val app = Router(
        loginHandler,
        registerUserHandler,
        getCurrentUserHandler,
        updateCurrentUserHandler,
        getProfileHandler,
        followUserHandler,
        unfollowUserHandler,
        createArticle,
        getArticlesFeed,
        getTags
    )()

    logger.info("Starting server...")
    val server = app.asServer(Jetty(config.port)).start()
    logger.info("Server started on port ${config.port}")
    return server
}