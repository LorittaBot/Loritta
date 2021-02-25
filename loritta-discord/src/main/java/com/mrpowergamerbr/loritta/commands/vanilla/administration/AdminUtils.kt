package com.mrpowergamerbr.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.stripCodeMarks
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.dao.servers.moduleconfigs.WarnAction
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.Placeholders
import net.perfectdreams.loritta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.Instant

object AdminUtils {
	private val LOCALE_PREFIX = "commands.category.moderation"
	val PUNISHMENT_EXAMPLES_KEY = LocaleKeyData("$LOCALE_PREFIX.punishmentExamples")
	val ROLE_TOO_LOW_KEY = LocaleKeyData("$LOCALE_PREFIX.roleTooLow")
	val ROLE_TOO_LOW_HOW_TO_FIX_KEY = LocaleKeyData("$LOCALE_PREFIX.roleTooLowHowToFix")
	val PUNISHER_ROLE_TOO_LOW_HOW_TO_FIX_KEY = LocaleKeyData("$LOCALE_PREFIX.punisherRoleTooLowHowToFix")

	val PUNISHMENT_USAGES = arguments {
		argument(ArgumentType.USER) {
			optional = false
		}
		argument(ArgumentType.TEXT) {
			optional = true
		}
	}

	/**
	 * Retrieves the moderation settings for the [serverConfig]
	 *
	 * @return the settings of the server or, if the server didn't configure the moderation module, default values
	 */
	suspend fun retrieveModerationInfo(serverConfig: ServerConfig): ModerationConfigSettings {
		val moderationConfig = loritta.newSuspendedTransaction {
			serverConfig.moderationConfig
		}

		return ModerationConfigSettings(
				moderationConfig?.sendPunishmentViaDm ?: false,
				moderationConfig?.sendPunishmentToPunishLog ?: false,
				moderationConfig?.punishLogChannelId,
				moderationConfig?.punishLogMessage
		)
	}

	/**
	 * Retrieves the punishment actions for warns, ordered by required warn count (ascending)
	 *
	 * @return the list of warn punishments, can be empty
	 */
	suspend fun retrieveWarnPunishmentActions(serverConfig: ServerConfig): List<WarnAction> {
		val moderationConfig = loritta.newSuspendedTransaction {
			serverConfig.moderationConfig
		} ?: return listOf()

		val warnActions = loritta.newSuspendedTransaction {
			WarnAction.find {
				WarnActions.config eq moderationConfig.id
			}.toList()
					.sortedBy { it.warnCount }
		}

		return warnActions
	}

	suspend fun checkAndRetrieveAllValidUsersFromMessages(context: CommandContext): UserMatchesResult? {
		val split = context.rawArgs
		var matchedCount = 0

		val validUsers = mutableListOf<User>()
		for (input in split) {
			// We don't want to query via other means, this would cause issues with Loritta detecting users as messages
			val shouldUseExtensiveMatching = validUsers.isEmpty()

			val matchedUser = DiscordUtils.extractUserFromString(
					input,
					context.message.mentionedUsers,
					context.guild,
					extractUserViaEffectiveName = shouldUseExtensiveMatching,
					extractUserViaUsername = shouldUseExtensiveMatching
			)

			if (matchedUser != null) {
				matchedCount++
				validUsers.add(matchedUser)
			}
			else break
		}

		if (validUsers.isEmpty()) {
			context.reply(
                    LorittaReply(
                            context.locale["commands.userDoesNotExist", context.rawArgs[0].stripCodeMarks()],
                            Emotes.LORI_HM
                    )
			)
			return null
		}

		return UserMatchesResult(validUsers, split.drop(matchedCount).joinToString(" "))
	}

	data class UserMatchesResult(
			val users: List<User>,
			val reason: String
	)

	suspend fun checkForUser(context: CommandContext): User? {
		val user = context.getUserAt(0)

		if (user == null) {
			context.reply(
                    LorittaReply(
                            context.locale["commands.userDoesNotExist", context.rawArgs[0].stripCodeMarks()],
                            Emotes.LORI_HM
                    )
			)
		}

		return user
	}

