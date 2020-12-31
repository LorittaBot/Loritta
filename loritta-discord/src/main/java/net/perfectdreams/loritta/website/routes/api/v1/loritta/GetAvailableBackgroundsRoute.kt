package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.dao.Background
import io.ktor.application.*
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson

class GetAvailableBackgroundsRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/backgrounds") {
	override suspend fun onRequest(call: ApplicationCall) {
		val array = loritta.newSuspendedTransaction {
			Background.find {
				Backgrounds.enabled eq true
			}.toList()
		}.map { WebsiteUtils.toSerializable(it) }
				.let {
					Json.encodeToJsonElement(ListSerializer(net.perfectdreams.loritta.serializable.Background.serializer()), it)
				}

		call.respondJson(array)
	}
}