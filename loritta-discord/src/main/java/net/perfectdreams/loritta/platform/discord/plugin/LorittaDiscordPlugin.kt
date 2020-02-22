package net.perfectdreams.loritta.platform.discord.plugin

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformer
import org.jetbrains.exposed.sql.Column

open class LorittaDiscordPlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	val lorittaDiscord = loritta as LorittaDiscord
	val routes = mutableListOf<BaseRoute>()
	val configTransformers = mutableListOf<ConfigTransformer>()
	val serverConfigColumns = mutableListOf<Column<out Any?>>()
}