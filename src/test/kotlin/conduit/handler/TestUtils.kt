package conduit.handler

import conduit.repository.ConduitRepository
import conduit.repository.ConduitTxManager

class TestTxManager(private val repository: ConduitRepository) : ConduitTxManager {
    override fun <T> tx(block: ConduitRepository.() -> T): T = block(repository)
}