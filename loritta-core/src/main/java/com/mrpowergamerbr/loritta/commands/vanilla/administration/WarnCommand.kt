package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.userdata.ModerationConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class WarnCommand : AbstractCommand("warn", listOf("aviso"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["WARN_Description"]
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
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS, Permission.BAN_MEMBERS)
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

			val warnCallback: (suspend (Message?, Boolean) -> Unit) = { message, isSilent ->
				if (!isSilent) {
					if (context.config.moderationConfig.sendPunishmentViaDm && context.guild.isMember(user)) {
						try {
							val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, locale, context.userHandle, locale["WARN_PunishAction"], reason)

							user.openPrivateChannel().queue {
								it.sendMessage(embed).queue()
							}
						} catch (e: Exception) {
							e.printStackTrace()
						}
					}

					if (context.config.moderationConfig.sendToPunishLog) {
						val textChannel = context.guild.getTextChannelByNullableId(context.config.moderationConfig.punishmentLogChannelId)

						if (textChannel != null && textChannel.canTalk()) {
							val message = MessageUtils.generateMessage(
									context.config.moderationConfig.punishmentLogMessage,
									listOf(user),
									context.guild,
									mutableMapOf(
											"reason" to reason,
											"punishment" to locale["WARN_PunishAction"],
											"staff" to context.userHandle.name,
											"@staff" to context.userHandle.asMention,
											"staff-discriminator" to context.userHandle.discriminator,
											"staff-avatar-url" to context.userHandle.effectiveAvatarUrl,
											"staff-id" to context.userHandle.id,
											"duration" to locale.toNewLocale()["commands.moderation.mute.forever"]
									)
							)

							textChannel.sendMessage(message!!).queue()
						}
					}
				}

				val config = loritta.getServerConfigForGuild(context.guild.id)

				val warnCount = transaction(Databases.loritta) {
					Warns.select { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.count()
				} + 1

				val punishments = config.moderationConfig.punishmentActions.filter { it.warnCount == warnCount }

				for (punishment in punishments) {
					when {
						punishment.punishmentAction == ModerationConfig.PunishmentAction.BAN -> BanCommand.ban(context.config, context.guild, context.userHandle, locale, user, reason, isSilent, punishment.customMetadata1)
						member != null && punishment.punishmentAction == ModerationConfig.PunishmentAction.SOFT_BAN -> SoftBanCommand.softBan(context, locale, member, 7, user, reason, isSilent)
						member != null && punishment.punishmentAction == ModerationConfig.PunishmentAction.KICK -> KickCommand.kick(context, locale, member, user, reason, isSilent)
						member != null && punishment.punishmentAction == ModerationConfig.PunishmentAction.MUTE -> {
							val time = punishment.customMetadata0?.convertToEpochMillisRelativeToNow()
							MuteCommand.muteUser(context, member, time, locale, user, reason, isSilent)
						}
					}
				}

				transaction(Databases.loritta) {
					Warn.new {
						this.guildId = context.guild.idLong
						this.userId = user.idLong
						this.receivedAt = System.currentTimeMillis()
						this.punishedById = context.userHandle.idLong
						this.content = reason
					}
				}

				loritta save config

				message?.delete()?.queue()

				context.reply(
						LoriReply(
								locale["BAN_SuccessfullyPunished"],
								"\uD83C\uDF89"
						)
				)
			}

			if (skipConfirmation) {
				warnCallback.invoke(null, silent)
				return
			}

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
				if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
					warnCallback.invoke(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
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
}
