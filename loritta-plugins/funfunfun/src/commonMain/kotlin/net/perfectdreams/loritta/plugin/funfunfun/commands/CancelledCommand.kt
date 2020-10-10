package net.perfectdreams.loritta.plugin.funfunfun.commands

import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.LorittaAbstractCommandBase
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.plugin.funfunfun.FunFunFunPlugin
import net.perfectdreams.loritta.utils.Emotes

class CancelledCommand(val m: FunFunFunPlugin) : LorittaAbstractCommandBase(m.loritta, listOf("cancelled", "cancelado", "cancel", "cancelar"), CommandCategory.FUN) {
	override fun command() = create {
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