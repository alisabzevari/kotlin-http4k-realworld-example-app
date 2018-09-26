package conduit

import conduit.handlers.LoginHandlerImpl
import conduit.repository.ConduitRepository
import mu.KotlinLogging
import org.apache.logging.log4j.core.config.Configurator
import org.http4k.server.Jetty
import org.http4k.server.asServer
import org.jetbrains.exposed.sql.Database

// TODO: Create filter for authentication
// TODO: Add error handling filter

// TODO: Fix content type of request
// TODO: Configure json serializer correctly
// TODO: Define a better place for request lenses

fun main(args: Array<String>) {
//    Configurator.initialize(null, "log4j2-local.yaml")
//
//    val logger = KotlinLogging.logger("main")
//
//    val database = Database.connect("jdbc:h2:~/conduit", driver = "org.h2.Driver")
//    val repository = ConduitRepository(database)
//
//    val loginHandler = LoginHandlerImpl(repository)
//
//    val app = Router(
//        loginHandler
//    )()
//
//    logger.info("Starting server...")
//    app.asServer(Jetty(9000)).start()
//    logger.info("Server started on port 9000")
}