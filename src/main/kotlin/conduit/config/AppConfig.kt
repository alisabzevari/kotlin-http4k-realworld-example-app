package conduit.config

data class AppConfig(
    val logConfig: String,
    val db: DbConfig,
    val port: Int
)

data class DbConfig(
    val url: String,
    val driver: String
)
