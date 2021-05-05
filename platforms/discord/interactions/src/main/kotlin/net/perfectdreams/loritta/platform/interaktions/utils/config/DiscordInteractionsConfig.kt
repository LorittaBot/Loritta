package net.perfectdreams.loritta.platform.interaktions.utils.config

import kotlinx.serialization.Serializable

@Serializable
class DiscordInteractionsConfig(
    val publicKey: String,
    val registerGlobally: Boolean,
    val guildsToBeRegistered: List<Long>
)