package net.perfectdreams.loritta.plugin.htmlprovider

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin

class HtmlProviderPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
    override fun onEnable() {
        htmlProvider = JVMHtmlProvider()
    }
}