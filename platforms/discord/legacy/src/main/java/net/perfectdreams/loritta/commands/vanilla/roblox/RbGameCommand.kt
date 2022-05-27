package net.perfectdreams.loritta.commands.vanilla.roblox

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.OutdatedCommandUtils

class RbGameCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("rbgame", "rbjogo", "rbgameinfo"), CommandCategory.ROBLOX) {
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