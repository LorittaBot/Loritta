package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.utils.MiscUtil
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class LimparCommand : AbstractCommand("clean", listOf("limpar", "clear"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["LIMPAR_DESCRIPTION"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = false
			}
		}
	}


	override fun getUsage(): String {
		return "QuantasMensagens"
	}

	override fun getExamples(): List<String> {
		return listOf("10", "25", "7 @Tsugami", "50 @Tsugami @Tsumari")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE)
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MESSAGE_MANAGE, Permission.MESSAGE_HISTORY)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.isNotEmpty()) {
			val toClear = context.args[0].toIntOrNull()

			if (toClear == null) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.legacyLocale["INVALID_NUMBER", context.args[0]]}")
				return
			}

			if (toClear !in 2..100) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.legacyLocale["LIMPAR_INVALID_RANGE"]}")
				return
			}

			// Primeiros iremos deletar a mensagem do comando que o usuário enviou
			try { context.message.delete().await() } catch (e: Exception) {}

			var hasTooOldMessages = false
			val messages = context.event.textChannel!!.history.retrievePast(toClear).await()
			val allowedMessages = messages.asSequence().filter {
				if (context.message.mentionedUsers.isNotEmpty()) {
					context.message.mentionedUsers.contains(it.author)
				} else {
					true
				}
			}.filter {
				val twoWeeksAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000 - Constants.DISCORD_EPOCH shl Constants.TIMESTAMP_OFFSET.toInt()
				val isTooOld = MiscUtil.parseSnowflake(it.id) > twoWeeksAgo
				if (isTooOld) {
					hasTooOldMessages = true
				}
				isTooOld
			}.toList()

			if (allowedMessages.isEmpty()) {
				context.sendMessage("${Constants.ERROR} **|** ${context.userHandle.asMention} ${context.legacyLocale["LIMPAR_COUDLNT_FIND_MESSAGES"]}")
				return
			}

			if (allowedMessages.size !in 2..100) {
				context.sendMessage("${Constants.ERROR} **|** ${context.userHandle.asMention} ${context.legacyLocale["LIMPAR_COUDLNT_FIND_MESSAGES"]}")
				return
			}

			// E agora realmente iremos apagar as mensagens!
			context.message.textChannel.deleteMessages(allowedMessages).await()

			if (allowedMessages.size == messages.size) {
				context.sendMessage(context.legacyLocale["LIMPAR_SUCCESS", context.userHandle.asMention])
			} else if (hasTooOldMessages) {
				context.sendMessage(context.legacyLocale["LIMPAR_SUCCESS_IGNORED_TOO_OLD", context.userHandle.asMention, messages.size - allowedMessages.size])
			}
		} else {
			this.explain(context)
		}
	}
}