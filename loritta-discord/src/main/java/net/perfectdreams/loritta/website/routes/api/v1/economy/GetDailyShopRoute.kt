package net.perfectdreams.loritta.website.routes.api.v1.economy

import io.ktor.application.*
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.serializable.DailyShopResult
import net.perfectdreams.loritta.tables.*
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll

class GetDailyShopRoute(val loritta: LorittaDiscord) : BaseRoute("/api/v1/economy/daily-shop") {
	override suspend fun onRequest(call: ApplicationCall) {
		val generatedAt: Long?

		val shop = loritta.newSuspendedTransaction {
			DailyShops.selectAll().orderBy(DailyShops.generatedAt, SortOrder.DESC).limit(1).first()
		}

		generatedAt = shop[DailyShops.generatedAt]

		val backgroundsInShop = loritta.newSuspendedTransaction {
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

		val profileDesignsInShop = loritta.newSuspendedTransaction {
			(DailyProfileShopItems innerJoin ProfileDesigns)
					.select {
						DailyProfileShopItems.shop eq shop[DailyShops.id]
					}
					.map {
						WebsiteUtils.fromProfileDesignToSerializable(it).also { profile ->
							profile.tag = it[DailyProfileShopItems.tag]
						}
					}
					.toList()
		}

		val shopPayload = DailyShopResult(
				backgroundsInShop,
				profileDesignsInShop,
				generatedAt ?: -1L
		)

		call.respondJson(Json.encodeToString(DailyShopResult.serializer(), shopPayload))
	}
}