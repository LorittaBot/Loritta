package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object DailyProfileShopItems : LongIdTable() {
	val shop = reference("shop", DailyShops)
	val item = reference("profile", ProfileDesigns)
	val tag = text("tag").nullable()
}