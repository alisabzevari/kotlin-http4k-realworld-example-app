package conduit.config

import io.jsonwebtoken.SignatureAlgorithm
import javax.crypto.spec.SecretKeySpec

data class AppConfig(
    val logConfig: String,
    val db: DbConfig,
    val jwtConfig: JwtConfig,
    val port: Int
)

data class DbConfig(
    val url: String,
    val driver: String
)

data class JwtConfig(
    private val secret: String = System.getenv("JWT_SIGNING_KEY_SECRET"),
    private val algorithm: String = System.getenv("JWT_SIGNING_KEY_SECRET") ?: SignatureAlgorithm.HS256.jcaName,
    val signingKey: SecretKeySpec = SecretKeySpec(secret.toByteArray(), algorithm),
    val expirationMillis: Long = System.getenv("JWT_SIGNING_KEY_SECRET").toLong(),
    val issuer: String
)
