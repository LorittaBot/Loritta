package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.set
import com.google.gson.JsonArray
import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.DailyShopItems
import net.perfectdreams.loritta.tables.DailyShops
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class GetDailyShopRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/economy/daily-shop") {
	override suspend fun onRequest(call: ApplicationCall) {
		val array = JsonArray()
		val shopPayload = jsonObject()

		var generatedAt: Long? = null

		val backgrounds = transaction(Databases.loritta) {
			val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

			generatedAt = shop[DailyShops.generatedAt]

			(DailyShopItems innerJoin Backgrounds)
					.select {
						DailyShopItems.shop eq shop[DailyShops.id]
					}.toList()
		}

		for (background in backgrounds) {
			array.add(
					WebsiteUtils.fromBackgroundToJson(background)
							.also { it["tag"] = background[DailyShopItems.tag] }
			)
		}

		shopPayload["backgrounds"] = array
		shopPayload["generatedAt"] = generatedAt

		call.respondJson(shopPayload)
	}
}