	suspend fun checkForPermissions(context: CommandContext, member: Member): Boolean {
		if (!context.guild.selfMember.canInteract(member)) {
			val reply = buildString {
				this.append(context.locale[ROLE_TOO_LOW_KEY])

				if (context.handle.hasPermission(Permission.MANAGE_ROLES)) {
					this.append(" ")
					this.append(context.locale[ROLE_TOO_LOW_HOW_TO_FIX_KEY])
				}
			}

			context.reply(
                    LorittaReply(
                            reply,
                            Constants.ERROR
                    )
			)
			return false
		}

		if (!context.handle.canInteract(member)) {
			val reply = buildString {
				this.append(context.locale[ROLE_TOO_LOW_KEY])

				if (context.handle.hasPermission(Permission.MANAGE_ROLES)) {
					this.append(" ")
					this.append(context.locale[PUNISHER_ROLE_TOO_LOW_HOW_TO_FIX_KEY])
				}
			}

			context.reply(
                    LorittaReply(
                            reply,
                            Constants.ERROR
                    )
			)
			return false
		}
		return true
	}

	suspend fun sendConfirmationMessage(context: CommandContext, users: List<User>, hasSilent: Boolean, type: String): Message {
		val str = context.locale["${LOCALE_PREFIX}.readyToPunish", context.locale["commands.command.$type.punishName"], users.joinToString { it.asMention }, users.joinToString { it.asTag }, users.joinToString { it.id }]

		val replies = mutableListOf(
                LorittaReply(
                        str,
                        "⚠"
                )
		)

		if (hasSilent) {
			replies += LorittaReply(
                    context.locale["${LOCALE_PREFIX}.silentTip"],
                    "\uD83D\uDC40",
                    mentionUser = false
            )
		}

		if (!context.config.getUserData(context.userHandle.idLong).quickPunishment) {
			replies += LorittaReply(
                    context.locale["${LOCALE_PREFIX}.skipConfirmationTip", "`${context.config.commandPrefix}quickpunishment`"],
                    mentionUser = false
            )
		}

		return context.reply(*replies.toTypedArray())
	}

	suspend fun sendConfirmationMessage(context: CommandContext, user: User, hasSilent: Boolean, type: String) = sendConfirmationMessage(context, listOf(user), hasSilent, type)

	suspend fun sendSuccessfullyPunishedMessage(context: CommandContext, reason: String, sendDiscordReportAdvise: Boolean) {
		val replies = mutableListOf(
                LorittaReply(
                        context.locale["${LOCALE_PREFIX}.successfullyPunished"] + " ${Emotes.LORI_RAGE}",
                        "\uD83C\uDF89"
                )
		)

		val reportExplanation = when {
			reason.contains("raid", true) -> context.locale["${LOCALE_PREFIX}.reports.raidReport"]
			reason.contains("porn", true) || reason.contains("nsfw", true) -> context.locale["${LOCALE_PREFIX}.reports.nsfwReport"]
			else -> null
		}

		if (reportExplanation != null) {
			replies.add(
                    LorittaReply(
                            context.locale["${LOCALE_PREFIX}.reports.pleaseReportToDiscord", reportExplanation, Emotes.LORI_PAT, "<${context.locale["${LOCALE_PREFIX}.reports.pleaseReportUrl"]}>"],
                            Emotes.LORI_HM,
                            mentionUser = false
                    )
			)
		}

		context.reply(
				*replies.toTypedArray()
		)
	}

	fun generateAuditLogMessage(locale: BaseLocale, punisher: User, reason: String) = locale["${LOCALE_PREFIX}.punishedLog", "${punisher.name}#${punisher.discriminator}", reason].substringIfNeeded(0 until 512)

	fun createPunishmentEmbedBuilderSentViaDirectMessage(guild: Guild, locale: BaseLocale, punisher: User, punishmentAction: String, reason: String): EmbedBuilder {
		val embed = EmbedBuilder()

		embed.setTimestamp(Instant.now())
		embed.setColor(Color(221, 0, 0))

		embed.setThumbnail(guild.iconUrl)
		embed.setAuthor(punisher.name + "#" + punisher.discriminator, null, punisher.avatarUrl)
		embed.setTitle("\uD83D\uDEAB ${locale["$LOCALE_PREFIX.youGotPunished", punishmentAction.toLowerCase(), guild.name]}!")
		embed.addField("\uD83D\uDC6E ${locale["$LOCALE_PREFIX.punishedBy"]}", punisher.name + "#" + punisher.discriminator, false)
		embed.addField("\uD83D\uDCDD ${locale["$LOCALE_PREFIX.punishmentReason"]}", reason, false)

		return embed
	}

