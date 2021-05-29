package com.mrpowergamerbr.loritta.utils.eventlog

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.StoredMessages
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.Connection

class DeleteOldStoredMessagesTask : Runnable {
    companion object {
        private const val TWO_WEEKS = 1_209_600_000
    }

    override fun run() {
        // Because this is only a clean up task, we don't care about reading uncommited changes.
        // We also try executing it 999 times, not really a big issue because we *want* the data to be deleted.
        transaction(Connection.TRANSACTION_READ_UNCOMMITTED, 999, Databases.loritta) {
            StoredMessages.deleteWhere {
                StoredMessages.createdAt lessEq System.currentTimeMillis() - TWO_WEEKS
            }
        }
    }
}