package net.perfectdreams.loritta.cinnamon.pudding.tables.transactions

import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosTransactionsLog
import org.jetbrains.exposed.dao.id.LongIdTable

object ShipEffectSonhosTransactionsLog : LongIdTable() {
    val timestampLog = reference("timestamp_log", SonhosTransactionsLog).index()
    val effect = reference("bundle", ShipEffects)
    val sonhos = long("sonhos")
}