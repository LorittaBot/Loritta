package com.mrpowergamerbr.loritta.utils.eventlog

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.StoredMessages
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction

class DeleteOldStoredMessagesTask : Runnable {
    companion object {
        private const val TWO_WEEKS = 1_209_600_000
    }

    override fun run() {
        transaction(Databases.loritta) {
            StoredMessages.deleteWhere {
                StoredMessages.createdAt lessEq System.currentTimeMillis() - TWO_WEEKS
            }
        }
    }
}