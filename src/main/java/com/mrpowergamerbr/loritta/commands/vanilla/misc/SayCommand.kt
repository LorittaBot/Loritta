package com.mrpowergamerbr.loritta.commands.vanilla.misc

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.TextChannel
import java.util.*
import java.util.regex.Pattern

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

				val matcher = Pattern.compile("[-a-zA-Z0-9@:%._\\+~#=]{2,256}\\.[a-z]{2,7}\\b([-a-zA-Z0-9@:%_\\+.~#?&//=]*)").matcher(message)

				while (matcher.find()) {
					var url = matcher.group()
					if (url.contains("discord") && url.contains("gg")) {
						url = "discord.gg" + matcher.group(1).replace(".", "")
					}

					val inviteId = MiscUtils.getInviteId("http://$url") ?: MiscUtils.getInviteId("https://$url")

					if (inviteId != null) { // INVITES DO DISCORD
						if (inviteId != "attachments" && inviteId != "forums" && !whitelisted.contains(inviteId))
							return // Tem convites válidos? Apenas ignore! A Lori irá aplicar as punições necessárias logo depois...
					}
				}
			}

			if (!context.isPrivateChannel && !context.handle.hasPermission(Permission.MESSAGE_MENTION_EVERYONE))
				message = message.escapeMentions()

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
				channel.sendMessage(discordMessage).complete()
			else
				channel.sendMessage(message).complete()

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