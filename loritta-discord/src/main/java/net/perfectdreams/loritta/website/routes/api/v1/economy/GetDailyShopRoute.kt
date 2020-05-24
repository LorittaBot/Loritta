package net.perfectdreams.loritta.website.routes.api.v1.economy

import io.ktor.application.ApplicationCall
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.serializable.DailyShopResult
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.DailyShopItems
import net.perfectdreams.loritta.tables.DailyShops
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class GetDailyShopRoute(loritta: LorittaDiscord) : BaseRoute(loritta, "/api/v1/economy/daily-shop") {
	override suspend fun onRequest(call: ApplicationCall) {
		var generatedAt: Long? = null

		val backgroundsInShop = loritta.newSuspendedTransaction {
			val shop = DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()

			generatedAt = shop[DailyShops.generatedAt]

			(DailyShopItems innerJoin Backgrounds)
					.select {
						DailyShopItems.shop eq shop[DailyShops.id]
					}
					.map {
						WebsiteUtils.fromBackgroundToSerializable(it).also { background ->
							background.tag = it[DailyShopItems.tag]
						}
					}
					.toList()
		}

		val shopPayload = DailyShopResult(
				backgroundsInShop,
				generatedAt ?: -1L
		)

		call.respondJson(Json.stringify(DailyShopResult.serializer(), shopPayload))
	}
}