package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.msgFormat
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.utils.MiscUtil




class LimparCommand : CommandBase() {
	override fun getLabel(): String {
		return "limpar"
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale.LIMPAR_DESCRIPTION.msgFormat()
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

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty()) {
			val toClear = context.args[0].toIntOrNull()

			if (toClear == null) {
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}${context.locale.INVALID_NUMBER.msgFormat(context.args[0])}")
				return
			}

			if (toClear !in 2..100) {
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}${context.locale.LIMPAR_INVALID_RANGE.msgFormat()}")
				return
			}

			val toDelete = mutableListOf<String>()
			var ignoredMessages = 0
			context.event.textChannel.history.retrievePast(toClear).complete().forEach { msg ->
				val twoWeeksAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000 - MiscUtil.DISCORD_EPOCH shl MiscUtil.TIMESTAMP_OFFSET.toInt()
				if (context.message.mentionedUsers.isNotEmpty()) {
					if (context.message.mentionedUsers.contains(msg.author)) {
						if (MiscUtil.parseSnowflake(msg.id) > twoWeeksAgo) {
							toDelete.add(msg.id)
						} else {
							ignoredMessages++
						}
					}
				} else {
					if (MiscUtil.parseSnowflake(msg.id) > twoWeeksAgo) {
						toDelete.add(msg.id)
					} else {
						ignoredMessages++
					}
				}
			}

			if (toDelete.size !in 2..100) {
				context.sendMessage("${LorittaUtils.ERROR} **|** ${context.getAsMention(true)}${context.locale.get("LIMPAR_COUDLNT_FIND_MESSAGES")}")
				return
			}

			context.event.textChannel.deleteMessagesByIds(toDelete).complete()

			if (ignoredMessages == 0) {
				context.sendMessage(context.locale.LIMPAR_SUCCESS.msgFormat(context.asMention))
			} else {
				context.sendMessage(context.locale.get("LIMPAR_SUCCESS_IGNORED_TOO_OLD", context.asMention, ignoredMessages))
			}
		} else {
			this.explain(context)
		}
	}
}