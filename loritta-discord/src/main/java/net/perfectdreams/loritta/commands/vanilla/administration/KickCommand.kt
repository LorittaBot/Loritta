package net.perfectdreams.loritta.commands.vanilla.administration

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.utils.getLorittaProfile
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.getBaseLocale
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.api.commands.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.PunishmentAction
import net.perfectdreams.loritta.utils.commands.*

class KickCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("kick", "expulsar", "kickar"), CommandCategory.ADMIN) {

    override fun command(): Command<CommandContext> = create {
        localizedDescription("commands.moderation.kick.description")

        userRequiredPermissions = listOf(Permission.KICK_MEMBERS)
        botRequiredPermissions = listOf(Permission.KICK_MEMBERS)

        examples {
            + "159985870458322944"
            + "159985870458322944 ${it["commands.moderation.ban.randomReason"]}"
        }

        usage {
            argument(ArgumentType.USER) {
                optional = false
            }
            argument(ArgumentType.TEXT) {
                optional = true
            }
        }

        executesDiscord {
            if (args.isEmpty()) return@executesDiscord explain()

            val data = parsePunishmentStatementData()
            val settings = retrieveModerationInfo(serverConfig)

            val punishment = createStandardLazyPunishment(Companion, settings, data)

            handleLazyPunishment(settings, punishment, data)
        }
    }

    companion object: PunishmentHandler {

        override fun applyPunishment(settings: ModerationConfigSettings, guild: Guild, punisher: User, guildLocale: BaseLocale, userLocale: BaseLocale, user: User, reason: String, isSilent: Boolean, delDays: Int) {
            val member = guild.getMember(user) ?: return
            if (!isSilent) {
                settings.handleNonSilentPunishment(PunishmentAction.KICK, guild, punisher, guildLocale, userLocale, user, reason)
            }

            guild.kick(member, generateAuditLogMessage(guildLocale, punisher, reason)).queue()
        }
    }

}