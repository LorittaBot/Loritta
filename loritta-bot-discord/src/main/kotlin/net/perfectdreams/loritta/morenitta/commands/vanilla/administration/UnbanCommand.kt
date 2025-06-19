package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor

class UnbanCommand(loritta: LorittaBot) : AbstractCommand(loritta, "unban", listOf("desbanir"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.unban.description")
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
			val (users, rawReason) = AdminUtils.checkAndRetrieveAllValidUsersFromMessages(context) ?: return

			for (user in users) {
				val member = context.guild.getMember(user)

				if (member != null) {
					context.reply(
						LorittaReply(
							locale["$LOCALE_PREFIX.unban.userIsInTheGuild"],
							Constants.ERROR
						)
					)
					return
				}
			}

			val (reason, skipConfirmation, silent, delDays) = AdminUtils.getOptions(context, rawReason) ?: return
			val settings = AdminUtils.retrieveModerationInfo(loritta, context.config)

			val banCallback: suspend (Message?, Boolean) -> (Unit) = { message, isSilent ->
				for (user in users)
					unban(loritta, context.i18nContext, settings, context.guild, context.userHandle, locale, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
					LorittaReply(
						locale["$LOCALE_PREFIX.unban.successfullyUnbanned"] + " ${Emotes.LORI_HMPF}",
						"\uD83C\uDF89"
					)
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, false)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "unban")

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
		private const val LOCALE_PREFIX = "commands.command"

		fun unban(loritta: LorittaBot, i18nContext: I18nContext, settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				val punishLogMessage = runBlocking {
					AdminUtils.getPunishmentForMessage(
						loritta,
						settings,
						guild,
						PunishmentAction.UNBAN
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
									+ AdminUtils.getPunishmentCustomTokens(locale, reason, "${LOCALE_PREFIX}.unban"),
							generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationUnban
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			// Log the punishment to the moderation logs
			runBlocking {
				loritta.pudding.moderationLogs.logPunishment(
					guild.idLong,
					user.idLong,
					punisher.idLong,
					ModerationLogAction.UNBAN,
					reason,
					null
				)
			}

			guild.unban(user).reason(AdminUtils.generateAuditLogMessage(locale, punisher, reason))
				.queue()
		}
	}
}
