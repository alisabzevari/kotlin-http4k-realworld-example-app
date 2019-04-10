package conduit.repository

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.transaction

interface TransactionManager<Repository> {
    fun <T> tx(block: Repository.() -> T): T
}

typealias ConduitTxManager = TransactionManager<ConduitRepository>

class ConduitTransactionManagerImpl(
    private val database: Database,
    private val repository: ConduitRepository
) : ConduitTxManager {
    override fun <T> tx(block: ConduitRepository.() -> T) =
        transaction(database) {
            block(repository)
        }
}
