package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.dao.Background
import io.ktor.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.serializable.BackgroundListResponse
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute

class GetAvailableBackgroundsRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/backgrounds") {
	override suspend fun onRequest(call: ApplicationCall) {
		val response = loritta.newSuspendedTransaction {
			Background.find {
				Backgrounds.enabled eq true
			}.toList()
		}.map { WebsiteUtils.toSerializable(it) }
			.let {
				Json.encodeToString(
					BackgroundListResponse(
						loritta.dreamStorageService.baseUrl,
						loritta.dreamStorageService.getCachedNamespaceOrRetrieve(),
						it
					)
				)
			}

		call.respondJson(response)
	}
}