package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.AutomodModule
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.commands.CommandCategory

class AntiRaidCommand : AbstractCommand("antiraid", category = CommandCategory.MAGIC) {
	override fun getDescription(locale: BaseLocale): String {
		return "Configura servidores na Lori's Server List"
	}

	override suspend fun run(context: CommandContext, locale: BaseLocale) {
		if (context.message.channel.id != "393332226881880074" && context.message.channel.id != "358774895850815488") {
			return
		}

		if (context.args.getOrNull(0) == "set") {
			AutomodModule.ANTIRAID_ENABLED = context.args[1].toBoolean()
			context.reply(
                    LorittaReply(
                            "Agora o antiraid está ${AutomodModule.ANTIRAID_ENABLED}!"
                    )
			)
			return
		}

		context.reply(
                LorittaReply(
                        "AntiRaid está ${AutomodModule.ANTIRAID_ENABLED}"
                ),
                LorittaReply(
                        "SIMILAR_MESSAGE_MULTIPLIER: ${AutomodModule.SIMILAR_MESSAGE_MULTIPLIER}"
                ),
                LorittaReply(
                        "SIMILARITY_THRESHOLD: ${AutomodModule.SIMILARITY_THRESHOLD}"
                ),
                LorittaReply(
                        "ATTACHED_IMAGE_SCORE: ${AutomodModule.ATTACHED_IMAGE_SCORE}"
                ),
                LorittaReply(
                        "SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER: ${AutomodModule.SIMILAR_SAME_AUTHOR_MESSAGE_MULTIPLIER}"
                ),
                LorittaReply(
                        "NO_AVATAR_SCORE: ${AutomodModule.NO_AVATAR_SCORE}"
                ),
                LorittaReply(
                        "MUTUAL_GUILDS_MULTIPLIER: ${AutomodModule.MUTUAL_GUILDS_MULTIPLIER}"
                ),
                LorittaReply(
                        "FRESH_ACCOUNT_DISCORD_MULTIPLIER: ${AutomodModule.FRESH_ACCOUNT_DISCORD_MULTIPLIER}"
                ),
                LorittaReply(
                        "FRESH_ACCOUNT_JOINED_MULTIPLIER: ${AutomodModule.FRESH_ACCOUNT_JOINED_MULTIPLIER}"
                ),
                LorittaReply(
                        "BAN_THRESHOLD: ${AutomodModule.BAN_THRESHOLD}"
                )
		)
	}
}