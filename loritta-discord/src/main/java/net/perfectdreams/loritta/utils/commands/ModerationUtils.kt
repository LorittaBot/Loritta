package net.perfectdreams.loritta.utils.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.retrieveMemberOrNull
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.getBaseLocale
import kotlinx.atomicfu.atomic
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.perfectdreams.loritta.api.commands.CommandException
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.dao.servers.moduleconfigs.WarnAction
import net.perfectdreams.loritta.platform.discord.commands.DiscordCommandContext
import net.perfectdreams.loritta.tables.AuditLog
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.Placeholders
import net.perfectdreams.loritta.utils.PunishmentAction
import net.perfectdreams.loritta.utils.extensions.toJDA
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.time.Instant

/** The prefix to the according section */
const val LOCALE_PREFIX = "commands.moderation"

/** An alias to lazy punishments */
typealias LazyPunishment = suspend (Message?, Boolean) -> (Unit)

/**
 * Represents a server's moderation section config
 * with all the configurable data
 *
 * @see retrieveModerationInfo
 */
data class ModerationConfigSettings(
        val sendPunishmentViaDm: Boolean,
        val sendPunishmentToPunishLog: Boolean,
        val punishLogChannelId: Long? = null,
        val punishLogMessage: String? = null
)

/**
 * Represents a punishment data, containing all valid users and the punishment reason
 *
 * @property users All valid found users
 * @property reason The punishment reason
 */
data class ProvidedPunishmentData(
        val users: List<User>,
        val reason: String
)

/**
 * All modifiers that can be applied to a punishment, including:
 *
 * @property reason The punishment reason (can be empty)
 * @property skipConfirmation If we'll skip the punishment confirmation (represented by |f)
 * @property isSilent If we won't notify the user and the moderation logs about the punishment (represented by |s)
 * @property delDays The time of the messages that are going to be deleted (example |7 days)
 */
data class PunishmentStatementModifiers(
        val reason: String,
        val skipConfirmation: Boolean,
        val isSilent: Boolean,
        val delDays: Int
)

data class PunishmentStatementData(
        val users: List<User>,
        val members: List<Member>,
        val modifiers: PunishmentStatementModifiers
)

/**
 * Retrieves the moderation settings for the [serverConfig]
 *
 * @see ModerationConfigSettings
 * @return the settings of the server or, if the server didn't configure the moderation module, default values
 */
suspend fun retrieveModerationInfo(serverConfig: ServerConfig): ModerationConfigSettings {
    // Retrieving guild's moderation config
    val moderationConfig = loritta.newSuspendedTransaction { serverConfig.moderationConfig }

    // Adapting the config to a ModerationConfigSettings object
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
    // Returning an empty list if the guild's moderation config is null
    val moderationConfig = loritta.newSuspendedTransaction { serverConfig.moderationConfig } ?: return emptyList()

    // Here we're going to find the guild's warnings
    return loritta.newSuspendedTransaction {
        WarnAction.find {
            WarnActions.config eq moderationConfig.id
        }.toList().sortedBy {
            it.warnCount
        }
    }
}

/**
 * This will parse the [PunishmentStatementData], it's non-nullable, if an unexpected or expected error occurs,
 * the command execution will be killed throwing a [CommandException]
 *
 * @throws CommandException
 */
suspend fun DiscordCommandContext.parsePunishmentStatementData(): PunishmentStatementData {
    // Retrieving the raw reason (with all the modifiers and related stuff)
    val (users, rawReason) = this.checkAndRetrieveAllValidUsersFromMessages()
    // Parsing all users to members, if there's one invalid, we'll throw a CommandException
    val members = this.mapToMemberFollowingPunishmentRequirements(users)

    // Parsing modifiers to retrieve them & the stripped reason
    val modifiers = this.parsePunishmentModifiers(rawReason)

    return PunishmentStatementData(users, members, modifiers)
}

