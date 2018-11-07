package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.User
import java.awt.Color
import java.time.Instant

class BanCommand : AbstractCommand("ban", listOf("banir", "hackban", "forceban"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["BAN_Description"]
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
		return listOf(Permission.BAN_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.BAN_MEMBERS)
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
			var delDays = 7
			
			var silent = false

			if (pipedReason.size > 1) {
				val pipedArgs=  pipedReason.toMutableList()
				val _reason = pipedArgs[0]
				pipedArgs.removeAt(0)

				pipedArgs.forEach {
					val arg = it.trim()
					if (arg == "force" || arg == "f") {
						skipConfirmation = true
						usingPipedArgs = true
					}
					if (arg == "s" || arg == "silent") {
						skipConfirmation = true
						usingPipedArgs = true
						silent = true
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

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				ban(context.config, context.guild, context.userHandle, locale, user, reason, isSilent, delDays)

				message?.delete()?.queue()

				context.reply(
						LoriReply(
								locale["BAN_SuccessfullyPunished"],
								"\uD83C\uDF89"
						)
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, silent)
				return
			}

			var str = locale["BAN_ReadyToPunish", locale["BAN_PunishName"], user.asMention, user.name + "#" + user.discriminator, user.id]

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
		fun ban(serverConfig: ServerConfig, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
			if (!isSilent) {
				if (serverConfig.moderationConfig.sendPunishmentViaDm && guild.isMember(user)) {
					try {
						val embed = EmbedBuilder()

						embed.setTimestamp(Instant.now())
						embed.setColor(Color(221, 0, 0))

						embed.setThumbnail(guild.iconUrl)
						embed.setAuthor(punisher.name + "#" + punisher.discriminator, null, punisher.avatarUrl)
						embed.setTitle("\uD83D\uDEAB ${locale["BAN_YouAreBanned", locale["BAN_PunishAction"].toLowerCase(), guild.name]}!")
						embed.addField("\uD83D\uDC6E ${locale["BAN_PunishedBy"]}", punisher.name + "#" + punisher.discriminator, false)
						embed.addField("\uD83D\uDCDD ${locale["BAN_PunishmentReason"]}", reason, false)

						user.openPrivateChannel().queue {
							it.sendMessage(embed.build()).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				if (serverConfig.moderationConfig.sendToPunishLog) {
					val textChannel = guild.getTextChannelById(serverConfig.moderationConfig.punishmentLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								serverConfig.moderationConfig.punishmentLogMessage,
								listOf(user),
								guild,
								mutableMapOf(
										"reason" to reason,
										"punishment" to locale["BAN_PunishAction"],
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

			guild.controller.ban(user, delDays, locale["BAN_PunishedBy"] + " ${punisher.name}#${punisher.discriminator} — ${locale["BAN_PunishmentReason"]}: ${reason}")
					.queue()
		}
	}
}
