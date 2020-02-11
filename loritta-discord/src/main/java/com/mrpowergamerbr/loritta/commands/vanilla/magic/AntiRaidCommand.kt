package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.AutomodModule
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale

class AntiRaidCommand : AbstractCommand("antiraid", category = CommandCategory.MAGIC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.message.channel.id != "393332226881880074" && context.message.channel.id != "358774895850815488") {
			return
		}

		if (context.args.getOrNull(0) == "set") {
			AutomodModule.ANTIRAID_ENABLED = context.args[1].toBoolean()
			context.reply(
					LoriReply(
							"Agora o antiraid está ${AutomodModule.ANTIRAID_ENABLED}!"
					)
			)
			return
		}

		context.reply(
				LoriReply(
						"AntiRaid está ${AutomodModule.ANTIRAID_ENABLED}"
				),
				LoriReply(
						"SIMILAR_MESSAGE_MULTIPLIER: ${AutomodModule.SIMILAR_MESSAGE_MULTIPLIER}"
				),
				LoriReply(
						"SIMILARITY_THRESHOLD: ${AutomodModule.SIMILARITY_THRESHOLD}"
				),
				LoriReply(
						"ATTACHED_IMAGE_SCORE: ${AutomodModule.ATTACHED_IMAGE_SCORE}"
				),
				LoriReply(
						"SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER: ${AutomodModule.SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER}"
				),
				LoriReply(
						"NO_AVATAR_SCORE: ${AutomodModule.NO_AVATAR_SCORE}"
				),
				LoriReply(
						"MUTUAL_GUILDS_MULTIPLIER: ${AutomodModule.MUTUAL_GUILDS_MULTIPLIER}"
				),
				LoriReply(
						"FRESH_ACCOUNT_DISCORD_MULTIPLIER: ${AutomodModule.FRESH_ACCOUNT_DISCORD_MULTIPLIER}"
				),
				LoriReply(
						"FRESH_ACCOUNT_JOINED_MULTIPLIER: ${AutomodModule.FRESH_ACCOUNT_JOINED_MULTIPLIER}"
				),
				LoriReply(
						"BAN_THRESHOLD: ${AutomodModule.BAN_THRESHOLD}"
				)
		)
	}
}