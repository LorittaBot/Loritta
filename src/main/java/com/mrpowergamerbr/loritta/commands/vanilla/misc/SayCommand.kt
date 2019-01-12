package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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

			val inviteBlockerConfig = context.config.inviteBlockerConfig
			val checkInviteLinks = inviteBlockerConfig.isEnabled && !inviteBlockerConfig.whitelistedChannels.contains(channel.id) && !context.lorittaUser.hasPermission(LorittaPermission.ALLOW_INVITES)

			if (checkInviteLinks) {
				val whitelisted = mutableListOf<String>()
				whitelisted.addAll(context.config.inviteBlockerConfig.whitelistedIds)

				InviteLinkModule.cachedInviteLinks[context.guild.id]?.forEach {
					whitelisted.add(it)
				}

				if (MiscUtils.hasInvite(message, whitelisted)) {
					return
				}
			}

			if (!context.isPrivateChannel && !context.handle.hasPermission(channel as TextChannel, Permission.MESSAGE_MENTION_EVERYONE))
				message = message.escapeMentions()

			if (!context.isPrivateChannel && !context.handle.hasPermission(channel as TextChannel, Permission.MESSAGE_MANAGE))
				message = "**${context.handle.asMention} me forçou a falar...** $message"

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