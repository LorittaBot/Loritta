package net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.jsonb
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.common.utils.TransactionType
import org.jetbrains.exposed.dao.id.LongIdTable

object SimpleSonhosTransactionsLog : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp").index()
    val user = reference("user", Profiles).index()
    val type = postgresEnumeration<TransactionType>("type").index()
    val sonhos = long("sonhos")
    val metadata = jsonb("metadata")
}