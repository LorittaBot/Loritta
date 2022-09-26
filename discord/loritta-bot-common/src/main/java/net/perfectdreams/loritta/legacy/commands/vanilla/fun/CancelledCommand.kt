package net.perfectdreams.loritta.legacy.commands.vanilla.`fun`

import net.perfectdreams.loritta.legacy.api.commands.ArgumentType
import net.perfectdreams.loritta.legacy.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.utils.Emotes
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class CancelledCommand(val m: LorittaDiscord) : LorittaAbstractCommandBase(m, listOf("cancelled", "cancelado", "cancel", "cancelar"), CommandCategory.FUN) {
	override fun command() = create {
		localizedDescription("commands.command.cancelled.description")
		localizedExamples("commands.command.cancelled.examples")

		usage {
			argument(ArgumentType.USER) {}
		}

		executes {
			OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "cancelled")

			val user = user(0) ?: run { explain(); return@executes }

			reply(
					LorittaReply(
							locale["commands.command.cancelled.wasCancelled", user.asMention, locale.getList("commands.command.cancelled.reasons").random()],
							Emotes.LORI_HMPF
					)
			)
		}
	}
}