package net.perfectdreams.loritta.plugin.parallaxroutes.routes

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.AdminUtils
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.ApplicationCall
import io.ktor.request.receiveText
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIAuthenticationRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class PutGuildBanRoute(loritta: LorittaDiscord) : RequiresAPIAuthenticationRoute(loritta, "/api/v1/parallax/guilds/{guildId}/bans/{userId}") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall) {
		loritta as Loritta

		val guildId = call.parameters["guildId"]!!
		val userId = call.parameters["userId"]!!
		val guild = lorittaShards.getGuildById(guildId)!!

		val options = JsonParser.parseString(call.receiveText()).obj
		val punisher = lorittaShards.retrieveUserById(options["punisher"].long)!!
		val user = lorittaShards.retrieveUserById(userId)!!

		val serverConfig = loritta.getOrCreateServerConfig(guildId.toLong(), true)
		val moderationInfo = AdminUtils.retrieveModerationInfo(serverConfig)

		BanCommand.ban(
				moderationInfo,
				guild,
				punisher,
				com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById(serverConfig.localeId),
				user,
				options["reason"].nullString ?: "",
				options["isSilent"].nullBool ?: false,
				options["days"].nullInt ?: 0
		)

		call.respondJson(jsonObject())
	}
}