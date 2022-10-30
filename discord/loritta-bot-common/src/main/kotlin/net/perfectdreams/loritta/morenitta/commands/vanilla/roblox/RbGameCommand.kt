package net.perfectdreams.loritta.morenitta.commands.vanilla.roblox

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.morenitta.utils.OutdatedCommandUtils

class RbGameCommand(loritta: LorittaBot) : DiscordAbstractCommandBase(
    loritta,
    listOf("rbgame", "rbjogo", "rbgameinfo"),
    net.perfectdreams.loritta.common.commands.CommandCategory.ROBLOX
) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.rbgame"
    }

    override fun command() = create {
        localizedDescription("$LOCALE_PREFIX.description")

        executesDiscord {
            val context = this

            if (context.args.isNotEmpty()) {
                OutdatedCommandUtils.sendOutdatedCommandMessage(
                    this,
                    locale,
                    "roblox game",
                    true
                )
            } else {
                context.explain()
            }
        }
    }
}