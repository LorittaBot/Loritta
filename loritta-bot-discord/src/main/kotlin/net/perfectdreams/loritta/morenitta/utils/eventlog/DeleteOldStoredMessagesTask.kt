package net.perfectdreams.loritta.morenitta.utils.eventlog

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.cinnamon.pudding.tables.StoredMessages
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import java.sql.Connection
import java.time.Instant

class DeleteOldStoredMessagesTask(val loritta: LorittaBot) : Runnable {
    companion object {
        private const val TWO_WEEKS = 1_209_600_000L
    }

    override fun run() {
        // Because this is only a clean up task, we don't care about reading uncommited changes.
        // We also try executing it 999 times, not really a big issue because we *want* the data to be deleted.
        val now = Instant.now()
        val nowTwoWeeksAgo = now.minusMillis(TWO_WEEKS)
        runBlocking {
            loritta.pudding.transaction(999, Connection.TRANSACTION_READ_UNCOMMITTED) {
                StoredMessages.deleteWhere {
                    StoredMessages.createdAt lessEq nowTwoWeeksAgo
                }
            }
        }
    }
}