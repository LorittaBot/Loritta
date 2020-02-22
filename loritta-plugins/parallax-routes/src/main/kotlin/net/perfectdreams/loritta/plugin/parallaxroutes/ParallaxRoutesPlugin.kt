package net.perfectdreams.loritta.plugin.parallaxroutes

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.parallaxroutes.routes.PostMessageRoute
import net.perfectdreams.loritta.plugin.parallaxroutes.routes.PutRoleToMemberRoute
import net.perfectdreams.loritta.plugin.parallaxroutes.routes.RemoveRoleFromMemberRoute

class ParallaxRoutesPlugin(name: String, loritta: LorittaBot) : LorittaDiscordPlugin(name, loritta) {
	override fun onEnable() {
		super.onEnable()

		routes.add(PostMessageRoute(lorittaDiscord))
		routes.add(PutRoleToMemberRoute(lorittaDiscord))
		routes.add(RemoveRoleFromMemberRoute(lorittaDiscord))
	}
}