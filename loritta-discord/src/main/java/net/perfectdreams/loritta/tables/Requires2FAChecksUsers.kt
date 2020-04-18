package net.perfectdreams.loritta.tables

import com.mrpowergamerbr.loritta.tables.Profiles
import org.jetbrains.exposed.dao.LongIdTable

object Requires2FAChecksUsers : LongIdTable() {
	val userId = reference("user", Profiles).index()
	val triggeredAt = long("triggered_at")
	val triggeredTransaction = reference("triggered_transaction", SonhosTransaction)
}