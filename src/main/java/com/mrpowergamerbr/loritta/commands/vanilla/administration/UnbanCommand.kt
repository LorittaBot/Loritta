package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User

class UnbanCommand : AbstractCommand("unban", listOf("desbanir"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["UNBAN_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Pediu desculpas pelo o que aconteceu, e prometeu que o que aconteceu não irá se repetir.", "395935916952256523 Bani sem querer, desculpa!");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = context.getUserAt(0)

			if (user == null) {
				context.reply(
						LoriReply(
								locale["BAN_UserDoesntExist"],
								Constants.ERROR
						)
				)
				return
			}

			val member = context.guild.getMember(user)

			if (member != null) {
				if (!context.guild.selfMember.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_RoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}

				if (!context.handle.canInteract(member)) {
					context.reply(
							LoriReply(
									locale["BAN_PunisherRoleTooLow"],
									Constants.ERROR
							)
					)
					return
				}
			}

			var rawArgs = context.rawArgs
			rawArgs = rawArgs.remove(0) // remove o usuário

			var reason = rawArgs.joinToString(" ")

			val pipedReason = reason.split("|")

			var usingPipedArgs = false
			var skipConfirmation = context.config.getUserData(context.userHandle.id).quickPunishment

			if (pipedReason.size > 1) {
				val pipedArgs=  pipedReason.toMutableList()
				val _reason = pipedArgs[0]
				pipedArgs.removeAt(0)

				pipedArgs.forEach {
					val arg = it.trim()
					if (arg == "force") {
						skipConfirmation = true
						usingPipedArgs = true
					}
				}

				if (usingPipedArgs)
					reason = _reason
			}

			val banCallback: (Message?, Boolean) -> (Unit) = { message, isSilent ->
				unban(context.config, context.guild, context.userHandle, locale, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
						LoriReply(
								locale["UNBAN_SuccessfullyUnbanned"],
								"\uD83C\uDF89"
						)
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, false)
				return
			}

			var str = locale["BAN_ReadyToPunish", locale["UNBAN_PunishName"], user.asMention, user.name + "#" + user.discriminator, user.id]

			val hasSilent = context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog
			if (context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog) {
				str += " ${locale["BAN_SilentTip"]}"
			}

			val message = context.replyComplete(
					LoriReply(
							message = str,
							prefix = "⚠"
					)
			)

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.name == "✅" || it.reactionEmote.name == "\uD83D\uDE4A") {
					banCallback.invoke(message, it.reactionEmote.name == "\uD83D\uDE4A")
				}
				return@onReactionAddByAuthor
			}

			message.addReaction("✅").queue()
			if (hasSilent) {
				message.addReaction("\uD83D\uDE4A").queue()
			}
		} else {
			this.explain(context);
		}
	}

	companion object {
		fun unban(serverConfig: ServerConfig, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (serverConfig.moderationConfig.sendToPunishLog) {
					val textChannel = guild.getTextChannelById(serverConfig.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								serverConfig.moderationConfig.punishmentLogMessage,
								listOf(user),
								guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale["UNBAN_PunishAction"],
										"staff" to punisher.name,
										"@staff" to punisher.asMention,
										"staff-discriminator" to punisher.discriminator,
										"staff-avatar-url" to punisher.avatarUrl,
										"staff-id" to punisher.id
								)
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			guild.controller.unban(user).reason(locale["UNBAN_UnbannedBy"] + " ${punisher.name}#${punisher.discriminator} — ${locale["BAN_PunishmentReason"]}: ${reason}")
					.queue()
		}
	}
}