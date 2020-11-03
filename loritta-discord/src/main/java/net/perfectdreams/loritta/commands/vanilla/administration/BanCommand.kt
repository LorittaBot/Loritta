package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.getLorittaProfile
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.getBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.PunishmentAction
import net.perfectdreams.loritta.utils.commands.*

class BanCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("ban", "banir", "hackban", "forceban"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.ban.description")

        userRequiredPermissions = listOf(Permission.BAN_MEMBERS)
        botRequiredPermissions = listOf(Permission.BAN_MEMBERS)


        examples {
            + "159985870458322944"
            + "159985870458322944 ${it["commands.moderation.ban.randomReason"]}"
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            val (users, rawReason) = checkAndRetrieveAllValidUsersFromMessages() ?: return@executesDiscord
            val members = mapToMemberFollowingPunishmentRequirements(users)

            if (members.isEmpty()) return@executesDiscord

            val (reason, skipConfirmation, silent, delDays) = parsePunishmentModifiers(rawReason)
            val settings = retrieveModerationInfo(serverConfig)

            val action = createPunishmentAction { message: Message?, silent: Boolean ->
                for (user in users) {
                    val userLocale = user.getLorittaProfile()?.getBaseLocale(loritta as Loritta, locale) ?: guildLocale

                    applyPunishment(settings, guild, user, guildLocale, userLocale, user, reason, silent, delDays)
                }
                message?.delete()?.queue()

                sendSuccessfullyPunishedMessage(reason, delDays == 0)
            }

            if (skipConfirmation) {
                return@executesDiscord action(null, silent)
            }

            val hasSilent = settings.sendPunishmentViaDm || settings.sendPunishmentToPunishLog
            val message = sendConfirmationMessage(users, hasSilent, "ban")

            handlePunishmentConfirmation(message, action)
        }
    }

    private fun applyPunishment(settings: ModerationConfigSettings, guild: Guild, punisher: User, serverLocale: BaseLocale, userProfile: BaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
        if (!isSilent) {
            handleNonSilentPunishmentStuff(settings, guild, punisher, serverLocale, userProfile, user, reason)
        }

        guild.ban(user, delDays, generateAuditLogMessage(serverLocale, punisher, reason)).queue()
    }

    private fun handleNonSilentPunishmentStuff(settings: ModerationConfigSettings, guild: Guild, punisher: User, serverLocale: BaseLocale, userProfile: BaseLocale, user: User, reason: String) {
        if (settings.sendPunishmentViaDm && guild.isMember(user)) {
            runCatching {
                val embed = createPunishmentMessageSentViaDirectMessage(guild, userProfile, punisher, userProfile["commands.moderation.ban.punishAction"], reason)

                user.openPrivateChannel().queue {
                    it.sendMessage(embed).queue()
                }
            }.onFailure { it.printStackTrace() }
        }

        val punishLogMessage = getPunishmentForMessage(settings, guild, PunishmentAction.BAN)

        if (settings.sendPunishmentToPunishLog && settings.punishLogChannelId != null && punishLogMessage != null) {
            val textChannel = guild.getTextChannelById(settings.punishLogChannelId)

            if (textChannel != null && textChannel.canTalk()) {
                val message = MessageUtils.generateMessage(
                        punishLogMessage,
                        listOf(user, guild),
                        guild,
                        mutableMapOf(
                                "duration" to serverLocale["commands.moderation.mute.forever"]
                        ) + getStaffCustomTokens(punisher) + getPunishmentCustomTokens(serverLocale, reason, "commands.moderation.ban")
                )

                message?.let {
                    textChannel.sendMessage(it).queue()
                }
            }
        }
    }
}