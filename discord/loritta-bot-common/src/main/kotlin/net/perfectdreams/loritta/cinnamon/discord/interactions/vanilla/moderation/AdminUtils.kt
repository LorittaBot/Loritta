package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation

import dev.kord.common.Color
import dev.kord.common.entity.Permission
import dev.kord.common.entity.Snowflake
import dev.kord.core.behavior.ban
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban.ConfirmBanData
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.declarations.BanCommand
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordResourceLimits
import net.perfectdreams.loritta.cinnamon.discord.utils.MessageUtils
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar
import net.perfectdreams.loritta.cinnamon.discord.utils.sources.StaffTokenSource
import net.perfectdreams.loritta.cinnamon.discord.utils.sources.UserTokenSource
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.common.utils.Placeholders
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis

object AdminUtils {
    private val USER_MENTION_REGEX = Regex("<@!?(\\d+)>")

    suspend fun appendCheckResultReason(loritta: LorittaBot, i18nContext: I18nContext, punisherMember: Member, builder: MessageBuilder, check: InteractionCheck) {
        val (issuer, target, result) = check

        val punisherMemberPermissions = punisherMember.getPermissions() // TODO: Get permissions from the interaction itself

        builder.apply {
            when (result) {
                InteractionCheckResult.TARGET_IS_OWNER -> {
                    styled(
                        i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.TargetIsOwner(target.mention)),
                        Emotes.LoriBonk
                    )
                }
                InteractionCheckResult.TARGET_ROLE_POSITION_HIGHER_OR_EQUAL_TO_ISSUER -> {
                    if (issuer.id == loritta.config.loritta.discord.applicationId) {
                        styled(
                            i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.RoleTooLow(target.mention)),
                            Emotes.LoriBonk
                        )

                        if (Permission.ManageRoles in punisherMemberPermissions) {
                            styled(
                                i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.RoleTooLowHowToFix),
                                Emotes.LoriReading
                            )
                        }
                    } else {
                        styled(
                            i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.PunisherRoleTooLow(target.mention)),
                            Emotes.LoriBonk
                        )

                        if (Permission.ManageRoles in punisherMemberPermissions) {
                            styled(
                                i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.PunisherRoleTooLowHowToFix),
                                Emotes.LoriReading
                            )
                        }
                    }
                }
                InteractionCheckResult.TRYING_TO_INTERACT_WITH_SELF -> {
                    if (issuer.id == loritta.config.loritta.discord.applicationId) {
                        styled(
                            i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.TargetIsLoritta),
                            Emotes.LoriBonk
                        )
                    } else {
                        styled(
                            i18nContext.get(BanCommand.CATEGORY_I18N_PREFIX.PunishmentInteractFailures.TargetIsSelf(issuer.mention)),
                            Emotes.LoriBonk
                        )
                    }
                }
                InteractionCheckResult.SUCCESS -> {
                    error("This should never happen!")
                }
            }
        }
    }

    suspend fun banUsers(loritta: LorittaBot, i18nContext: I18nContext, confirmBanData: ConfirmBanData) {
        val guild = Guild(confirmBanData.guild, loritta.kord)

        val sendPunishmentViaDirectMessage = confirmBanData.sendPunishmentViaDirectMessage
        val sendPunishmentToPunishmentLog = confirmBanData.sendPunishmentToPunishmentLog
        val reason = confirmBanData.reason ?: i18nContext.get(I18nKeysData.Commands.Category.Moderation.ReasonNotGiven)
        val users = confirmBanData.users
        val punisher = User(confirmBanData.punisher, loritta.kord)

        val punishmentMessageForType = loritta.pudding.serverConfigs.getMessageForPunishmentTypeOnGuildId(
            guild.id.value,
            PunishmentAction.BAN
        )?.ifBlank { null } // Because some stupid users love putting an empty message in the dashboard

        val punishmentLogChannelId = confirmBanData.punishmentLogChannelId

        // We will check if we really need to
        var canTalkInPunishmentLogChannel: Boolean? = null

        // TODO: Check if the server already has too many bans and, if it has, fallback to a "we will only ban after they join the server"
        // (Because in this case, Discord will reject the ban)
        for (userWithMemberData in users) {
            val userData = userWithMemberData.userData
            val memberData = userWithMemberData.memberData

            val user = User(userData, loritta.kord)
            val member = memberData?.let { Member(it, userData, loritta.kord) }

            // Don't try to send a direct message if the user is a bot
            // Also don't send a DM if the member isn't in the server, this avoids users using Loritta to send DM spam to users by
            // using the punishment feature
            if (sendPunishmentViaDirectMessage && member != null && !user.isBot) {
                loritta.sendMessageToUserViaDirectMessage(
                    user.id,
                    createDirectMessagePunishmentMessage(i18nContext, guild, punisher, reason)
                )
            }

            if (sendPunishmentToPunishmentLog && punishmentLogChannelId != null && punishmentMessageForType != null) {
                if (canTalkInPunishmentLogChannel == null) {
                    val cachedLorittaPermissions = loritta.cache.getLazyCachedLorittaPermissions(guild.id, punishmentLogChannelId)
                    canTalkInPunishmentLogChannel = cachedLorittaPermissions.canTalk()
                }

                if (canTalkInPunishmentLogChannel == true) {
                    val punishAction = when (PunishmentAction.BAN) {
                        PunishmentAction.BAN -> i18nContext.get(I18nKeysData.Commands.Command.Ban.PunishAction)
                        else -> TODO()
                    }

                    // Okay, do we have permission to send a message there?
                    val message = MessageUtils.createMessage(
                        loritta,
                        guild.id,
                        punishmentMessageForType,
                        listOf(
                            UserTokenSource(loritta.kord, user.data, memberData),
                            StaffTokenSource(loritta.kord, punisher.data, null) // TODO: Punisher Member Data
                        ),
                        mapOf(
                            Placeholders.PUNISHMENT_DURATION to i18nContext.get(I18nKeysData.Commands.Category.Moderation.Forever),
                            Placeholders.PUNISHMENT_REASON to reason,
                            Placeholders.PUNISHMENT_REASON_SHORT to reason,

                            Placeholders.PUNISHMENT_TYPE to punishAction,
                            Placeholders.PUNISHMENT_TYPE_SHORT to punishAction,
                        )
                    )

                    // Yay, we do have permission! Then let's create a message there...
                    loritta.rest.channel.createMessage(
                        punishmentLogChannelId,
                    ) {
                        message.apply(this)
                    }
                }
            }

            guild.ban(
                user.id
            ) {
                this.reason = i18nContext.get(I18nKeysData.Commands.Category.Moderation.AuditLogPunishmentLog(punisher.tag, reason))
                    .shortenWithEllipsis(512) // Max audit log entry size
            }
        }
    }

    private fun createDirectMessagePunishmentMessage(
        i18nContext: I18nContext,
        guild: Guild,
        punisher: User,
        reason: String
    ): UserMessageCreateBuilder.() -> (Unit) = {
        embed {
            author(punisher.tag, null, punisher.effectiveAvatar.url)
            title = "\uD83D\uDEAB ${i18nContext.get(I18nKeysData.Commands.Command.Ban.YouGotPunished(guild.name))}"
            field(i18nContext.get(I18nKeysData.Commands.Category.Moderation.PunishedBy), "${punisher.username}#${punisher.discriminator}", false)
            field(i18nContext.get(I18nKeysData.Commands.Category.Moderation.Reason), reason.shortenWithEllipsis(1024), false)
            color = Color(221, 0, 0)
            timestamp = Clock.System.now()
        }
    }

    suspend fun canInteract(guild: Guild, issuers: List<User>, targets: List<User>): Map<User, List<InteractionCheck>> {
        val interactionChecks = mutableMapOf<User, List<InteractionCheck>>()

        val ownerId = guild.ownerId

        for (target in targets) {
            val targetInteractionChecks = mutableListOf<InteractionCheck>()

            for (issuer in issuers) {
                if (issuer.id == target.id) {
                    // Haha, so funny...
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.TRYING_TO_INTERACT_WITH_SELF
                        )
                    )
                    continue
                }

                if (target.id == ownerId) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.TARGET_IS_OWNER
                        )
                    )
                    continue
                }

                // They are the owner, so they can do anything haha
                if (issuer.id == ownerId) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.SUCCESS
                        )
                    )
                    continue
                }

                // If the target is null, then it means that they aren't in the server and anything that we do against them should succeed
                val targetAsMember = target as? Member ?: target.fetchMemberOrNull(guild.id)
                if (targetAsMember == null) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.SUCCESS
                        )
                    )
                    continue
                }

                // If the issuer is null, then we have bigger problems lmao
                val issuerAsMember = issuer as? Member ?: issuer.fetchMember(guild.id)

                // Using "member.roles" is expensive because we would query the role list (which is a Get Guild query) every single time
                // However we already have the guild object... there! So let's get the roles from there!
                val issuerRoles = guild.roles.filter { it.id in issuerAsMember.roleIds }
                    .toList()
                    .sortedByDescending { it.rawPosition }
                val targetRoles = guild.roles.filter { it.id in targetAsMember.roleIds }
                    .toList()
                    .sortedByDescending { it.rawPosition }

                val firstIssuerRole = issuerRoles.firstOrNull()
                val firstTargetRole = targetRoles.firstOrNull()

                val firstIssuerRoleRawPosition = firstIssuerRole?.rawPosition ?: Int.MIN_VALUE
                val firstTargetRoleRawPosition = firstTargetRole?.rawPosition ?: Int.MIN_VALUE

                // The issuer raw position must be higher than the target raw position
                if (firstTargetRoleRawPosition >= firstIssuerRoleRawPosition) {
                    targetInteractionChecks.add(
                        InteractionCheck(
                            issuer,
                            target,
                            InteractionCheckResult.TARGET_ROLE_POSITION_HIGHER_OR_EQUAL_TO_ISSUER
                        )
                    )
                    continue
                }

                // Okay, everything is correct, so we can interact with the user!
                targetInteractionChecks.add(
                    InteractionCheck(
                        issuer,
                        target,
                        InteractionCheckResult.SUCCESS
                    )
                )
            }

            interactionChecks[target] = targetInteractionChecks
        }

        return interactionChecks
    }

    suspend fun checkAndRetrieveAllValidUsersFromString(context: ApplicationCommandContext, usersAsString: String): List<UserQueryResult> {
        val users = retrieveAllValidUsersFromString(context, usersAsString)

        if (users.isEmpty())
            context.failEphemerally {
                content = "No users found!"
            }

        return users
    }

    suspend fun retrieveAllValidUsersFromString(context: ApplicationCommandContext, usersAsString: String): List<UserQueryResult> {
        val users = mutableListOf<UserQueryResult>()

        // First, we will get all the mentioned users in the usersAsString, as long as they are ResolvedObjects map
        USER_MENTION_REGEX.findAll(usersAsString)
            .mapNotNull { it.groupValues[1].toLongOrNull() }
            .map { Snowflake(it) }
            .mapNotNull {
                val user = context.interaKTionsContext.interactionData.resolved?.users?.get(it) ?: return@mapNotNull null
                val member = context.interaKTionsContext.interactionData.resolved?.members?.get(it)

                // We are sure that the member doesn't exist because it wasn't resolved, so we will indicate to the UserQueryResult
                // that we don't need to fetch the member!
                UserQueryResult(member ?: user, member == null)
            }
            .toCollection(users)

        // Now, we will get all the user IDs in the input
        usersAsString.replace(USER_MENTION_REGEX, " ")
            .split(" ")
            .asSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { it.toLongOrNull() }
            .map { Snowflake(it) }
            .toList() // We need to have a terminal operator here, because a sequence is not suspendable
            .mapNotNull {
                try {
                    context.loritta.kord.getUser(it)
                } catch (e: Exception) {
                    null
                } // Probably not a valid user
            }
            .map {
                UserQueryResult(it, false)
            }
            .toCollection(users)

        return users
    }

    class UserQueryResult(val user: User, val memberDoesNotExist: Boolean) {
        suspend fun queryMember(guildId: Snowflake) = if (user is Member)
            user
        else if (memberDoesNotExist)
            null
        else
            user.fetchMemberOrNull(guildId)
    }

    data class InteractionCheck(
        val issuer: User,
        val target: User,
        val result: InteractionCheckResult
    )

    enum class InteractionCheckResult {
        SUCCESS,
        TARGET_IS_OWNER,
        TARGET_ROLE_POSITION_HIGHER_OR_EQUAL_TO_ISSUER,
        TRYING_TO_INTERACT_WITH_SELF
    }
}