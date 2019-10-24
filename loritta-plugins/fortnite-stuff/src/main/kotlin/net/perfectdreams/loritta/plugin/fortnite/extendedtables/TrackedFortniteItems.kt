package net.perfectdreams.loritta.plugin.fortnite.extendedtables

import com.mrpowergamerbr.loritta.tables.Profiles
import org.jetbrains.exposed.dao.LongIdTable

object TrackedFortniteItems : LongIdTable() {
	val trackedBy = reference("tracked_by", Profiles)
	val itemId = text("item_id")
}