	fun createPunishmentMessageSentViaDirectMessage(guild: Guild, locale: BaseLocale, punisher: User, punishmentAction: String, reason: String): MessageEmbed {
		return createPunishmentEmbedBuilderSentViaDirectMessage(guild, locale, punisher, punishmentAction, reason).build()
	}

	fun getPunishmentForMessage(settings: ModerationConfigSettings, guild: Guild, punishmentAction: PunishmentAction): String? {
		val messageConfig = transaction(Databases.loritta) {
			ModerationPunishmentMessagesConfig.select {
				ModerationPunishmentMessagesConfig.guild eq guild.idLong and
						(ModerationPunishmentMessagesConfig.punishmentAction eq punishmentAction)
			}.firstOrNull()
		}

		return messageConfig?.get(ModerationPunishmentMessagesConfig.punishLogMessage) ?: settings.punishLogMessage
	}

	suspend fun getOptions(context: CommandContext, rawReason: String): AdministrationOptions? {
		var reason = rawReason

		val pipedReason = reason.split("|")

		var usingPipedArgs = false
		var skipConfirmation = context.config.getUserData(context.userHandle.idLong).quickPunishment
		var delDays = 0

		var silent = false

		if (pipedReason.size > 1) {
			val pipedArgs=  pipedReason.toMutableList()
			val _reason = pipedArgs[0]
			pipedArgs.removeAt(0)

			pipedArgs.forEach {
				val arg = it.trim()
				if (arg == "force" || arg == "f") {
					skipConfirmation = true
					usingPipedArgs = true
				}
				if (arg == "s" || arg == "silent") {
					skipConfirmation = true
					usingPipedArgs = true
					silent = true
				}
				if (arg.endsWith("days") || arg.endsWith("dias") || arg.endsWith("day") || arg.endsWith("dia")) {
					delDays = arg.split(" ")[0].toIntOrNull() ?: 0

					if (delDays > 7) {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["$LOCALE_PREFIX.cantDeleteMessagesMoreThan7Days"])
						return null
					}
					if (0 > delDays) {
						context.sendMessage(Constants.ERROR + " **|** " + context.getAsMention(true) + context.locale["$LOCALE_PREFIX.cantDeleteMessagesLessThan0Days"])
						return null
					}

					usingPipedArgs = true
				}
			}

			if (usingPipedArgs)
				reason = _reason
		}

		val attachment = context.message.attachments.firstOrNull { it.isImage }

		if (attachment != null)
			reason += " " + attachment.url

		return AdministrationOptions(reason, skipConfirmation, silent, delDays)
	}

	fun getStaffCustomTokens(punisher: User) = mapOf(
			Placeholders.STAFF_NAME_SHORT.name to punisher.name,
			Placeholders.STAFF_NAME.name to punisher.name,
			Placeholders.STAFF_MENTION.name to punisher.asMention,
			Placeholders.STAFF_DISCRIMINATOR.name to punisher.discriminator,
			Placeholders.STAFF_AVATAR_URL.name to punisher.effectiveAvatarUrl,
			Placeholders.STAFF_ID.name to punisher.id,
			Placeholders.STAFF_TAG.name to punisher.asTag,

			Placeholders.Deprecated.STAFF_DISCRIMINATOR.name to punisher.discriminator,
			Placeholders.Deprecated.STAFF_AVATAR_URL.name to punisher.effectiveAvatarUrl,
			Placeholders.Deprecated.STAFF_ID.name to punisher.id
	)

	fun getPunishmentCustomTokens(locale: BaseLocale, reason: String, typePrefix: String) = mapOf(
			Placeholders.PUNISHMENT_REASON.name to reason,
			Placeholders.PUNISHMENT_REASON_SHORT.name to reason,

			Placeholders.PUNISHMENT_TYPE.name to locale["$typePrefix.punishAction"],
			Placeholders.PUNISHMENT_TYPE_SHORT.name to locale["$typePrefix.punishAction"]
	)

	data class AdministrationOptions(val reason: String, val skipConfirmation: Boolean, val silent: Boolean, val delDays: Int)
	data class ModerationConfigSettings(
			val sendPunishmentViaDm: Boolean,
			val sendPunishmentToPunishLog: Boolean,
			val punishLogChannelId: Long? = null,
			val punishLogMessage: String? = null
	)
}