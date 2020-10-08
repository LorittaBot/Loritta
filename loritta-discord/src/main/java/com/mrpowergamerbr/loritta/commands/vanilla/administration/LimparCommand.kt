package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.api.messages.LorittaReply
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class LimparCommand : AbstractCommand("clean", listOf("limpar", "clear"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["commands.moderation.clear.description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.NUMBER) {
				optional = false
			}
			argument(ArgumentType.USER){
				optional = true
			}
		}
	}

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return locale.toNewLocale().getList("commands.moderation.clear.examples")
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
				context.reply(
                        LorittaReply(
                                "${context.legacyLocale["INVALID_NUMBER", context.args[0]]}",
                                Constants.ERROR
                        )
				)
				return
			}

			if (toClear !in 2..100) {
				context.reply(
                        LorittaReply(
                                "${context.locale["commands.moderation.clear.invalidClearRange"]}",
                                Constants.ERROR
                        )
				)
				return
			}

			// Primeiros iremos deletar a mensagem do comando que o usuário enviou
			try { context.message.delete().await() } catch (e: Exception) { }

			val messages = context.event.textChannel!!.history.retrievePast(toClear).await()
			val oldMessages = messages.filter { (System.currentTimeMillis() / 1000) - it.timeCreated.toEpochSecond() > 14 * 24 * 60 * 60 }
			val pinnedMessages = messages.filter { it.isPinned }
			val allowedMessages  = messages.filter {(context.message.mentionedUsers.isEmpty() || (context.message.mentionedUsers.isNotEmpty() && context.message.mentionedUsers.contains(it.author))) && !oldMessages.contains(it) && !pinnedMessages.contains(it) }

			if (allowedMessages.isEmpty()) {
				context.reply(
                        LorittaReply(
                                "${context.locale["commands.moderation.clear.couldNotFindMessages"]}",
                                Constants.ERROR
                        )
				)
				return
			}

			if (allowedMessages.size !in 2..100) {
				context.reply(
                        LorittaReply(
                                "${context.locale["commands.moderation.clear.couldNotFindMessages"]}",
                                Constants.ERROR
                        )
				)
				return
			}

			// E agora realmente iremos apagar as mensagens!
			context.message.textChannel.deleteMessages(allowedMessages).await()
			if (oldMessages.size > 0 && pinnedMessages.size > 0) {
				context.sendMessage(context.locale["commands.moderation.clear.ignoredTooOldAndPinnedMessages", context.userHandle.asMention, oldMessages.size, pinnedMessages.size])
			} else if (oldMessages.size > 0) {
				context.sendMessage(context.locale["commands.moderation.clear.ignoredTooOldMessages", context.userHandle.asMention, oldMessages.size])
			} else if (pinnedMessages.size > 0) {
				context.sendMessage(context.locale["commands.moderation.clear.ignoredPinnedMessages", context.userHandle.asMention, pinnedMessages.size])
			} else {
				context.sendMessage(context.locale["commands.moderation.clear.success", context.userHandle.asMention])
			}
		} else {
			this.explain(context)
		}
	}
}
