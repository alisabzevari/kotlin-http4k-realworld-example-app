package conduit.config

import org.http4k.core.Method
import org.http4k.filter.CorsPolicy
import org.http4k.filter.Only
import org.http4k.filter.OriginPolicy

val local = AppConfig(
    logConfig = "log4j2-local.yaml",
    db = DbConfig(
        url = "jdbc:h2:~/conduit-db/conduit",
        driver = "org.h2.Driver"
    ),
    corsPolicy = CorsPolicy(
        originPolicy = OriginPolicy.Only("localhost:9000"),
        headers = listOf("content-type", "authorization"),
        methods = Method.values().toList(),
        credentials = true
    ),
    jwtConfig = JwtConfig(
        secret = "Top Secret",
        issuer = "thinkster.io",
        expirationMillis = 36_000_000
    ),
    port = 9000
)
