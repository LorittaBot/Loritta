package net.perfectdreams.loritta.morenitta.utils.eventlog

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.pudding.tables.StoredMessages
import org.jetbrains.exposed.sql.SqlExpressionBuilder.lessEq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class DeleteOldStoredMessagesTask(val loritta: LorittaBot) : Runnable {
    companion object {
        private const val TWO_WEEKS = 1_209_600_000
    }

    override fun run() {
        // Because this is only a clean up task, we don't care about reading uncommited changes.
        // We also try executing it 999 times, not really a big issue because we *want* the data to be deleted.
        runBlocking {
            loritta.pudding.transaction(999, Connection.TRANSACTION_READ_UNCOMMITTED) {
                StoredMessages.deleteWhere {
                    StoredMessages.createdAt lessEq System.currentTimeMillis() - TWO_WEEKS
                }
            }
        }
    }
}