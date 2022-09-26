package net.perfectdreams.loritta.legacy.website.routes.api.v1.economy

import io.ktor.server.application.*
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.data.Background
import net.perfectdreams.loritta.cinnamon.pudding.data.BackgroundWithVariations
import net.perfectdreams.loritta.cinnamon.pudding.services.fromRow
import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import net.perfectdreams.loritta.cinnamon.pudding.tables.ProfileDesigns
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.serializable.DailyShopBackgroundEntry
import net.perfectdreams.loritta.legacy.serializable.DailyShopResult
import net.perfectdreams.loritta.legacy.tables.DailyProfileShopItems
import net.perfectdreams.loritta.legacy.tables.DailyShopItems
import net.perfectdreams.loritta.legacy.tables.DailyShops
import net.perfectdreams.loritta.legacy.website.utils.WebsiteUtils
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondJson
import net.perfectdreams.sequins.ktor.BaseRoute
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

		val backgroundsInShopResults = loritta.newSuspendedTransaction {
			(DailyShopItems innerJoin Backgrounds)
				.select {
					DailyShopItems.shop eq shop[DailyShops.id]
				}
				.toList()
		}

		val backgroundsInShop = backgroundsInShopResults.map {
			DailyShopBackgroundEntry(
				BackgroundWithVariations(
					Background.fromRow(it),
					loritta.pudding.backgrounds.getBackgroundVariations(it[Backgrounds.internalName])
				),
				it[DailyShopItems.tag]
			)
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
			loritta.dreamStorageService.baseUrl,
			loritta.dreamStorageService.getCachedNamespaceOrRetrieve(),
			backgroundsInShop,
			profileDesignsInShop,
			generatedAt ?: -1L
		)

		call.respondJson(Json.encodeToString(DailyShopResult.serializer(), shopPayload))
	}
}