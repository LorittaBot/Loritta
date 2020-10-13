package net.perfectdreams.loritta.plugin.akilori

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.akilori.commands.AkinatorCommand

class AkiLoriPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        registerCommand(AkinatorCommand(this))
    }
}