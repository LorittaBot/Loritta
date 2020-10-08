package net.perfectdreams.loritta.plugin.akilori

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.akilori.commands.AkinatorCommand

class AkiLoriPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        loritta as LorittaDiscord
        registerCommand(AkinatorCommand.create(loritta))
    }
}