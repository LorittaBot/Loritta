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

class KickCommand {
	companion object {
		private val LOCALE_PREFIX = "commands.command"

		fun kick(loritta: LorittaBot, guild: Guild, i18nContext: I18nContext, punisher: User, settings: AdminUtils.ModerationConfigSettings, locale: BaseLocale, user: User, reason: String, isSilent: Boolean) {
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

				val punishLogMessage = runBlocking {
					AdminUtils.getPunishmentForMessage(
						loritta,
						settings,
						guild,
						PunishmentAction.KICK
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
								"duration" to locale["commands.command.mute.forever"]
							) + AdminUtils.getStaffCustomTokens(punisher) + AdminUtils.getPunishmentCustomTokens(locale, reason, "${LOCALE_PREFIX}.kick"),
							generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationKick
						)

						message?.let {
							textChannel.sendMessage(it).queue()
						}
					}
				}
			}

			// Log the punishment to the database
			runBlocking {
				loritta.pudding.moderationLogs.logPunishment(
					guild.idLong,
					user.idLong,
					punisher.idLong,
					ModerationLogAction.KICK,
					reason,
					null
				)
			}

			guild.kick(user)
				.reason(AdminUtils.generateAuditLogMessage(locale, punisher, reason))
				.queue()
		}
	}
}
