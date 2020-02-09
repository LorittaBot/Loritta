package net.perfectdreams.loritta.platform.frontend.commands

import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.entities.Message
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.platform.frontend.entities.JSUser

class JSCommandContext(args: List<String>, message: Message) : CommandContext(args, message) {
	override suspend fun user(argument: Int): User? {
		val argAt = args.getOrNull(argument)

		if (argAt != null) {
			return when (argAt) {
				"Loritta" -> JSUser("Loritta")
				"MrPowerGamerBR" -> JSUser("MrPowerGamerBR")
				"Pantufa" -> JSUser("Pantufa")
				else -> null
			}
		}
		return null
	}
}