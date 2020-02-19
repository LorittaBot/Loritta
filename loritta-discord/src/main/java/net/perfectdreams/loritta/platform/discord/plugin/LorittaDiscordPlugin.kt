package net.perfectdreams.loritta.platform.discord.plugin

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute

open class LorittaDiscordPlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	val lorittaDiscord = loritta as LorittaDiscord
	val routes = mutableListOf<BaseRoute>()
}