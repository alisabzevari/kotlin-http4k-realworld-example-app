package conduit.config

val local = AppConfig(
    logConfig = "log4j2-local.yaml",
    db = DbConfig(
        url = "jdbc:h2:~/conduit-db/conduit",
        driver = "org.h2.Driver"
    ),
    port = 9000,
    jwtConfig = JwtConfig(
        secret = "Top Secret",
        issuer = "thinkster.io",
        expirationMillis = 36_000_000
    )
)
