package net.perfectdreams.loritta.website.backend.utils.config

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.config.LorittaConfig

@Serializable
data class RootConfig(
    val sessionHex: String,
    val sessionName: String,
    val sessionDomain: String,
    val loritta: LorittaConfig,
    val discord: LorittaDiscordConfig,
    val etherealGambi: EtherealGambiConfig,
    val pudding: PuddingConfig
) {
    @Serializable
    data class PuddingConfig(
        val database: String,
        val address: String,
        val username: String,
        val password: String
    )
}