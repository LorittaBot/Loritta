package net.perfectdreams.loritta.plugin.malcommands

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.malcommands.commands.MalAnimeCommand

class MalCommandsPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        registerCommand(MalAnimeCommand(this))
    }
}