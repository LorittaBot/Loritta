package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.time.Instant

class KickCommand : AbstractCommand("kick", listOf("expulsar", "kickar"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["KICK_Description"]
	}

	override fun getUsage(locale: BaseLocale): CommandArguments {
		return arguments {
			argument(ArgumentType.USER) {
				optional = false
			}
			argument(ArgumentType.TEXT) {
				optional = true
			}
		}
	}

	override fun getExamples(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
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

			if (member == null) {
				context.reply(
						LoriReply(
								locale["BAN_UserNotInThisServer"],
								Constants.ERROR
						)
				)
				return
			}

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

			var rawArgs = context.rawArgs
			rawArgs = rawArgs.remove(0) // remove o usuário

			var reason = rawArgs.joinToString(" ")

			val pipedReason = reason.split("|")

			var usingPipedArgs = false
			var skipConfirmation = context.config.getUserData(context.userHandle.id).quickPunishment
			var delDays = 0

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
					if (arg.endsWith("days") || arg.endsWith("dias") || arg.endsWith("day") || arg.endsWith("dia")) {
						delDays = it.split(" ")[0].toIntOrNull() ?: 0

						if (delDays > 7) {
							context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_FAIL_MORE_THAN_SEVEN_DAYS"]);
							return
						}
						if (0 > delDays) {
							context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + locale["SOFTBAN_FAIL_LESS_THAN_ZERO_DAYS"]);
							return
						}

						usingPipedArgs = true
					}
				}

				if (usingPipedArgs)
					reason = _reason
			}

			val kickCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				kick(context, locale, member, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
						LoriReply(
								locale["BAN_SuccessfullyPunished"],
								"\uD83C\uDF89"
						)
				)
			}

			if (skipConfirmation) {
				kickCallback.invoke(null, false)
				return
			}

			var str = locale["BAN_ReadyToPunish", locale["KICK_PunishName"], member.asMention, member.user.name + "#" + member.user.discriminator, member.user.id]

			val hasSilent = context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog
			if (context.config.moderationConfig.sendPunishmentViaDm || context.config.moderationConfig.sendToPunishLog) {
				str += " ${locale["BAN_SilentTip"]}"
			}

			val message = context.reply(
					LoriReply(
							message = str,
							prefix = "⚠"
					)
			)

			message.onReactionAddByAuthor(context) {
				if (it.reactionEmote.name == "✅" || it.reactionEmote.name == "\uD83D\uDE4A") {
					kickCallback.invoke(message, it.reactionEmote.name == "\uD83D\uDE4A")
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
		fun kick(context: CommandContext, locale: BaseLocale, member: Member, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
					try {
						val embed = EmbedBuilder()

						embed.setTimestamp(Instant.now())
						embed.setColor(Color(221, 0, 0))

						embed.setThumbnail(context.guild.iconUrl)
						embed.setAuthor(context.userHandle.name + "#" + context.userHandle.discriminator, null, context.userHandle.avatarUrl)
						embed.setTitle("\uD83D\uDEAB ${locale["BAN_YouAreBanned", locale["KICK_PunishAction"].toLowerCase(), context.guild.name]}!")
						embed.addField("\uD83D\uDC6E ${locale["BAN_PunishedBy"]}", context.userHandle.name + "#" + context.userHandle.discriminator, false)
						embed.addField("\uD83D\uDCDD ${locale["BAN_PunishmentReason"]}", reason, false)

						user.openPrivateChannel().queue {
							it.sendMessage(embed.build()).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				if (context.config.moderationConfig.sendToPunishLog) {
					val textChannel = context.guild.getTextChannelById(context.config.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								context.config.moderationConfig.punishmentLogMessage,
								listOf(user),
								context.guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale["KICK_PunishAction"],
										"staff" to context.userHandle.name,
										"@staff" to context.userHandle.asMention,
										"staff-discriminator" to context.userHandle.discriminator,
										"staff-avatar-url" to context.userHandle.avatarUrl,
										"staff-id" to context.userHandle.id
								)
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			context.guild.controller.kick(member, locale["BAN_PunishedBy"] + " ${context.userHandle.name}#${context.userHandle.discriminator} — ${locale["BAN_PunishmentReason"]}: ${reason}")
					.queue()
		}
	}
}