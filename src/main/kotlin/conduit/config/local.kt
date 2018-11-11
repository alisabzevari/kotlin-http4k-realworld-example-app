package conduit.config

val local = AppConfig(
    logConfig = "log4j2-local.yaml",
    db = DbConfig(
        "jdbc:h2:~/conduit-db/conduit",
        "org.h2.Driver"
    ),
    port = 9000
)