/**
 * Retrieve the following punishment's [ProvidedPunishmentData], including targets and reason
 * We ignore if the users are apt or not for the punishment, because the command itself must do that.
 *
 * @see ProvidedPunishmentData
 * @return The obtained data
 */
suspend fun DiscordCommandContext.checkAndRetrieveAllValidUsersFromMessages(): ProvidedPunishmentData {
    val validCount = atomic(0)
    val validUsers = args.mapNotNull {
        val shouldUseExtensiveMatching = validCount.value != 0

        // Trying to parse the user
        DiscordUtils.extractUserFromString(
                it,
                discordMessage.mentionedUsers,
                guild,
                extractUserViaEffectiveName = shouldUseExtensiveMatching,
                extractUserViaUsername = shouldUseExtensiveMatching
        )?.also { validCount += 1 }
    }

    // If no valid users were found, we're going to cancel the command execution sending a message
    if (validUsers.isEmpty()) {
        fail(
                LorittaReply(
                        locale["commands.userDoesNotExist", "`${args[0].stripCodeMarks()}`"],
                        Emotes.LORI_HM
                )
        )
    }

    return ProvidedPunishmentData(validUsers, args.drop(validUsers.size).joinToString(" "))
}

/**
 * This will check if the user and the [SelfUser] can both punish the provided target, and if not,
 * we'll fail the command execution.
 *
 * @param target The provided user
 */
fun DiscordCommandContext.checkForPunishmentPermissions(target: Member) {
    requireNotNull(member)

    // If Loritta can't interact with the user, we'll advise who tried to punish.
    if (guild.selfMember.canInteract(target).not()) {
        val reply = buildString {
            append(locale["$LOCALE_PREFIX.roleTooLow"])

            // Let's send how to fix Lori's role position if the user can fix it
            if (member.hasPermission(Permission.MANAGE_ROLES))
                append(" ${locale["$LOCALE_PREFIX.roleTooLowHowToFix"]}")
        }

        fail(reply, Constants.ERROR)
    } else if (member.canInteract(target).not()) {
        val reply = buildString {
            append(locale["$LOCALE_PREFIX.staffRoleTooLow"])

            if (member.hasPermission(Permission.MANAGE_ROLES))
                append(" ${locale["$LOCALE_PREFIX.staffRoleTooLowHowToFix"]}")
        }

        fail(reply, Constants.ERROR)
    }
}

/**
 * This will consider all apt members for the punishment, also using the [checkForPunishmentPermissions] method.
 * They must pass on the [checkForPunishmentPermissions] requirement and must be a member of the guild too.
 *
 * @see checkForPunishmentPermissions
 */
suspend fun DiscordCommandContext.mapToMemberFollowingPunishmentRequirements(users: List<User>): List<Member> = users.map {
    // Trying to parse the member, if he's not on the guild, we're failing the execution
    val member = guild.retrieveMemberOrNull(it) ?: fail(
            locale["commands.userNotOnTheGuild", "${it.asMention} (`${it.name.stripCodeMarks()}#${it.discriminator} (${it.idLong})`)"],
            Emotes.LORI_HM
    )

    // Checking if the user can be punished, and if so, we'll validate him
    checkForPunishmentPermissions(member); member
}

/**
 * This will just transform the [EmbedBuilder] from [createPunishmentEmbedBuilderSentViaDirectMessage] to a [MessageEmbed]
 *
 * @see createPunishmentEmbedBuilderSentViaDirectMessage
 */
fun createPunishmentMessageSentViaDirectMessage(guild: Guild, locale: BaseLocale, staff: User, punishmentAction: String, reason: String): MessageEmbed =
        createPunishmentEmbedBuilderSentViaDirectMessage(guild, locale, staff, punishmentAction, reason).build()

/**
 * This will just create the message that'll be sent to the punished user at his DMs (if public)
 *
 * @see createPunishmentMessageSentViaDirectMessage
 */
