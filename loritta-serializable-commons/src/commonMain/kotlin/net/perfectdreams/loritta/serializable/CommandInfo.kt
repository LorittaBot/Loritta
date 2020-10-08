package net.perfectdreams.loritta.serializable

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.api.commands.CommandCategory

@Serializable
class CommandInfo(
		val name: String,
		val label: String,
		val aliases: List<String>,
		val category: CommandCategory,
		val description: String? = null,
		val usage: String? = null
)