package cash.atto.commons

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AttoNetworkConfigurationTest {
    @Test
    fun `should resolve cash network by code`() {
        assertEquals(AttoNetwork.CASH, AttoNetwork.from(AttoNetwork.CASH.code))
    }

    @Test
    fun `should expose default endpoints for cash`() {
        val configuration = AttoNetworkConfigurations.configuration(AttoNetwork.CASH)

        assertEquals("https://wallet-gatekeeper.cash.application.atto.cash", configuration.endpoints.walletGatekeeperUrl)
        assertEquals("https://gatekeeper.cash.application.atto.cash", configuration.endpoints.gatekeeperUrl)
        assertEquals(configuration.endpoints.gatekeeperUrl, configuration.endpoints.nodeUrl)
        assertEquals(configuration.endpoints.gatekeeperUrl, configuration.endpoints.workerUrl)
        assertEquals(AttoUnit.CASH, configuration.displayUnit)
        assertTrue(configuration.defaultRepresentatives.isNotEmpty())
    }

    @Test
    fun `should override endpoints`() {
        val network = AttoNetwork.CASH
        val customConfig =
            AttoNetworkConfiguration(
                network = network,
                endpoints =
                    AttoNetworkEndpoints(
                        gatekeeperUrl = "https://gatekeeper.custom.cash.atto.dev",
                        walletGatekeeperUrl = "https://wallet-gatekeeper.custom.cash.atto.dev",
                        nodeUrl = "https://node.custom.cash.atto.dev",
                        workerUrl = "https://worker.custom.cash.atto.dev",
                    ),
                defaultRepresentatives = emptyList(),
                displayUnit = AttoUnit.CASH,
            )

        try {
            AttoNetworkConfigurations.override(customConfig)
            val resolved = AttoNetworkConfigurations.configuration(network)

            assertEquals(customConfig.endpoints.nodeUrl, resolved.endpoints.nodeUrl)
            assertEquals(customConfig.endpoints.walletGatekeeperUrl, resolved.endpoints.walletGatekeeperUrl)
            assertEquals(0, resolved.defaultRepresentatives.size)
        } finally {
            AttoNetworkConfigurations.reset(network)
        }
    }
}