fun createPunishmentEmbedBuilderSentViaDirectMessage(guild: Guild, locale: BaseLocale, staff: User, punishmentAction: String, reason: String): EmbedBuilder = EmbedBuilder().also { embed ->
    embed.setTimestamp(Instant.now())
    embed.setColor(Color(221, 0, 0))

    embed.setThumbnail(guild.iconUrl)
    embed.setAuthor(staff.name + "#" + staff.discriminator, null, staff.avatarUrl)
    embed.setTitle("\uD83D\uDEAB ${locale["$LOCALE_PREFIX.youGotPunished", punishmentAction.toLowerCase(), guild.name]}!")
    embed.addField("\uD83D\uDC6E ${locale["$LOCALE_PREFIX.punishedBy"]}", staff.name + "#" + staff.discriminator, false)
    embed.addField("\uD83D\uDCDD ${locale["$LOCALE_PREFIX.punishmentReason"]}", reason, false)
}

/**
 * Here we'll get the message for the given [PunishmentAction]
 * It's configurable, you can customize it at Loritta's dashboard
 */
fun getMessageForPunishment(settings: ModerationConfigSettings, guild: Guild, action: PunishmentAction): String? {
    // Getting the config to the provided action
    val messageConfig = transaction(Databases.loritta) {
        ModerationPunishmentMessagesConfig.select {
            ModerationPunishmentMessagesConfig.guild eq guild.idLong and
                    (ModerationPunishmentMessagesConfig.punishmentAction eq action)
        }.firstOrNull()
    }

    // Getting the message from the config
    return messageConfig?.get(ModerationPunishmentMessagesConfig.punishLogMessage) ?: settings.punishLogMessage
}

/**
 * Parsing the placeholders with the provided user (usually the one who punished)
 *
 * @see getPunishmentCustomTokens
 */
fun getStaffCustomTokens(staff: User) = mapOf(
        Placeholders.STAFF_NAME_SHORT.name to staff.name,
        Placeholders.STAFF_NAME.name to staff.name,
        Placeholders.STAFF_MENTION.name to staff.asMention,
        Placeholders.STAFF_DISCRIMINATOR.name to staff.discriminator,
        Placeholders.STAFF_AVATAR_URL.name to staff.effectiveAvatarUrl,
        Placeholders.STAFF_ID.name to staff.id,
        Placeholders.STAFF_TAG.name to staff.asTag,

        Placeholders.Deprecated.STAFF_DISCRIMINATOR.name to staff.discriminator,
        Placeholders.Deprecated.STAFF_AVATAR_URL.name to staff.effectiveAvatarUrl,
        Placeholders.Deprecated.STAFF_ID.name to staff.id
)

/**
 * Parsing the placeholders with the provided reason and type
 *
 * @see getPunishmentCustomTokens
 */
fun getPunishmentCustomTokens(locale: BaseLocale, reason: String, typePrefix: String) = mapOf(
        Placeholders.PUNISHMENT_REASON.name to reason,
        Placeholders.PUNISHMENT_REASON_SHORT.name to reason,

        Placeholders.PUNISHMENT_TYPE.name to locale["$typePrefix.punishAction"],
        Placeholders.PUNISHMENT_TYPE_SHORT.name to locale["$typePrefix.punishAction"]
)

/**
 * Function to make [LazyPunishment] creation easier and cleaner.
 *
 * @see LazyPunishment
 */
fun createLazyPunishment(lazyPunishment: LazyPunishment): LazyPunishment =
        lazyPunishment

suspend fun DiscordCommandContext.createStandardLazyPunishment(handler: PunishmentHandler, settings: ModerationConfigSettings, data: PunishmentStatementData): LazyPunishment = createLazyPunishment { message, silent ->
    for (member in data.members) {
        val userLocale = member.user.getLorittaProfile()?.getBaseLocale(loritta as Loritta) ?: guildLocale

        // Applying punishment to user using the provided handler
        handler.applyPunishment(settings, guild, user, userLocale, guildLocale, member.user, data.modifiers.reason, silent, data.modifiers.delDays)
    }

    // Deleting the message if it's not a silent punishment
    message?.delete()?.queue()

    // Sending the "punished" message
    sendSuccessfullyPunishedMessage(data.modifiers.reason)
}

