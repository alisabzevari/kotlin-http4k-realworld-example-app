package io.kotlintest.provided

import conduit.IntegrationTest
import io.kotlintest.AbstractProjectConfig

object ProjectConfig : AbstractProjectConfig() {
    override fun listeners() = listOf(IntegrationTest)
}