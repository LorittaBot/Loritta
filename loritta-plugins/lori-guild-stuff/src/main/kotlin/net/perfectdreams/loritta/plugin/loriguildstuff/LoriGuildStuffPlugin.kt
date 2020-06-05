package net.perfectdreams.loritta.plugin.loriguildstuff

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.loriguildstuff.commands.NotifyCommand

class LoriGuildStuffPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        loritta as LorittaDiscord
        registerCommand(NotifyCommand.create(loritta))
    }
}