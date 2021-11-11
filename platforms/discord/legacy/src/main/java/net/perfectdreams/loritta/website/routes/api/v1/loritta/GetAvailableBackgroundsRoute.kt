package net.perfectdreams.loritta.website.routes.api.v1.loritta

import io.ktor.application.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundWithVariations
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.serializable.BackgroundListResponse
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
import org.jetbrains.exposed.sql.select

class GetAvailableBackgroundsRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/loritta/backgrounds") {
	override suspend fun onRequest(call: ApplicationCall) {
		val response = loritta.newSuspendedTransaction {
			Backgrounds.select {
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
						it
					)
				)
			}

		call.respondJson(response)
	}
}