package net.perfectdreams.loritta.legacy.tables

import net.perfectdreams.loritta.cinnamon.pudding.tables.Backgrounds
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyShopItems : LongIdTable() {
	val shop = reference("shop", DailyShops)
	val item = reference("background", Backgrounds)
	val tag = text("tag").nullable()
}