package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.dao.ProfileDesign
import io.ktor.application.ApplicationCall
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.ProfileDesigns
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetAvailableProfileDesignsRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/profile-designs") {
	override suspend fun onRequest(call: ApplicationCall) {
		val array = loritta.newSuspendedTransaction {
			ProfileDesign.find {
				ProfileDesigns.enabled eq true
			}.toList()
		}.map { WebsiteUtils.toSerializable(it) }
				.let {
					Json.toJson(net.perfectdreams.loritta.serializable.ProfileDesign.serializer().list, it)
				}

		call.respondJson(array)
	}
}