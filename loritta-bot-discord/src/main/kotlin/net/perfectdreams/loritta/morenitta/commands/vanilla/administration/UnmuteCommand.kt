package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import kotlinx.coroutines.runBlocking
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Mutes
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.ModerationLogAction
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.addReaction
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.retrieveMemberOrNull
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class UnmuteCommand(loritta: LorittaBot) : AbstractCommand(loritta, "unmute", listOf("desmutar", "desilenciar", "desilenciar"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.unmute.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.unmute.examples")
	override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

	override fun getDiscordPermissions(): List<Permission> {
		return listOf(Permission.KICK_MEMBERS)
	}

	override fun canUseInPrivateChannel(): Boolean {
		return false
	}

	override fun getBotPermissions(): List<Permission> {
		return listOf(Permission.MANAGE_ROLES, Permission.MANAGE_PERMISSIONS)
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
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
					unmute(loritta, context.i18nContext, settings, context.guild, context.userHandle, locale, user, reason, isSilent)

				message?.delete()?.queue()

				context.reply(
					LorittaReply(
						locale["commands.command.unmute.successfullyUnmuted"],
						"\uD83C\uDF89"
					)
				)
			}

			if (skipConfirmation) {
				banCallback.invoke(null, false)
				return
			}

			val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
			val message = AdminUtils.sendConfirmationMessage(context, users, hasSilent, "unmute")

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
		fun unmute(loritta: LorittaBot, i18nContext: I18nContext, settings: AdminUtils.ModerationConfigSettings, guild: Guild, punisher: User, locale: BaseLocale, user: User, reason: String, isSilent: Boolean) {
			if (!isSilent) {
				val punishLogMessage = runBlocking {
					AdminUtils.getPunishmentForMessage(
						loritta,
						settings,
						guild,
						PunishmentAction.UNMUTE
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
							) + AdminUtils.getStaffCustomTokens(punisher)
									+ AdminUtils.getPunishmentCustomTokens(locale, reason, "commands.command.unmute"),
							generationErrorMessageI18nKey = I18nKeysData.InvalidMessages.MemberModerationUnmute
						)

						textChannel.sendMessage(message).queue()
					}
				}
			}

			// Cancel the roleRemovalJob (if it exists, it may not exist at all!)
			val roleRemovalKey = guild.id + "#" + user.id
			val thread = MuteCommand.roleRemovalJobs[roleRemovalKey]
			thread?.cancel()
			MuteCommand.roleRemovalJobs.remove(roleRemovalKey)

			// Delete the mute from the database, this avoids the MutedUserTask rechecking the mute again even after it was deleted
			runBlocking {
				loritta.pudding.transaction {
					Mutes.deleteWhere {
						(Mutes.guildId eq guild.idLong) and (Mutes.userId eq user.idLong)
					}
				}

				// Log the punishment to the moderation logs
				loritta.pudding.moderationLogs.logPunishment(
					guild.idLong,
					user.idLong,
					punisher.idLong,
					ModerationLogAction.UNMUTE,
					reason,
					null
				)
			}

			// And now remove the "Muted" role if needed!
			val member = runBlocking { guild.retrieveMemberOrNull(user) }
			// Get the legacy "Muted" role if it exists
			val mutedRole = MuteCommand.getMutedRole(loritta, guild, locale)

			if (member != null) {
				member.removeTimeout().queue()

				if (mutedRole != null)
					guild.removeRoleFromMember(member, mutedRole).queue()
			}
		}
	}
}
