package conduit.config

import org.http4k.core.Method
import org.http4k.filter.CorsPolicy

val local = AppConfig(
    logConfig = "log4j2-local.yaml",
    db = DbConfig(
        url = "jdbc:h2:~/conduit-db/conduit",
        driver = "org.h2.Driver"
    ),
    port = 9000,
    corsPolicy = CorsPolicy(
        origins = listOf("localhost:9000"),
        headers = listOf("content-type", "authorization"),
        methods = Method.values().toList(),
        credentials = true
    )
)
