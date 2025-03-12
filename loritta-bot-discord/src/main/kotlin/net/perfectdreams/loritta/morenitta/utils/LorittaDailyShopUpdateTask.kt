package net.perfectdreams.loritta.morenitta.utils

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.*
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.serializable.internal.requests.LorittaInternalRPCRequest
import net.perfectdreams.loritta.serializable.internal.responses.LorittaInternalRPCResponse
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.*

class LorittaDailyShopUpdateTask(val loritta: LorittaBot) : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		// How many new items should be shown in the shop on every shop rotation?
		// This exists to avoid Lori always selecting previously sold items instead of selecting never seen before items
		private const val NEW_ITEMS_TARGET = 2
		private const val DAILY_PROFILE_DESIGNS_TARGET = 4
		private const val DAILY_BACKGROUNDS_TARGET = 10

		fun generate(loritta: LorittaBot) {
			logger.info { "Generating a new daily shop..." }

			runBlocking {
				val resultId = loritta.pudding.transaction {
					val newShop = DailyShops.insertAndGetId {
						it[generatedAt] = System.currentTimeMillis()
					}

					getAndAddRandomBackgroundsToShop(newShop)
					getAndAddRandomProfileDesignsToShop(newShop)

					newShop
				}

				// Notify that it was refreshed
				val shards = loritta.config.loritta.clusters.instances

				val jobs = shards.map { cluster ->
					cluster to GlobalScope.async {
						withTimeout(25_000) {
							loritta.makeRPCRequest<LorittaInternalRPCResponse.DailyShopRefreshedResponse>(
								cluster,
								LorittaInternalRPCRequest.DailyShopRefreshedRequest(resultId.value)
							)
						}
					}
				}

				for (job in jobs) {
					try {
						job.second.await()
						logger.info { "Successfully notified Cluster ${job.first.id} (${job.first.name}) that the daily shop was refreshed!" }
					} catch (e: Exception) {
						logger.warn(e) { "Something went wrong when notifying Cluster ${job.first.id} (${job.first.name}) that the daily shop was refreshed..." }
					}
				}
			}
		}

		private fun getAndAddRandomBackgroundsToShop(shopId: EntityID<Long>) {
			val allBackgrounds = Backgrounds.selectAll().where {
				Backgrounds.enabled eq true and (Backgrounds.availableToBuyViaDreams eq true)
			}.toMutableList()

			val selectedBackgrounds = mutableListOf<ResultRow>()

			// We will try to at least have two new items every single day, to avoid showing already sold backgrounds every day
			val neverSoldBeforeBackgrounds = allBackgrounds.filter {
				DailyShopItems.selectAll().where {
					DailyShopItems.item eq it[Backgrounds.id]
				}.count() == 0L
			}.toMutableList()

			repeat(Math.min(NEW_ITEMS_TARGET, neverSoldBeforeBackgrounds.size)) {
				if (neverSoldBeforeBackgrounds.isNotEmpty()) { // Because we repeat multiple times and remove the background from the list, we need to check if the list is empty inside the repeat
					val randomBackground = neverSoldBeforeBackgrounds.random()

					allBackgrounds.remove(randomBackground)
					neverSoldBeforeBackgrounds.remove(randomBackground)
					selectedBackgrounds.add(randomBackground)
				}
			}

			repeat(DAILY_BACKGROUNDS_TARGET - selectedBackgrounds.size) {
				val randomBackground = allBackgrounds.random()
				allBackgrounds.remove(randomBackground)
				selectedBackgrounds.add(randomBackground)
			}

			for (background in selectedBackgrounds) {
				DailyShopItems.insert {
					it[shop] = shopId
					it[item] = background[Backgrounds.id]
					it[tag] = if (DailyShopItems.selectAll().where { item eq background[Backgrounds.id] }.count() == 0L) { "website.dailyShop.new" } else null
				}
			}
		}

		private fun getAndAddRandomProfileDesignsToShop(shopId: EntityID<Long>) {
			val allProfileDesigns = ProfileDesigns.selectAll().where {
				ProfileDesigns.enabled eq true and (ProfileDesigns.availableToBuyViaDreams eq true)
			}.toMutableList()

			val selectedProfileDesigns = mutableListOf<ResultRow>()

			// We will try to at least have two new items every single day, to avoid showing already sold backgrounds every day
			val neverSoldBeforeProfileDesigns = allProfileDesigns.filter {
				DailyProfileShopItems.selectAll().where {
					DailyProfileShopItems.item eq it[ProfileDesigns.id]
				}.count() == 0L
			}.toMutableList()

			repeat(Math.min(NEW_ITEMS_TARGET, neverSoldBeforeProfileDesigns.size)) {
				if (neverSoldBeforeProfileDesigns.isNotEmpty()) { // Because we repeat multiple times and remove the background from the list, we need to check if the list is empty inside the repeat
					val randomBackground = neverSoldBeforeProfileDesigns.random()

					allProfileDesigns.remove(randomBackground)
					neverSoldBeforeProfileDesigns.remove(randomBackground)
					selectedProfileDesigns.add(randomBackground)
				}
			}

			repeat(DAILY_PROFILE_DESIGNS_TARGET - selectedProfileDesigns.size) {
				val randomBackground = allProfileDesigns.random()
				allProfileDesigns.remove(randomBackground)
				selectedProfileDesigns.add(randomBackground)
			}

			for (background in selectedProfileDesigns) {
				DailyProfileShopItems.insert {
					it[shop] = shopId
					it[item] = background[ProfileDesigns.id]
					it[tag] = if (DailyProfileShopItems.selectAll().where { item eq background[ProfileDesigns.id] }.count() == 0L) { "website.dailyShop.new" } else null
				}
			}
		}
	}

	override fun run() {
		logger.info { "Automatically updating the daily shop!" }
		try {
			generate(loritta)
			logger.info { "Successfully updated the daily shop!" }
		} catch (e: Exception) {
			logger.warn(e) { "Something went wrong while updating the daily shop..." }
		}
	}
}