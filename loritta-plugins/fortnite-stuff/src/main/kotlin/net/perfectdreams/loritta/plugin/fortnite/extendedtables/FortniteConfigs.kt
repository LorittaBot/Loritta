package net.perfectdreams.loritta.plugin.fortnite.extendedtables

import org.jetbrains.exposed.dao.LongIdTable

object FortniteConfigs : LongIdTable() {
	val advertiseNewItems = bool("advertise_new_items").default(false)
	val channelToAdvertiseNewItems = long("channel").nullable()
	val itemAdvertisementMessage = text("item_advertisement").nullable()
}