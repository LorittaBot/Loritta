package net.perfectdreams.loritta.plugin.parallaxroutes

import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.plugin.parallaxroutes.routes.*

class ParallaxRoutesPlugin(name: String, loritta: LorittaDiscord) : LorittaDiscordPlugin(name, loritta) {
	override fun onEnable() {
		super.onEnable()

		routes.add(PostMessageRoute(loritta))
		routes.add(PutRoleToMemberRoute(loritta))
		routes.add(DeleteRoleFromMemberRoute(loritta))
		routes.add(PutGuildBanRoute(loritta))
		routes.add(PutReactionToMessageRoute(loritta))
		routes.add(PutReactionActionToMessageRoute(loritta))
	}
}