package net.perfectdreams.loritta.legacy.utils

import net.perfectdreams.loritta.legacy.commands.CommandContext
import net.perfectdreams.loritta.legacy.utils.stripCodeMarks
import net.perfectdreams.loritta.legacy.api.messages.LorittaReply
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordCommandContext

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