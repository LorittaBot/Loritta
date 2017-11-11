package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.utils.MiscUtil

class LimparCommand : CommandBase("limpar") {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LIMPAR_DESCRIPTION"]
	}

	override fun getUsage(): String {
		return "QuantasMensagens"
	}

	override fun getExample(): List<String> {
		return listOf("10", "25", "7 @Tsugami", "50 @Tsugami @Tsumari")
	}

	override fun getCategory(): CommandCategory {
		return CommandCategory.ADMIN
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val toClear = context.args[0].toIntOrNull()

			if (toClear == null) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["INVALID_NUMBER", context.args[0]]}")
				return
			}

			if (2 >= toClear) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["LIMPAR_INVALID_RANGE"]}")
				return
			}

			var aux = toClear
			var ignoredMessages = 0

			while (aux > 0) {
				val cleanUp = Math.min(aux, 100)
				aux -= cleanUp
				val toDelete = mutableListOf<String>()

				for (msg in context.event.textChannel.history.retrievePast(cleanUp).complete()) {
					val twoWeeksAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000 - MiscUtil.DISCORD_EPOCH shl MiscUtil.TIMESTAMP_OFFSET.toInt()
					if (context.message.mentionedUsers.isNotEmpty()) {
						if (!context.message.mentionedUsers.contains(msg.author)) {
							continue;
						}
					}
					if (MiscUtil.parseSnowflake(msg.id) > twoWeeksAgo) {
						toDelete.add(msg.id)
					} else {
						ignoredMessages++
					}
				}

				if (toDelete.size !in 2..100) {
					context.sendMessage("${Constants.ERROR} **|** ${context.userHandle.asMention} ${context.locale["LIMPAR_COUDLNT_FIND_MESSAGES"]}")
					return
				}

				context.event.textChannel.deleteMessagesByIds(toDelete).complete()
			}

			if (ignoredMessages == 0) {
				context.sendMessage(context.locale["LIMPAR_SUCCESS", context.asMention])
			} else {
				context.sendMessage(context.locale["LIMPAR_SUCCESS_IGNORED_TOO_OLD", context.asMention, ignoredMessages])
			}
		} else {
			this.explain(context)
		}
	}
}