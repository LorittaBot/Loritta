package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import java.util.concurrent.TimeUnit

class KickCommand {
	companion object {
		private val LOCALE_PREFIX = "commands.command"

		suspend fun kick(
			loritta: LorittaBot,
			guild: Guild,
			i18nContext: I18nContext,
			punisher: User,
			settings: AdminUtils.ModerationConfigSettings,
			locale: BaseLocale,
			user: User,
			reason: String,
			isSilent: Boolean,
			deleteDays: Long?
		) {
			val punishmentAction = if (deleteDays != null) {
				PunishmentAction.PURGE_KICK
			} else {
				PunishmentAction.KICK
			}

			val moderationLogAction = if (deleteDays != null) {
				ModerationLogAction.PURGE_KICK
			} else {
				ModerationLogAction.KICK
			}

			if (!isSilent) {
				if (settings.sendPunishmentViaDm && guild.isMember(user)) {
					try {
						val embed = AdminUtils.createPunishmentMessageSentViaDirectMessage(guild, locale, punisher, locale["commands.command.kick.punishAction"], reason)

						GlobalScope.launch {
							loritta.getOrRetrievePrivateChannelForUser(user).sendMessageEmbeds(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = AdminUtils.getPunishmentForMessage(
					loritta,
					settings,
					guild,
					punishmentAction
				)

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = guild.getGuildMessageChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessageOrFallbackIfInvalid(
							i18nContext,
							punishLogMessage,
							listOf(user, guild),
							guild,
							mutableMapOf(
								"duration" to locale["commands.command.mute.forever"]
							) + AdminUtils.getStaffCustomTokens(punisher) + AdminUtils.getPunishmentCustomTokens(
								locale,
								reason,
								when (moderationLogAction) {
									ModerationLogAction.KICK -> "${LOCALE_PREFIX}.kick"
									ModerationLogAction.PURGE_KICK -> "${LOCALE_PREFIX}.purgekick"
									else -> error("Unsupported moderation log action $moderationLogAction! This should NEVER happen!!")
								}
							),
							generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationKick
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			// Log the punishment to the database
			loritta.pudding.moderationLogs.logPunishment(
				guild.idLong,
				user.idLong,
				punisher.idLong,
				moderationLogAction,
				reason,
				null
			)

			if (deleteDays != null) {
				guild.ban(user, deleteDays.toInt(), TimeUnit.DAYS)
					.reason(AdminUtils.generateAuditLogMessage(locale, punisher, reason))
					.await()

				// Wait for the ban and THEN unban them!
				guild.unban(user).reason(AdminUtils.generateAuditLogMessage(locale, punisher, reason)).await()
			} else {
				guild.kick(user)
					.reason(AdminUtils.generateAuditLogMessage(locale, punisher, reason))
					.await()
			}
		}
	}
}
