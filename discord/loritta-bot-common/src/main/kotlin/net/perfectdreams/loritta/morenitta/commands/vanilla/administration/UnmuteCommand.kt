package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.tables.Mutes
import net.perfectdreams.loritta.morenitta.utils.MessageUtils
import net.perfectdreams.loritta.morenitta.utils.onReactionAddByAuthor
import kotlinx.coroutines.runBlocking
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.entities.User
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.PunishmentAction
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import net.perfectdreams.loritta.morenitta.LorittaBot

class UnmuteCommand(loritta: LorittaBot) : AbstractCommand(
    loritta,
    "unmute",
    listOf("desmutar", "desilenciar", "desilenciar"),
    net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION
) {
    override fun getDescriptionKey() = LocaleKeyData("commands.command.unmute.description")
    override fun getExamplesKey() = LocaleKeyData("commands.command.unmute.examples")
    override fun getUsage() = AdminUtils.PUNISHMENT_USAGES

    override fun getDiscordPermissions(): List<Permission> {
        return listOf(Permission.KickMembers)
    }

    override fun canUseInPrivateChannel(): Boolean {
        return false
    }

    override fun getBotPermissions(): List<Permission> {
        return listOf(Permission.ManageRoles, Permission.ManageRoles)
    }

    override suspend fun run(context: CommandContext, locale: BaseLocale) {
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
                    unmute(loritta, settings, context.guild, context.userHandle, locale, user, reason, isSilent)

                runCatching { message?.delete() }

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
                if (it.reactionEmote.isEmote("✅") || it.reactionEmote.isEmote("\uD83D\uDE4A")) {
                    banCallback.invoke(message, it.reactionEmote.isEmote("\uD83D\uDE4A"))
                }
                return@onReactionAddByAuthor
            }

            runCatching { message.addReaction("✅") }
            if (hasSilent) {
                runCatching { message.addReaction("\uD83D\uDE4A") }
            }
        } else {
            this.explain(context)
        }
    }

    companion object {
        suspend fun unmute(
            loritta: LorittaBot,
            settings: AdminUtils.ModerationConfigSettings,
            guild: Guild,
            punisher: User,
            locale: BaseLocale,
            user: User,
            reason: String,
            isSilent: Boolean
        ) {
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
                    val textChannel = guild.getTextChannelById(settings.punishLogChannelId)

                    if (textChannel != null && textChannel.canTalk()) {
                        val message = MessageUtils.generateMessage(
                            punishLogMessage,
                            listOf(user, guild),
                            guild,
                            mutableMapOf(
                                "duration" to locale["commands.command.mute.forever"]
                            ) + AdminUtils.getStaffCustomTokens(punisher)
                                    + AdminUtils.getPunishmentCustomTokens(locale, reason, "commands.command.unmute")
                        )

                        message?.let {
                            runCatching { textChannel.sendMessage(it) }
                        }
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
            }

            // And now remove the "Muted" role if needed!
            val member = runBlocking { guild.retrieveMemberOrNull(user) }

            if (member != null) {
                val mutedRoles = MuteCommand.getMutedRole(loritta, guild, locale)
                if (mutedRoles != null)
                    runCatching { guild.removeRoleFromMember(member, mutedRoles) }
            }
        }
    }
}