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
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import java.util.concurrent.TimeUnit

class BanCommand {
	companion object {
		private val LOCALE_PREFIX = "commands.command"

		fun ban(loritta: LorittaBot, i18nContext: I18nContext, settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
			if (!isSilent) {
				if (settings.sendPunishmentViaDm && guild.isMember(user)) {
					try {
						val embed =  AdminUtils.createPunishmentMessageSentViaDirectMessage(guild, locale, punisher, locale["$LOCALE_PREFIX.ban.punishAction"], reason)

						GlobalScope.launch {
							loritta.getOrRetrievePrivateChannelForUser(user).sendMessageEmbeds(embed).queue()
						}
					} catch (e: Exception) {
						e.printStackTrace()
					}
				}

				val punishLogMessage = runBlocking {
					AdminUtils.getPunishmentForMessage(
						loritta,
						settings,
						guild,
						PunishmentAction.BAN
					)
				}

				if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
					val textChannel = guild.getGuildMessageChannelById(settings.punishLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						val message = MessageUtils.generateMessageOrFallbackIfInvalid(
							i18nContext,
							punishLogMessage,
							listOf(user, guild),
							guild,
							mutableMapOf(
								"duration" to locale["$LOCALE_PREFIX.mute.forever"]
							) + AdminUtils.getStaffCustomTokens(punisher)
									+ AdminUtils.getPunishmentCustomTokens(locale, reason, "$LOCALE_PREFIX.ban"),
							generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationBan
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			// Log the punishment to the database
			runBlocking {
				loritta.pudding.moderationLogs.logPunishment(
					guild.idLong,
					user.idLong,
					punisher.idLong,
					ModerationLogAction.BAN,
					reason,
					null
				)
			}

			guild.ban(user, delDays, TimeUnit.DAYS)
				.reason(AdminUtils.generateAuditLogMessage(locale, punisher, reason))
				.queue()
		}
	}
}
