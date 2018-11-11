package conduit

import conduit.config.AppConfig
import conduit.config.DbConfig
import io.kotlintest.extensions.TestListener
import org.http4k.server.Http4kServer
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory
import java.io.Closeable

private val logger = LoggerFactory.getLogger("IntegrationTest")

class App : Closeable {
    val db: Database
    val server: Http4kServer
    val config: AppConfig = AppConfig(
        "log4j2.yaml",
        DbConfig(
            "jdbc:h2:mem:conduit-test-db",
            "org.h2.Driver"
        ),
        9192
    )

    init {
        server = startApp(config)
        db = Database.connect(config.db.url, driver = config.db.driver)
    }

    fun resetDb() {
        // ...
    }

    override fun close() {
        logger.info("Closing App...")
        db.connector().close()
        server.stop()
    }
}

object IntegrationTest : TestListener {
    private val lazyApp = lazy { App() }
    val app: App by lazyApp

    override fun afterProject() {
        if (lazyApp.isInitialized()) {
            app.close()
        }
    }
}