package cash.atto.commons

import cash.atto.commons.utils.JsExportForJs

@JsExportForJs
data class AttoNetworkEndpoints(
    val gatekeeperUrl: String,
    val walletGatekeeperUrl: String,
    val nodeUrl: String = gatekeeperUrl,
    val workerUrl: String = gatekeeperUrl,
)

@JsExportForJs
data class AttoNetworkConfiguration(
    val network: AttoNetwork,
    val endpoints: AttoNetworkEndpoints,
    val defaultRepresentatives: List<AttoAddress> = emptyList(),
    val displayUnit: AttoUnit = AttoUnit.ATTO,
) {
    fun primaryRepresentative(): AttoAddress? = defaultRepresentatives.firstOrNull()
}

@JsExportForJs
object AttoNetworkConfigurations {
    private val overrides = mutableMapOf<AttoNetwork, AttoNetworkConfiguration>()

    private val cashRepresentatives =
        listOf(
            createCashRepresentative(0xA1),
            createCashRepresentative(0xB2),
        )

    private val defaults: Map<AttoNetwork, AttoNetworkConfiguration> =
        listOf(
            createDefaultConfiguration(AttoNetwork.LIVE),
            createDefaultConfiguration(AttoNetwork.CASH, AttoUnit.CASH, cashRepresentatives),
            createDefaultConfiguration(AttoNetwork.BETA),
            createDefaultConfiguration(AttoNetwork.DEV),
            createDefaultConfiguration(AttoNetwork.LOCAL),
        ).associateBy { it.network }

    private fun createDefaultConfiguration(
        network: AttoNetwork,
        displayUnit: AttoUnit = AttoUnit.ATTO,
        representatives: List<AttoAddress> = emptyList(),
    ): AttoNetworkConfiguration {
        val subdomain = network.name.lowercase()
        val gatekeeperUrl = backendUrl("gatekeeper", subdomain)
        val walletGatekeeperUrl = backendUrl("wallet-gatekeeper", subdomain)
        return AttoNetworkConfiguration(
            network = network,
            endpoints =
                AttoNetworkEndpoints(
                    gatekeeperUrl = gatekeeperUrl,
                    walletGatekeeperUrl = walletGatekeeperUrl,
                    nodeUrl = gatekeeperUrl,
                    workerUrl = gatekeeperUrl,
                ),
            defaultRepresentatives = representatives,
            displayUnit = displayUnit,
        )
    }

    private fun backendUrl(
        service: String,
        subdomain: String,
    ): String = "https://$service.$subdomain.application.atto.cash"

    private fun createCashRepresentative(seed: Int): AttoAddress =
        AttoAddress(
            AttoAlgorithm.V1,
            AttoPublicKey(ByteArray(32) { seed.toByte() }),
        )

    fun configuration(network: AttoNetwork): AttoNetworkConfiguration =
        overrides[network] ?: defaults[network] ?: error("No configuration available for $network")

    fun override(configuration: AttoNetworkConfiguration) {
        overrides[configuration.network] = configuration
    }

    fun override(
        network: AttoNetwork,
        endpoints: AttoNetworkEndpoints,
        defaultRepresentatives: List<AttoAddress> = configuration(network).defaultRepresentatives,
        displayUnit: AttoUnit = configuration(network).displayUnit,
    ) {
        override(AttoNetworkConfiguration(network, endpoints, defaultRepresentatives, displayUnit))
    }

    fun reset(network: AttoNetwork) {
        overrides.remove(network)
    }
}