/**
 * Creating the logs to the punishment with the provided data
 *
 * @see AuditLog
 */
fun generateAuditLogMessage(locale: BaseLocale, staff: User, reason: String) =
        locale["$LOCALE_PREFIX.punishedLog", "${staff.name}#${staff.discriminator}", reason].substringIfNeeded(0 until 512)

/**
 * Sending the success message, usually after the [handlePunishmentConfirmation] or, if the user has the quick punishments enabled,
 * it will be executed at the command class itself.
 *
 * @see handlePunishmentConfirmation
 */
suspend fun DiscordCommandContext.sendSuccessfullyPunishedMessage(reason: String, sendDiscordReportAdvise: Boolean = true) {
    val replies = mutableListOf(
            LorittaReply(
                    locale["$LOCALE_PREFIX.successfullyPunished"] + " ${Emotes.LORI_RAGE}",
                    "\uD83C\uDF89"
            )
    )

    val reportExplanation = when {
        reason.contains("raid", true) -> locale["$LOCALE_PREFIX.reports.raidReport"]
        reason.contains("porn", true) || reason.contains("nsfw", true) -> locale["$LOCALE_PREFIX.reports.nsfwReport"]
        else -> null
    }

    if (reportExplanation != null && sendDiscordReportAdvise) {
        replies.add(
                LorittaReply(
                        locale["$LOCALE_PREFIX.reports.pleaseReportToDiscord", reportExplanation, Emotes.LORI_PAT, "<${locale["$LOCALE_PREFIX.reports.pleaseReportUrl"]}>"],
                        Emotes.LORI_HM,
                        mentionUser = false
                )
        )
    }

    reply(*replies.toTypedArray())
}

/**
 * Sending the confirmation to the execution's channel.
 * This will be handled at [handlePunishmentConfirmation] if the user doesn't have the quick punishments option enabled.
 *
 * @see handlePunishmentConfirmation
 */
suspend fun DiscordCommandContext.sendConfirmationMessage(users: List<User>, hasSilent: Boolean, type: String): Message {
    val str = locale["$LOCALE_PREFIX.readyToPunish", locale["$LOCALE_PREFIX.$type.punishName"], users.joinToString { it.asMention }, users.joinToString { it.asTag }, users.joinToString { it.id }]

    val replies = mutableListOf(
            LorittaReply(
                    str,
                    "⚠"
            )
    )

    if (hasSilent) {
        replies += LorittaReply(
                locale["$LOCALE_PREFIX.silentTip"],
                "\uD83D\uDC40",
                mentionUser = false
        )
    }

    if (!serverConfig.getUserData(user.idLong).quickPunishment) {
        replies += LorittaReply(
                locale["$LOCALE_PREFIX.skipConfirmationTip", "`${serverConfig.commandPrefix}quickpunishment`"],
                mentionUser = false
        )
    }

    return reply(*replies.toTypedArray()).toJDA()
}

/**
 * This will handle the lazy punishment to execute if it's silent or on the
 * confirmation at [handlePunishmentConfirmation]
 *
 * @see LazyPunishment
 * @see handlePunishmentConfirmation
 */
suspend fun DiscordCommandContext.handleLazyPunishment(settings: ModerationConfigSettings, punishment: LazyPunishment, data: PunishmentStatementData) {
    if (data.modifiers.skipConfirmation) {
        return punishment(null, data.modifiers.isSilent)
    }

    val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
    val message = this.sendConfirmationMessage(data.users, hasSilent, "ban")

    this.handlePunishmentConfirmation(message, punishment)
}

/**
 * This will handle the confirmation executing the provided [LazyPunishment] if
 * the user confirms the punishment.
 *
 * @see LazyPunishment
 */
