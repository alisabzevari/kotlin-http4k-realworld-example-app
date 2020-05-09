package conduit.config

import org.http4k.filter.CorsPolicy

data class AppConfig(
    val logConfig: String,
    val db: DbConfig,
    val corsPolicy: CorsPolicy,
    val jwtConfig: JwtConfig,
    val port: Int
)

data class DbConfig(
    val url: String,
    val driver: String
)

data class JwtConfig(
    val secret: String = System.getenv("JWT_SECRET"),
    val algorithm: String? = System.getenv("JWT_ALGORITHM"),
    val expirationMillis: Long = System.getenv("JWT_EXPIRATION_MILLIS").toLong(),
    val issuer: String
)
