package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import java.util.*

class SayCommand : AbstractCommand("say", listOf("falar"), CommandCategory.MISC) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["SAY_Description"]
	}

	override fun getUsage(): String {
		return "mensagem"
	}

	override fun getExample(): List<String> {
		return Arrays.asList("Eu sou fofa! :3")
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			var args = context.rawArgs
			val channelId = context.rawArgs[0]

			// Pegando canal de texto, via menções, ID ou nada
			val channel = if (channelId.startsWith("<#") && channelId.endsWith(">")) {
				try {
					val ch = context.guild.getTextChannelById(channelId.substring(2, channelId.length - 1))
					args = args.remove(0)
					ch
				} catch (e: Exception) {
					null
				}
			} else {
				try {
					val ch = context.guild.getTextChannelById(channelId)
					args = args.remove(0)
					ch
				} catch (e: Exception) {
					null
				}
			} ?: context.event.channel

			if (channel is TextChannel) { // Caso seja text channel...
				if (!channel.canTalk()) {
					context.reply(
							LoriReply(
									"Eu não tenho permissão para falar no ${channel.asMention}!",
									Constants.ERROR
							)
					)
					return
				}
				if (!channel.canTalk(context.handle)) {
					context.reply(
							LoriReply(
									"Você não tem permissão para falar no ${channel.asMention}",
									Constants.ERROR
							)
					)
					return
				}
				if (context.config.blacklistedChannels.contains(channel.id) && !context.lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					context.reply(
							LoriReply(
									"Comandos não podem ser utilizados no ${channel.asMention}!",
									Constants.ERROR
							)
					)
					return
				}
			}

			var message = args.joinToString(" ")

			if (!context.isPrivateChannel && !context.handle.hasPermission(Permission.MESSAGE_MENTION_EVERYONE))
				message = message.escapeMentions()

			val discordMessage = try {
				MessageUtils.generateMessage(
						message,
						null,
						context.guild
				)
			} catch (e: Exception) {
				null
			}

			if (discordMessage != null)
				channel.sendMessage(discordMessage).complete()
			else
				channel.sendMessage(message).complete()

			if (context.event.channel != channel && channel is TextChannel)
				context.reply(
						LoriReply(
								"Mensagem enviada no ${channel.asMention} com sucesso!",
								"\uD83C\uDF89"
						)
				)
		} else {
			this.explain(context)
		}
	}
}