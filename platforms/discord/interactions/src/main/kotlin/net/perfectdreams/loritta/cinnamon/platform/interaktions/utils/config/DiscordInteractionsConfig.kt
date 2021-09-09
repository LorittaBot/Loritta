package net.perfectdreams.loritta.cinnamon.platform.interaktions.utils.config

import kotlinx.serialization.Serializable

@Serializable
class DiscordInteractionsConfig(
    val publicKey: String,
    val registerGlobally: Boolean,
    val guildsToBeRegistered: List<Long>
)