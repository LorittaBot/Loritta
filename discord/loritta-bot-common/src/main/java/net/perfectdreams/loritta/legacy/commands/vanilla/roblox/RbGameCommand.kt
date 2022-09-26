package net.perfectdreams.loritta.legacy.commands.vanilla.roblox

import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils

class RbGameCommand(loritta: LorittaDiscord): DiscordAbstractCommandBase(loritta, listOf("rbgame", "rbjogo", "rbgameinfo"), net.perfectdreams.loritta.common.commands.CommandCategory.ROBLOX) {
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