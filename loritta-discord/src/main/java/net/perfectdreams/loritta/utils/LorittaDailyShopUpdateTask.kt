package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.network.Databases
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.DailyShopItems
import net.perfectdreams.loritta.tables.DailyShops
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaDailyShopUpdateTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}
		// How many new items should be shown in the shop on every shop rotation?
		// This exists to avoid Lori always selecting previously sold items instead of selecting never seen before items
		private const val NEW_ITEMS_TARGET = 2
		private const val DAILY_BACKGROUNDS_TARGET = 10

		fun generate() {
			logger.info { "Generating a new daily shop..." }
			transaction(Databases.loritta) {
				val newShop = DailyShops.insertAndGetId {
					it[generatedAt] = System.currentTimeMillis()
				}

				val allBackgrounds = Backgrounds.select {
					Backgrounds.enabled eq true and (Backgrounds.availableToBuyViaDreams eq true)
				}.toMutableList()

				val selectedBackgrounds = mutableListOf<ResultRow>()

				// We will try to at least have two new items every single day, to avoid showing already sold backgrounds every day
				val neverSoldBeforeBackgrounds = allBackgrounds.filter {
					DailyShopItems.select {
						DailyShopItems.item eq it[Backgrounds.id]
					}.count() == 0L
				}.toMutableList()

				repeat(Math.min(NEW_ITEMS_TARGET, neverSoldBeforeBackgrounds.size)) {
					if (neverSoldBeforeBackgrounds.isNotEmpty()) { // Because we repeat multiple times and remove the background from the list, we need to check if the list is empty inside the repeat
						val randomBackground = neverSoldBeforeBackgrounds.random()
						// We need to do this because the ResultRow isn't the same instance
						// TODO: Check if this is really true, I'm 99% sure it is, but...
						allBackgrounds.removeIf { it[Backgrounds.internalName] == randomBackground[Backgrounds.internalName] }
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
						it[shop] = newShop
						it[item] = background[Backgrounds.id]
						it[tag] = if (DailyShopItems.select { item eq background[Backgrounds.id] }.count() == 0L) { "website.dailyShop.new" } else null
					}
				}
			}
		}
	}

	override fun run() {
		logger.info { "Automatically updating the daily shop!" }
		try {
			generate()
			logger.info { "Successfully updated the daily shop!" }
		} catch (e: Exception) {
			logger.warn(e) { "Something went wrong while updating the daily shop..." }
		}
	}
}