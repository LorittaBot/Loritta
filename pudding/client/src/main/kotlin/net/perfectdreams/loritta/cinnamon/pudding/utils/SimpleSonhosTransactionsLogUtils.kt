package net.perfectdreams.loritta.cinnamon.pudding.utils

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.simpletransactions.SimpleSonhosTransactionsLog
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.serializable.StoredSonhosTransaction
import org.jetbrains.exposed.sql.insert
import java.time.Instant

object SimpleSonhosTransactionsLogUtils {
    /**
     * Inserts a simple sonhos transactions log into the database
     *
     * **This does not add nor remove sonhos from the user!**
     */
    fun insert(
        userId: Long,
        timestamp: Instant,
        type: TransactionType,
        sonhos: Long,
        metadata: StoredSonhosTransaction
    ) {
        SimpleSonhosTransactionsLog.insert {
            it[SimpleSonhosTransactionsLog.user] = userId
            it[SimpleSonhosTransactionsLog.timestamp] = timestamp
            it[SimpleSonhosTransactionsLog.type] = type
            it[SimpleSonhosTransactionsLog.sonhos] = sonhos
            it[SimpleSonhosTransactionsLog.metadata] = Json.encodeToString<StoredSonhosTransaction>(metadata)
        }
    }
}