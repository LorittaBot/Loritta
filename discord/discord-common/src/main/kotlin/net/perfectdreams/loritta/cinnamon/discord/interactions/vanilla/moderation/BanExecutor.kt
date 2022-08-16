package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation

import com.sun.rowset.CachedRowSetImpl
import dev.kord.common.entity.Permission
import dev.kord.core.behavior.channel.createMessage
import dev.kord.core.cache.data.GuildData
import dev.kord.core.entity.Guild
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.toList
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.discordinteraktions.common.commands.options.SlashCommandArguments
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CinnamonSlashCommandExecutor
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.GuildApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.options.LocalizedApplicationCommandOptions
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.utils.PuddingExperimental
import net.perfectdreams.loritta.cinnamon.utils.TodoFixThisData
import javax.sql.rowset.CachedRowSet

class BanExecutor(loritta: LorittaCinnamon) : CinnamonSlashCommandExecutor(loritta) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    inner class Options : LocalizedApplicationCommandOptions(loritta) {
        // May be multiple in the same string
        val users = string("users", TodoFixThisData)

        // TODO: Pre-defined reasons with autocomplete
        val reason = string("reason", TodoFixThisData)

        // TODO: Delete days
        val sendToDirectMessage = optionalBoolean("send_via_direct_message", TodoFixThisData)
        val sendToPunishmentLog = optionalBoolean("send_to_punishment_log", TodoFixThisData)
    }

    override val options = Options()

    @OptIn(PuddingExperimental::class)
    override suspend fun execute(context: ApplicationCommandContext, args: SlashCommandArguments) {
        if (context !is GuildApplicationCommandContext)
            return

        context.deferChannelMessageEphemerally()

        if (Permission.BanMembers !in context.interaKTionsContext.appPermissions)
            context.failEphemerally {
                content = "Eu não tenho permissão para banir membros!"
            }

        val users = AdminUtils.checkAndRetrieveAllValidUsersFromString(context, args[options.users])
        val reason = args[options.reason]

        // TODO: Check if the user can interact with the user (banning them)
        // To do this, we need to query the guild information

        // Technically the "guild" shouldn't be null here, ever
        val guild = Guild(GuildData.from(rest.guild.getGuild(context.guildId)), loritta.kord)

        val interactResults = AdminUtils.canInteract(
            guild,
            listOf(
                loritta.kord.getSelf(), // TODO: Cache getSelf somewhere
                context.member
            ),
            users.map { it.queryMember(guild.id) ?: it.user }
        )

        val interactableUsers = interactResults.filterValues { it.all { it.result == AdminUtils.InteractionCheckResult.SUCCESS } }
        if (false && interactableUsers.isEmpty()) {
            // Okay, so none of the users are interactable... now what?
            context.failEphemerally {
                content = "None of the users are interactable! ${interactResults.values.flatMap { it }.map { it.result } }"
            }
        }

        // TODO: Retrieve this servers' moderation settings

        // TODO: Ban confirmation reactions

        // TODO: Check if the server already has too many bans and, if it has, fallback to a "we will only ban after they join the server"
        // (Because in this case, Discord will reject the ban)
        for (user in users) {
            try {
                loritta.sendMessageToUserViaDirectMessage(
                    user.user.id,
                    AdminUtils.createDirectMessagePunishmentMessage(guild, context.user, reason)
                )
            } catch (e: Exception) {
                // DMs can fail
                e.printStackTrace()
            }

            /* loritta.rest.guild.addGuildBan(
                context.guildId,
                user.id
            ) {
                this.reason = reason
            } */
        }

        context.sendEphemeralMessage {
            content = "Usuários banidos!"
        }

        // TODO: If the interact fail list is not empty, tell the user why those users weren't banned
    }

    data class ModerationSettings(
        val sendPunishmentViaDm: Boolean,
        val sendToPunishmentLog: Boolean
    )
}