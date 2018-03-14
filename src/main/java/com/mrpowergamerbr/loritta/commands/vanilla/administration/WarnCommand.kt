package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.ModerationConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import java.awt.Color
import java.time.Instant

class WarnCommand : AbstractCommand("warn", listOf("aviso"), CommandCategory.ADMIN) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["WARN_Description"]
	}

	override fun getExample(): List<String> {
		return listOf("159985870458322944", "159985870458322944 Algum motivo bastante aleatório");
	}

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val user = LorittaUtils.getUserFromContext(context, 0)

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

			val reason = rawArgs.joinToString(" ")

			var str = locale["BAN_ReadyToPunish", locale["WARN_PunishName"], user.asMention, user.name + "#" + user.discriminator, user.id]

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
					var isSilent = it.reactionEmote.name == "\uD83D\uDE4A"

					if (!isSilent) {
						if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
							try {
								val embed = EmbedBuilder()

								embed.setTimestamp(Instant.now())
								embed.setColor(Color(221, 0, 0))

								embed.setThumbnail(context.guild.iconUrl)
								embed.setAuthor(context.userHandle.name + "#" + context.userHandle.discriminator, null, context.userHandle.avatarUrl)
								embed.setTitle("\uD83D\uDEAB ${locale["BAN_YouAreBanned", locale["WARN_PunishAction"].toLowerCase(), context.guild.name]}!")
								embed.addField("\uD83D\uDC6E ${locale["BAN_PunishedBy"]}", context.userHandle.name + "#" + context.userHandle.discriminator, false)
								embed.addField("\uD83D\uDCDD ${locale["BAN_PunishmentReason"]}", reason, false)

								user.openPrivateChannel().complete().sendMessage(embed.build()).complete()
							} catch (e: Exception) {
								e.printStackTrace()
							}
						}

						if (context.config.moderationConfig.sendToPunishLog) {
							val textChannel = context.guild.getTextChannelById(context.config.moderationConfig.punishmentLogChannelId)

							if (textChannel != null && textChannel.canTalk()) {
								val message = MessageUtils.generateMessage(
										context.config.moderationConfig.punishmentLogMessage,
										null,
										context.guild,
										mutableMapOf(
												"reason" to reason,
												"punishment" to locale["WARN_PunishAction"],
												"staff" to context.userHandle.name,
												"@staff" to context.userHandle.asMention,
												"#staff" to context.userHandle.discriminator,
												"staff-avatar-url" to context.userHandle.avatarUrl,
												"user" to user.name,
												"@user" to user.asMention,
												"#user" to user.discriminator,
												"user-avatar-url" to user.effectiveAvatarUrl,
												"user-id" to user.id,
												"staff-id" to context.userHandle.id
										)
								)

								textChannel.sendMessage(message).complete()
							}
						}
					}

					val config = loritta.getServerConfigForGuild(context.guild.id)

					val userData = config.getUserData(
							user.id
					)

					val warnCount = userData.warns.size + 1

					val punishments = config.moderationConfig.punishmentActions.filter { it.warnCount == warnCount }

					for (punishment in punishments) {
						if (punishment.punishmentAction == ModerationConfig.PunishmentAction.BAN) BanCommand.ban(context, locale, user, reason, isSilent)
						else if (punishment.punishmentAction == ModerationConfig.PunishmentAction.SOFT_BAN) SoftBanCommand.softBan(context, locale, member, 30, user, reason, isSilent)
						else if (punishment.punishmentAction == ModerationConfig.PunishmentAction.KICK) KickCommand.kick(context, locale, member, user, reason, isSilent)
						else if (punishment.punishmentAction == ModerationConfig.PunishmentAction.MUTE) {
							val time = punishment.customMetadata0?.convertToEpochMillis()
							MuteCommand.muteUser(context, member, time, locale, user, reason, isSilent)
						}
					}

					userData.warns.add(
							ModerationConfig.Warn(
									reason,
									System.currentTimeMillis(),
									context.userHandle.id
							)
					)

					loritta save config

					message.delete().complete()

					context.reply(
							LoriReply(
									locale["BAN_SuccessfullyPunished"],
									"\uD83C\uDF89"
							)
					)
				}
				return@onReactionAddByAuthor
			}

			message.addReaction("✅").complete()
			if (hasSilent) {
				message.addReaction("\uD83D\uDE4A").complete()
			}
		} else {
			this.explain(context);
		}
	}
}