suspend fun DiscordCommandContext.handlePunishmentConfirmation(message: Message, punishment: LazyPunishment) {
    // Handling the reaction
    message.onReactionAddByAuthor(this) {
        // Checking the emote if it's expected
        if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
            punishment(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
        }
        return@onReactionAddByAuthor
    }

    // Parsing the moderation settings
    // TODO: Add it to the parameters
    val settings = retrieveModerationInfo(serverConfig)
    val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog

    //Adding the reaction
    message.addReaction("✅").queue()
    if (hasSilent) {
        message.addReaction("\uD83D\uDE4A").queue()
    }
}


/**
 * Represents a class that will apply/handle all the punishments
 * Normally used by moderation commands classes' companion objects.
 *
 * @see applyPunishment
 * @see ModerationConfigSettings
 */
interface PunishmentHandler {

    /**
     * Method that will handle the punishment, usually call [handleNonSilentPunishment]
     * if [isSilent] is true.
     *
     * @see handleNonSilentPunishment
     */
    fun applyPunishment(settings: ModerationConfigSettings, guild: Guild, staff: User, guildLocale: BaseLocale, userLocale: BaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int)

    fun ModerationConfigSettings.handleNonSilentPunishment(type: PunishmentAction, guild: Guild, staff: User, serverLocale: BaseLocale, userLocale: BaseLocale, user: User, reason: String) {
        /** The locale prefix for the provided [PunishmentAction] */
        val prefix = "$LOCALE_PREFIX.${type.name.toLowerCase()}"

        if (this.sendPunishmentViaDm && guild.isMember(user)) {
            runCatching {
                val embed = createPunishmentMessageSentViaDirectMessage(guild, userLocale, staff, userLocale["$prefix.punishAction"], reason)

                user.openPrivateChannel().queue {
                    it.sendMessage(embed).queue()
                }
            }.onFailure { it.printStackTrace() }
        }

        val punishLogMessage = getMessageForPunishment(this, guild, type)

        if (this.sendPunishmentToPunishLog && this.punishLogChannelId != null && punishLogMessage != null) {
            val textChannel = guild.getTextChannelById(this.punishLogChannelId)

            if (textChannel != null && textChannel.canTalk()) {
                val message = MessageUtils.generateMessage(
                        punishLogMessage,
                        listOf(user, guild),
                        guild,
                        mutableMapOf(
                                "duration" to serverLocale["commands.moderation.mute.forever"]
                        ) + getStaffCustomTokens(staff) + getPunishmentCustomTokens(serverLocale, reason,prefix)
                )

                message?.let {
                    textChannel.sendMessage(it).queue()
                }
            }
        }
    }

}

/**
 * This will parse all [PunishmentStatementModifiers] from a punishment statement.
 *
 * @see PunishmentStatementData
 * @see PunishmentStatementModifiers
 */
suspend fun DiscordCommandContext.parsePunishmentModifiers(rawReason: String): PunishmentStatementModifiers {
    var reason = rawReason

    val pipedReason = reason.split("|")

    var delDays = 0
    var usingPipedArgs = false
    var skipConfirmation = serverConfig.getUserData(user.idLong).quickPunishment

    var silent = false

    if (pipedReason.size > 1) {
        val pipedArgs = pipedReason.toMutableList()
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
                    fail(locale["$LOCALE_PREFIX.cantDeleteMessagesMoreThan7Days"], Constants.ERROR)
                }
                if (0 > delDays) {
                    fail(locale["$LOCALE_PREFIX.cantDeleteMessagesLessThan0Days"], Constants.ERROR)
                }

                usingPipedArgs = true
            }
        }

        if (usingPipedArgs) reason = _reason
    }

    val attachment = discordMessage.attachments.firstOrNull { it.isImage }

    if (attachment != null) reason += " " + attachment.url

    return PunishmentStatementModifiers(reason, skipConfirmation, silent, delDays)
}