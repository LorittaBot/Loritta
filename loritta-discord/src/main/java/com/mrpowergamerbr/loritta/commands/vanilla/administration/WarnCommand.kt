package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.github.salomonbrys.kotson.nullString
import com.github.salomonbrys.kotson.obj
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.Warn
import com.mrpowergamerbr.loritta.tables.Warns
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.convertToEpochMillisRelativeToNow
import com.mrpowergamerbr.loritta.utils.extensions.getValidMembersForPunishment
import com.mrpowergamerbr.loritta.utils.extensions.handlePunishmentConfirmation
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.locale.getLegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class WarnCommand : AbstractCommand("warn", listOf("aviso"), CommandCategory.ADMIN) {
	companion object {
		private const val LOCALE_PREFIX = "commands.moderation"
	}

	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.warn.description"]
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
		if (context.args.isEmpty()) return this.explain(context)

		val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return
		val members = context.getValidMembersForPunishment(users)

		if (members.isEmpty()) return

		val settings = AdminUtils.retrieveModerationInfo(context.config)
		val punishmentActions = AdminUtils.retrieveWarnPunishmentActions(context.config)
		val (reason, skipConfirmation, silent, _) = AdminUtils.getOptions(context, rawReason) ?: return

		val profileLocale = context.lorittaUser.profile.getLegacyBaseLocale(loritta, locale)

		val warnCallback: (suspend (Message?, Boolean) -> Unit) = { message, isSilent ->
			for (user in users) {
				val member = context.guild.retrieveMemberOrNull(user)
				if (!isSilent) {
					if (settings.sendPunishmentViaDm && context.guild.isMember(user)) {
						try {
							val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(context.guild, profileLocale, context.userHandle, context.locale["$LOCALE_PREFIX.warn.punishAction"], reason)

							user.openPrivateChannel().queue {
								it.sendMessage(embed).queue()
							}
						} catch (e: Exception) {
							e.printStackTrace()
						}
					}

					val punishLogMessage = AdminUtils.getPunishmentForMessage(
							settings,
							context.guild,
							PunishmentAction.WARN
					)

					if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
						val textChannel = context.guild.getTextChannelById(settings.punishLogChannelId)

						if (textChannel != null && textChannel.canTalk()) {
							val message = MessageUtils.generateMessage(
									punishLogMessage,
									listOf(user, context.guild),
									context.guild,
									mutableMapOf(
											"duration" to locale.toNewLocale()["$LOCALE_PREFIX.mute.forever"]
									) + AdminUtils.getStaffCustomTokens(context.userHandle)
											+ AdminUtils.getPunishmentCustomTokens(locale.toNewLocale(), reason, "$LOCALE_PREFIX.warn")
							)

							message?.let {
								textChannel.sendMessage(it).queue()
							}
						}
					}
				}

				val warnCount = (
						loritta.newSuspendedTransaction {
							Warns.select { (Warns.guildId eq context.guild.idLong) and (Warns.userId eq user.idLong) }.count()
						} + 1
						).toInt()

				val punishments = punishmentActions.filter { it.warnCount == warnCount }

				loop@ for (punishment in punishments) {
					when {
						punishment.punishmentAction == PunishmentAction.BAN -> BanCommand.ban(settings, context.guild, context.userHandle, locale, user, reason, isSilent, 0)
						member != null && punishment.punishmentAction == PunishmentAction.KICK -> KickCommand.kick(context, settings, locale, member, user, reason, isSilent)
						member != null && punishment.punishmentAction == PunishmentAction.MUTE -> {
							val metadata = punishment.metadata ?: continue@loop
							val obj = metadata.obj
							val time = obj["time"].nullString?.convertToEpochMillisRelativeToNow()
							MuteCommand.muteUser(context, settings, member, time, locale, user, reason, isSilent)
						}
					}
				}

				loritta.newSuspendedTransaction {
					Warn.new {
						this.guildId = context.guild.idLong
						this.userId = user.idLong
						this.receivedAt = System.currentTimeMillis()
						this.punishedById = context.userHandle.idLong
						this.content = reason
					}
				}
			}

			message?.delete()?.queue()

			AdminUtils.sendSuccessfullyPunishedMessage(context, reason, true)
		}

		if (skipConfirmation) {
			warnCallback.invoke(null, silent)
			return
		}

		val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
		val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "warn")

		context.handlePunishmentConfirmation(message, warnCallback)
	}
}
