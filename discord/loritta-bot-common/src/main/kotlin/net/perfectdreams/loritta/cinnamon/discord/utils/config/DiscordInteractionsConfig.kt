package net.perfectdreams.loritta.cinnamon.discord.utils.config

import kotlinx.serialization.Serializable

@Serializable
class DiscordInteractionsConfig(
    val registerGlobally: Boolean,
    val guildsToBeRegistered: List<Long>
)