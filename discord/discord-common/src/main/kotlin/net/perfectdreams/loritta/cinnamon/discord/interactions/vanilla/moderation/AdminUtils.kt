package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation

import dev.kord.common.Color
import dev.kord.common.entity.Snowflake
import dev.kord.core.cache.data.GuildData
import dev.kord.core.cache.data.MemberData
import dev.kord.core.entity.Guild
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.embed
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import kotlinx.datetime.Clock
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation.ban.ConfirmBanData
import net.perfectdreams.loritta.cinnamon.discord.utils.effectiveAvatar

object AdminUtils {
    private val USER_MENTION_REGEX = Regex("<@!?(\\d+)>")

    suspend fun banUsers(loritta: LorittaCinnamon, confirmBanData: ConfirmBanData) {
        val guild = Guild(confirmBanData.guild, loritta.kord)

        val sendPunishmentViaDirectMessage = confirmBanData.sendPunishmentViaDirectMessage
        val sendPunishmentToPunishmentLog = confirmBanData.sendPunishmentToPunishmentLog
        val reason = confirmBanData.reason
        val users = confirmBanData.users
        val punisher = User(confirmBanData.punisher, loritta.kord)

        // TODO: Check if the server already has too many bans and, if it has, fallback to a "we will only ban after they join the server"
        // (Because in this case, Discord will reject the ban)
        for (userWithMemberData in users) {
            val userData = userWithMemberData.userData
            val memberData = userWithMemberData.memberData

            val user = User(userData, loritta.kord)
            val member = memberData?.let { Member(it, userData, loritta.kord) }

            try {
                // Don't try to send a direct message if the user is a bot
                // Also don't send a DM if the member isn't in the server, this avoids users using Loritta to send DM spam to users by
                // using the punishment feature
                if (sendPunishmentViaDirectMessage && member != null && !user.isBot) {
                    loritta.sendMessageToUserViaDirectMessage(
                        user.id,
                        createDirectMessagePunishmentMessage(guild, punisher, reason)
                    )
                }
            } catch (e: Exception) {
                // DMs can fail
                e.printStackTrace()
            }

            // TODO: The reason should include who banned
            /* loritta.rest.guild.addGuildBan(
                context.guildId,
                user.id
            ) {
                this.reason = reason
            } */
        }
    }

    fun createDirectMessagePunishmentMessage(guild: Guild, punisher: User, reason: String): UserMessageCreateBuilder.() -> (Unit) = {
        embed {
            author("${punisher.username}#${punisher.discriminator}", null, punisher.effectiveAvatar.url)
            title = "\uD83D\uDEAB Você foi banido de ${guild.name}!"
            field("Punido por", "${punisher.username}#${punisher.discriminator}", false)
            field("Motivo", reason, false)
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

                println(firstIssuerRole)
                println(firstTargetRole)

                println("Target Role raw position: $firstTargetRoleRawPosition")
                println("Issuer Role raw position: $firstIssuerRoleRawPosition")
                // The issuer raw position must be higher than the target raw position
                if (firstTargetRoleRawPosition > firstIssuerRoleRawPosition) {
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
            .filter { it.isNotBlank() }
            .mapNotNull { it.toLongOrNull() }
            .map { Snowflake(it) }
            .forEach {
                // TODO: Manually parse the user IDs
            }

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