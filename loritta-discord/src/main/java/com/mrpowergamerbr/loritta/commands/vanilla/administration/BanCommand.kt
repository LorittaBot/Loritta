package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.getValidMembersForPunishment
import com.mrpowergamerbr.loritta.utils.extensions.handlePunishmentConfirmation
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.CommandArguments
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.utils.PunishmentAction

class BanCommand : AbstractCommand("ban", listOf("banir", "hackban", "forceban"), CommandCategory.ADMIN) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale.toNewLocale()["$LOCALE_PREFIX.ban.description"]
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

	override fun getExamples(locale: LegacyBaseLocale): List<String> {
		return listOf("159985870458322944", "159985870458322944 ${locale.toNewLocale()["$LOCALE_PREFIX.ban.randomReason"]}")
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
		if (context.args.isEmpty()) return this.explain(context)

		val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return
		val members = context.getValidMembersForPunishment(users)

		if (members.isEmpty()) return

		val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return

		val settings = AdminUtils.retrieveModerationInfo(context.config)

		val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
			for (user in users)
				ban(settings, context.guild, context.userHandle, locale, user, reason, isSilent, delDays)

			message?.delete()?.queue()

			AdminUtils.sendSuccessfullyPunishedMessage(context, reason, delDays == 0)
		}

		if (skipConfirmation) {
			banCallback.invoke(null, silent)
			return
		}

		val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
		val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "ban")

		context.handlePunishmentConfirmation(message, banCallback)
	}

	companion object {
		private val LOCALE_PREFIX = "commands.moderation"

		fun ban(settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: LegacyBaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
			if (!isSilent) {
				if (settings.sendPunishmentViaDm && guild.isMember(user)) {
					try {
						val embed =  AdminUtils.createPunishmentMessageSentViaDirectMessage(guild, locale, punisher, locale.toNewLocale()["$LOCALE_PREFIX.ban.punishAction"], reason)

						user.openPrivateChannel().queue {
							it.sendMessage(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = AdminUtils.getPunishmentForMessage(
						settings,
						guild,
						PunishmentAction.BAN
				)

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = guild.getTextChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessage(
								punishLogMessage,
								listOf(user, guild),
								guild,
								mutableMapOf(
										"duration" to locale.toNewLocale()["$LOCALE_PREFIX.mute.forever"]
								) + AdminUtils.getStaffCustomTokens(punisher)
										+ AdminUtils.getPunishmentCustomTokens(locale.toNewLocale(), reason, "$LOCALE_PREFIX.ban")
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			guild.ban(user, delDays, AdminUtils.generateAuditLogMessage(locale.toNewLocale(), punisher, reason))
					.queue()
		}
	}
}
