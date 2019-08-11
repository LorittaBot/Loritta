package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.TextChannel
import net.perfectdreams.loritta.api.commands.CommandCategory
import java.util.*

class SayCommand : AbstractCommand("say", listOf("falar"), CommandCategory.MISC) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["SAY_Description"]
	}

	override fun getUsage(): String {
		return "mensagem"
	}

	override fun getExamples(): List<String> {
		return Arrays.asList("Eu sou fofa! :3")
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_SERVER)
	}

	override suspend fun run(context: CommandContext, locale: LegacyBaseLocale) {
		if (context.rawArgs.isNotEmpty()) {
			var args = context.rawArgs
			val channelId = context.rawArgs[0]

			// Pegando canal de texto, via menções, ID ou nada
			val channel = if (args.size >= 2) {
				if (channelId.startsWith("<#") && channelId.endsWith(">")) {
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
				}
			} else { null } ?: context.event.channel

			if (channel is TextChannel) { // Caso seja text channel...
				if (!channel.canTalk()) {
					context.reply(
							LoriReply(
									locale["SAY_IDontHavePermissionToTalkIn", channel.asMention],
									Constants.ERROR
							)
					)
					return
				}
				if (!channel.canTalk(context.handle)) {
					context.reply(
							LoriReply(
									locale["SAY_YouDontHavePermissionToTalkIn", channel.asMention],
									Constants.ERROR
							)
					)
					return
				}
				if (context.config.blacklistedChannels.contains(channel.id) && !context.lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
					context.reply(
							LoriReply(
									locale["SAY_CommandsCannotBeUsedIn", channel.asMention],
									Constants.ERROR
							)
					)
					return
				}
			}

			var message = args.joinToString(" ")

			if (!context.isPrivateChannel && !context.handle.hasPermission(channel as TextChannel, Permission.MESSAGE_MENTION_EVERYONE))
				message = message.escapeMentions()

			var useWebhook = false
			var webhook: TemmieWebhook? = null

			if (!context.isPrivateChannel && !context.handle.hasPermission(channel as TextChannel, Permission.MANAGE_SERVER)) {
				// Para evitar pessoas xingando outros membros usando a Loritta, vamos usar uma webhook com o mesmo nome do usuário para todas as ocasiões necessárias.
				// Mas *apenas* se o usuário está usando emojis que o webhook possa usar
				val usingExternalEmotes = context.message.emotes.any { it.guild != context.guild }
				if (!usingExternalEmotes) { // Se não está usando, vamos usar webhooks!

					useWebhook = true
					try {
						webhook = WebhookUtils.getOrCreateWebhook(channel, "User sent +say")

						if (webhook == null) {
							message = "**${context.handle.asMention} me forçou a falar...** $message"
						}
					} catch (e: Exception) {}
				} else {
					message = "**${context.handle.asMention} me forçou a falar...** $message"
				}
			}

			if (useWebhook && webhook != null) {
				webhook.sendMessage(
						DiscordMessage(
								context.userHandle.name,
								message,
								context.userHandle.effectiveAvatarUrl
						)
				)
			} else {
				val discordMessage = try {
					MessageUtils.generateMessage(
							message,
							listOf(context.guild, context.userHandle),
							context.guild
					)
				} catch (e: Exception) {
					null
				}

				if (discordMessage != null)
					channel.sendMessage(discordMessage).queue()
				else
					channel.sendMessage(message).queue()
			}

			if (context.event.channel != channel && channel is TextChannel)
				context.reply(
						LoriReply(
								locale["SAY_MessageSuccessfullySent", channel.asMention],
								"\uD83C\uDF89"
						)
				)

		} else {
			this.explain(context)
		}
	}
}