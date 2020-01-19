package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LoriReply

object GenericReplies {
	suspend fun invalidNumber(context: CommandContext, value: String) {
		context.reply(
				LoriReply(
						context.locale["commands.invalidNumber", value] + " ${Emotes.LORI_CRYING}",
						Emotes.LORI_HM
				)
		)
	}
}