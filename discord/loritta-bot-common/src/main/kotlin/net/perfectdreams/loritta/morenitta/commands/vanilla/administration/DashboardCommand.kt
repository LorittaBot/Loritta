package net.perfectdreams.loritta.morenitta.commands.vanilla.administration

import net.perfectdreams.loritta.morenitta.utils.LorittaPermission
import net.dv8tion.jda.api.Permission
import net.perfectdreams.loritta.common.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.arguments
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.CommandContextCompat
import net.perfectdreams.loritta.morenitta.interactions.vanilla.moderation.DashboardCommand
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class DashboardCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(loritta, listOf("dashboard", "painel", "configurar", "config"), net.perfectdreams.loritta.common.commands.CommandCategory.MODERATION) {
    override fun command() = create {
        localizedDescription("commands.command.dashboard.description")
        localizedExamples("commands.command.dashboard.examples")

        arguments {
            argument(ArgumentType.TEXT) {
                optional = true
            }
        }

        executesDiscord {
            OutdatedCommandUtils.sendOutdatedCommandMessage(this, locale, "dashboard")

            DashboardCommand.executeCompat(CommandContextCompat.LegacyDiscordCommandContextCompat(this))
        }
    }
}
