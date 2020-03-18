package net.perfectdreams.loritta.plugin.lorittabirthday2020

import mu.KotlinLogging
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.lorittabirthday2020.routes.ChooseTeamRoute

class LorittaBirthday2020Event(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	override fun onEnable() {
		this.routes.add(ChooseTeamRoute(loritta as LorittaDiscord))
	}

	override fun onDisable() {
		super.onDisable()
	}

	companion object {
		private val logger = KotlinLogging.logger {}
	}
}