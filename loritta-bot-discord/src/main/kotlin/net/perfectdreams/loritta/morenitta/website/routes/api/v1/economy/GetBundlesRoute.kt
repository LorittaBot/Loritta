package net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.selectAll

class GetBundlesRoute(val loritta: LorittaBot) : BaseRoute("/api/v1/economy/bundles/{bundleType}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val sonhosBundles = loritta.newSuspendedTransaction {
			SonhosBundles.selectAll()
					.toList()
		}

		call.respondJson(
				sonhosBundles.map {
					jsonObject(
							"id" to it[SonhosBundles.id].value,
							"active" to it[SonhosBundles.active],
							"price" to it[SonhosBundles.price],
							"sonhos" to it[SonhosBundles.sonhos]
					)
				}.toJsonArray()
		)
	}
}