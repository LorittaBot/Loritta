package net.perfectdreams.loritta.website.routes.api.v1.loritta

import com.mrpowergamerbr.loritta.dao.Background
import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.application.ApplicationCall
import kotlinx.serialization.builtins.list
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.transactions.transaction

class GetAvailableBackgroundsRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/loritta/backgrounds") {
	override suspend fun onRequest(call: ApplicationCall) {
		val array = transaction(Databases.loritta) {
			Background.find {
				Backgrounds.enabled eq true
			}.toList()
		}.map { WebsiteUtils.toSerializable(it) }
				.let {
					Json.toJson(net.perfectdreams.loritta.datawrapper.Background.serializer().list, it)
				}

		call.respondJson(array)
	}
}