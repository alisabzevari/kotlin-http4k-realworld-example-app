package conduit.config

import org.http4k.filter.CorsPolicy

data class AppConfig(
    val logConfig: String,
    val db: DbConfig,
    val corsPolicy: CorsPolicy,
    val port: Int
)

data class DbConfig(
    val url: String,
    val driver: String
)
