package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class GetBundlesRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/economy/bundles/{bundleType}") {
	override suspend fun onRequest(call: ApplicationCall) {
		val sonhosBundles = transaction(Databases.loritta) {
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