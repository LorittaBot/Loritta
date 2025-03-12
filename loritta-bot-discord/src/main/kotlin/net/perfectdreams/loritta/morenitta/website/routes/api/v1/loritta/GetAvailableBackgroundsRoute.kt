package net.perfectdreams.loritta.morenitta.website.routes.api.v1.loritta

import io.ktor.server.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.Background
import net.perfectdreams.loritta.serializable.BackgroundListResponse
import net.perfectdreams.loritta.serializable.BackgroundWithVariations
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.selectAll

class GetAvailableBackgroundsRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/loritta/backgrounds") {
	override suspend fun onRequest(call: ApplicationCall) {
		val response = loritta.newSuspendedTransaction {
			Backgrounds.selectAll().where {
				Backgrounds.enabled eq true
			}.toList()
		}.map {
			BackgroundWithVariations(
				Background.fromRow(it),
				loritta.pudding.backgrounds.getBackgroundVariations(it[Backgrounds.internalName])
			)
		}
			.let {
				Json.encodeToString(
					BackgroundListResponse(
						loritta.dreamStorageService.baseUrl,
						loritta.dreamStorageService.getCachedNamespaceOrRetrieve(),
						loritta.config.loritta.etherealGambiService.url,
						it
					)
				)
			}

		call.respondJson(response)
	}
}