package net.perfectdreams.loritta.plugin.htmlprovider

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.htmlprovider.JVMHtmlProvider

class HtmlProviderPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
    companion object {
        val assetHashProvider = JVMAssetHashProvider()
    }

    override fun onEnable() {
        this.htmlProvider = JVMHtmlProvider()
    }
}