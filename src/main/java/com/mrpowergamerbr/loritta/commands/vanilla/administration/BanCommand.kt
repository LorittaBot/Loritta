package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.*
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.onReactionAddByAuthor
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments

class BanCommand : AbstractCommand("ban", listOf("banir", "hackban", "forceban"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["BAN_Description"]
	}

	override fun getUsage(locale: LegacyBaseLocale): CommandArguments {
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
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório")
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

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
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

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context) ?: return

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
			this.explain(context)
		}
	}

	companion object {
		fun ban(serverConfig: MongoServerConfig, guild: Guild, punisher: User, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
			if (!isSilent) {
				if (serverConfig.moderationConfig.sendPunishmentViaDm && guild.isMember(user)) {
					try {
						val embed =  AdminUtils.createPunishmentMessageSentViaDirectMessage(guild, locale, punisher, locale["BAN_PunishAction"], reason)

						user.openPrivateChannel().queue {
							it.sendMessage(embed).queue()
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
