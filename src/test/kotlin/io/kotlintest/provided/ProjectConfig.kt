package io.kotlintest.provided

import conduit.IntegrationTest
import io.kotest.core.config.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override fun listeners() = listOf(IntegrationTest)
}
