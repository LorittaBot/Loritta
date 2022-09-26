package net.perfectdreams.loritta.morenitta.utils.extensions

import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.dao.Entity

/**
 * Refreshes the current entity within a transaction
 *
 * @see [Entity.refresh]
 */
fun Entity<*>.refreshInTransaction(loritta: LorittaBot, flush: Boolean = false) = loritta.transaction { this@refreshInTransaction.refresh(flush) }

/**
 * Refreshes the current entity within a deferred transaction
 *
 * @see [Entity.refresh]
 */
suspend fun Entity<*>.refreshInDeferredTransaction(loritta: LorittaBot, flush: Boolean = false) = loritta.suspendedTransactionAsync { this@refreshInDeferredTransaction.refresh(flush) }

/**
 * Refreshes the current entity within a async transaction
 *
 * @see [Entity.refresh]
 */
suspend fun Entity<*>.refreshInAsyncTransaction(loritta: LorittaBot, flush: Boolean = false) = loritta.newSuspendedTransaction { this@refreshInAsyncTransaction.refresh(flush) }