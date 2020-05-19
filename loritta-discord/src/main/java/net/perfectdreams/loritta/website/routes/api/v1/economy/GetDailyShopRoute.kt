package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.mrpowergamerbr.loritta.network.Databases
import io.ktor.application.ApplicationCall
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.datawrapper.Background
import net.perfectdreams.loritta.datawrapper.DailyShopResult
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
		val backgroundsInShop = mutableListOf<Background>()

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
			backgroundsInShop.add(
					WebsiteUtils.fromBackgroundToJson(background)
							.also { it.tag = background[DailyShopItems.tag] }
			)
		}

		val shopPayload = DailyShopResult(
				backgroundsInShop,
				generatedAt ?: -1L
		)

		call.respondJson(Json.stringify(DailyShopResult.serializer(), shopPayload))
	}
}