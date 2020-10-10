package net.perfectdreams.loritta.plugin.funfunfun.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funfunfun.commands.base.DSLCommandBase
import net.perfectdreams.loritta.utils.Emotes

object CancelledCommand : DSLCommandBase {
	override fun command(loritta: LorittaBot) = create(loritta, listOf("cancelled", "cancelado", "cancel", "cancelar")) {
		localizedDescription("commands.fun.cancelled.description")

		usage {
			argument(ArgumentType.USER) {}
		}

		executes {
			val user = user(0) ?: run { explain(); return@executes }

			reply(
					LorittaReply(
							locale["commands.fun.cancelled.wasCancelled", user.asMention, locale.getList("commands.fun.cancelled.reasons").random()],
							Emotes.LORI_HMPF
					)
			)
		}
	}
}