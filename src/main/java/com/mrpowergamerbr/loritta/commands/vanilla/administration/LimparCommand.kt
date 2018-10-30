package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.utils.MiscUtil

class LimparCommand : AbstractCommand("clean", listOf("limpar", "clear"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["LIMPAR_DESCRIPTION"]
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

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val toClear = context.args[0].toIntOrNull()

			if (toClear == null) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["INVALID_NUMBER", context.args[0]]}")
				return
			}

			if (toClear !in 2..100) {
				context.sendMessage("${Constants.ERROR} **|** ${context.getAsMention(true)}${context.locale["LIMPAR_INVALID_RANGE"]}")
				return
			}

			// Primeiros iremos deletar a mensagem do comando que o usuário enviou
			try { context.message.delete().await() } catch (e: Exception) {}

			val messages = context.event.textChannel!!.history.retrievePast(toClear).await()
			val allowedMessages = messages.asSequence().filter {
				if (context.message.mentionedUsers.isNotEmpty()) {
					context.message.mentionedUsers.contains(it.author)
				} else {
					true
				}
			}.filter {
				val twoWeeksAgo = System.currentTimeMillis() - 14 * 24 * 60 * 60 * 1000 - MiscUtil.DISCORD_EPOCH shl MiscUtil.TIMESTAMP_OFFSET.toInt()
				MiscUtil.parseSnowflake(it.id) > twoWeeksAgo
			}.toList()

			if (allowedMessages.isEmpty()) {
				context.sendMessage("${Constants.ERROR} **|** ${context.userHandle.asMention} ${context.locale["LIMPAR_COUDLNT_FIND_MESSAGES"]}")
				return
			}

			if (allowedMessages.size !in 2..100) {
				context.sendMessage("${Constants.ERROR} **|** ${context.userHandle.asMention} ${context.locale["LIMPAR_COUDLNT_FIND_MESSAGES"]}")
				return
			}

			// E agora realmente iremos apagar as mensagens!
			context.message.textChannel.deleteMessages(allowedMessages).await()

			if (allowedMessages.size == messages.size) {
				context.sendMessage(context.locale["LIMPAR_SUCCESS", context.userHandle.asMention])
			} else {
				context.sendMessage(context.locale["LIMPAR_SUCCESS_IGNORED_TOO_OLD", context.userHandle.asMention, messages.size - allowedMessages.size])
			}
		} else {
			this.explain(context)
		}
	}
}