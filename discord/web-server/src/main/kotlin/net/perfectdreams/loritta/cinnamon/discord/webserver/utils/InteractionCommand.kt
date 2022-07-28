package net.perfectdreams.loritta.cinnamon.discord.webserver.utils

import kotlinx.serialization.Serializable
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandCategory

@Serializable
data class InteractionCommand(
    val labels: List<String>,
    val description: StringI18nData,
    val category: CommandCategory,
    val executor: String?,
    val groups: List<InteractionCommandGroup>,
    val subcommands: List<InteractionCommand>
)

@Serializable
data class InteractionCommandGroup(
    val labels: List<String>,
    val subcommands: List<InteractionCommand>
)