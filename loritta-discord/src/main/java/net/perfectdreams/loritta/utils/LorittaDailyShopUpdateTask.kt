package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.network.Databases
import mu.KotlinLogging
import net.perfectdreams.loritta.tables.Backgrounds
import net.perfectdreams.loritta.tables.DailyShopItems
import net.perfectdreams.loritta.tables.DailyShops
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class LorittaDailyShopUpdateTask : Runnable {
	companion object {
		private val logger = KotlinLogging.logger {}

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

				repeat(10) {
					val randomBackground = allBackgrounds.random()
					allBackgrounds.remove(randomBackground)
					selectedBackgrounds.add(randomBackground)
				}

				for (background in selectedBackgrounds) {
					DailyShopItems.insert {
						it[shop] = newShop
						it[item] = background[Backgrounds.id]
						it[tag] = if (DailyShopItems.select { item eq background[Backgrounds.id] }.count() == 0) { "website.dailyShop.new" } else null
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