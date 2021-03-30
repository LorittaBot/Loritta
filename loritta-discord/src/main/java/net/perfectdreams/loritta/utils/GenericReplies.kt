package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import net.perfectdreams.loritta.api.messages.LorittaReply

object GenericReplies {
	suspend fun invalidNumber(context: CommandContext, value: String) {
		context.reply(
                LorittaReply(
                        context.locale["commands.invalidNumber", value.stripCodeMarks()] + " ${Emotes.LORI_CRYING}",
                        Emotes.LORI_HM
                )
		)
	}
}