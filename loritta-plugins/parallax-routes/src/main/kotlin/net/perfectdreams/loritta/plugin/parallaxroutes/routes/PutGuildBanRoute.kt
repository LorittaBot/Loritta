package net.perfectdreams.loritta.plugin.parallaxroutes.routes

import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute

class PutGuildBanRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/parallax/guilds/{guildId}/bans/{userId}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		/* println("Received auth")
		val guildId = call.parameters["guildId"] ?: return
		println("#1 $guildId")
		val userId = call.parameters["userId"] ?: return
		println("#2 $userId")
		val guild = lorittaShards.getGuildById(guildId) ?: return
		println("#3 $guild")

		val options = JsonParser.parseString(call.receiveText()).obj
		val punisher = lorittaShards.getUserById(options["punisher"].long)!!
		val user = lorittaShards.getUserById(userId)!!

		val serverConfig = com.mrpowergamerbr.loritta.utils.loritta.getServerConfigForGuild(guild.id)
		BanCommand.ban(
				serverConfig,
				guild,
				punisher,
				com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById(serverConfig.localeId),
				user,
				options["reason"].nullString ?: "",
				options["isSilent"].nullBool ?: false,
				options["days"].nullInt ?: 0
		) */
	}
}