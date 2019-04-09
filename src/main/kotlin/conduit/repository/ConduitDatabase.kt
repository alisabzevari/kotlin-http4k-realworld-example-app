package conduit.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

interface ConduitDatabase {
    fun <T> tx(block: ConduitRepository.() -> T): T
}

class ConduitDatabaseImpl(
    private val database: Database,
    private val repository: ConduitRepository
) : ConduitDatabase {
    override fun <T> tx(block: ConduitRepository.() -> T) =
        transaction(database) {
            block(repository)
        }
}
