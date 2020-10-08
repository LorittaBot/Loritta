package net.perfectdreams.loritta.plugin.htmlprovider

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin

class HtmlProviderPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        htmlProvider = JVMHtmlProvider()
    }
}