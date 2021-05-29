package net.perfectdreams.loritta.utils.extensions

import com.mrpowergamerbr.loritta.utils.loritta
import org.jetbrains.exposed.dao.Entity

/**
 * Refreshes the current entity within a transaction
 *
 * @see [Entity.refresh]
 */
fun Entity<*>.refreshInTransaction(flush: Boolean = false) = loritta.transaction { this@refreshInTransaction.refresh(flush) }

/**
 * Refreshes the current entity within a deferred transaction
 *
 * @see [Entity.refresh]
 */
suspend fun Entity<*>.refreshInDeferredTransaction(flush: Boolean = false) = loritta.suspendedTransactionAsync { this@refreshInDeferredTransaction.refresh(flush) }

/**
 * Refreshes the current entity within a async transaction
 *
 * @see [Entity.refresh]
 */
suspend fun Entity<*>.refreshInAsyncTransaction(flush: Boolean = false) = loritta.newSuspendedTransaction { this@refreshInAsyncTransaction.refresh(flush) }