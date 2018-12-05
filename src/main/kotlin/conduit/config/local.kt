package conduit.config

val local = AppConfig(
    logConfig = "log4j2-local.yaml",
    db = DbConfig(
        url = "jdbc:h2:~/conduit-db/conduit",
        driver = "org.h2.Driver"
    ),
    port = 9000
)
