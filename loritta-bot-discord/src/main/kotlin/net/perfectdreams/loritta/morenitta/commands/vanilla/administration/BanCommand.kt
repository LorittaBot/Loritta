package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import java.util.concurrent.TimeUnit

class BanCommand(loritta: LorittaBot) : AbstractCommand(loritta, "ban", listOf("banir", "hackban", "forceban"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.ban.description")
	override fun getExamplesKey() = AdminUtils.PUNISHMENT_EXAMPLES_KEY
	override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

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
			OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "ban")

			val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

			for (user in users) {
				val member = context.guild.retrieveMemberOrNull(user)

				if (member != null) {
					if (!AdminUtils.checkForPermissions(context, member))
						return
				}
			}

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return

			val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				for (user in users)
					ban(loritta, context.i18nContext, settings, context.guild, context.userHandle, locale, user, reason, isSilent, delDays)

				message?.delete()?.queue()

				AdminUtils.sendSuccessfullyPunishedMessage(context, reason, delDays == 0)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, silent)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "ban")

			message.onReactionAddByAuthor(context) {
				if (it.emoji.isEmote("✅") || it.emoji.isEmote("\uD83D\uDE4A")) {
					banCallback.invoke(message, it.emoji.isEmote("\uD83D\uDE4A"))
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
