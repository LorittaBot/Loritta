package net.perfectdreams.loritta.morenitta.utils

import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.common.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext

object GenericReplies {
	suspend fun invalidNumber(context: CommandContext, value: String) {
		context.reply(
                LorittaReply(
                        context.locale["commands.invalidNumber", value.stripCodeMarks()] + " ${Emotes.LORI_CRYING}",
                        Emotes.LORI_HM
                )
		)
	}

	fun invalidNumber(context: DiscordCommandContext, value: String): Nothing = context.fail(context.locale["commands.invalidNumber", value] + " ${Emotes.LORI_CRYING}", Emotes.LORI_HM)
}