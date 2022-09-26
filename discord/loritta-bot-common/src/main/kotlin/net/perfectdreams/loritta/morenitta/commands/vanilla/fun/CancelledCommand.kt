package net.perfectdreams.loritta.morenitta.commands.vanilla.`fun`

import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.morenitta.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class CancelledCommand(val m: LorittaBot) : LorittaAbstractCommandBase(m, listOf("cancelled", "cancelado", "cancel", "cancelar"), net.perfectdreams.loritta.common.commands.CommandCategory.FUN) {
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