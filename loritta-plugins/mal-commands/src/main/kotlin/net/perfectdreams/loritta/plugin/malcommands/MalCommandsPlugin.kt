package net.perfectdreams.loritta.plugin.malcommands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.malcommands.commands.MalAnimeCommand

class MalCommandsPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        loritta as LorittaDiscord
        registerCommand(MalAnimeCommand.command(loritta, this))
    }
}