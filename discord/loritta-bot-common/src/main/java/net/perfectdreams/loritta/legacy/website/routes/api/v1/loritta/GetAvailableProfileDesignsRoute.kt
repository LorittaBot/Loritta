package net.perfectdreams.loritta.legacy.website.routes.api.v1.loritta

import net.perfectdreams.loritta.legacy.dao.ProfileDesign
import io.ktor.server.application.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.legacy.website.utils.WebsiteUtils
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson

class GetAvailableProfileDesignsRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/profile-designs") {
	override suspend fun onRequest(call: ApplicationCall) {
		val array = loritta.newSuspendedTransaction {
			ProfileDesign.find {
				ProfileDesigns.enabled eq true
			}.toList()
		}.map { WebsiteUtils.toSerializable(it) }
				.let {
					Json.encodeToJsonElement(ListSerializer(net.perfectdreams.loritta.legacy.serializable.ProfileDesign.serializer()), it)
				}

		call.respondJson(array)
	}
}