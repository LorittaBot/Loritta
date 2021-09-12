package net.perfectdreams.loritta.cinnamon.platform.utils.config

import kotlinx.serialization.Serializable

@Serializable
class DiscordInteractionsConfig(
    val registerGlobally: Boolean,
    val guildsToBeRegistered: List<Long>
)