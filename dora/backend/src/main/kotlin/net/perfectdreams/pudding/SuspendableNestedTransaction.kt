package net.perfectdreams.pudding

import kotlinx.coroutines.withContext
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext

private val logger by HarmonyLoggerFactory.logger {}

/**
 * A suspendable transaction block that reuses transactions if nested.
 *
 * ```kotlin
 * transaction(dispatcher, database) {
 *      val transaction1 = this
 *
 *      transaction(dispatcher, database) {
 *          val transaction2 = this
 *
 *          assert(transaction1 == transaction2) // true!
 *      }
 * }
 */
// https://github.com/JetBrains/Exposed/issues/1003
suspend fun <T> suspendableNestableTransaction(
    context: CoroutineContext,
    database: Database?,
    repetitions: Int = 5,
    transactionIsolation: Int? = null,
    beforeNewTransactionCallBlock: suspend (suspend () -> (T)) -> (T), // We don't provide a default lambda because for some reason that's causing issues with the K2 compiler
    statement: suspend Transaction.() -> T
): T {
    // Handle nested transactions
    // TECHNICALLY Exposed should handle this correctly if you are using coroutines, but it doesn't
    // So we are going to handle it ourselves
    //
    // If a CoroutineTransaction is present, we will use it to invoke the statement
    // This allows nested transactions to reuse an already executing coroutine
    // And because it is nested, an exception SHOULD roll back without issues
    val coroutineTransaction = coroutineContext[CoroutineTransaction]
    if (coroutineTransaction != null)
        return statement.invoke(coroutineTransaction.transaction)

    var lastException: Exception? = null
    for (i in 1..repetitions) {
        try {
            return beforeNewTransactionCallBlock.invoke {
                return@invoke newSuspendedTransaction(
                    context,
                    database,
                    transactionIsolation
                ) {
                    // We are handling the repetition attempts ourselves, see h
                    // https://github.com/JetBrains/Exposed/commit/88315512d392a7f723ae387f87f9656ccf9b8210
                    // If we keep Exposed handling the repetition attempts, it will throw exceptions saying that the connection is closed
                    this.maxAttempts = 1

                    withContext(coroutineContext + CoroutineTransaction(this)) {
                        statement.invoke(this@newSuspendedTransaction)
                    }
                }
            }
        } catch (e: ExposedSQLException) {
            logger.warn(e) { "Exception while trying to execute query. Tries: $i" }
            lastException = e
        }
    }
    throw lastException ?: RuntimeException("This should never happen")
}

// This is public because maybe someone wants to access the stored transaction in the current context
class CoroutineTransaction(
    val transaction: Transaction
) : AbstractCoroutineContextElement(CoroutineTransaction) {
    companion object Key : CoroutineContext.Key<CoroutineTransaction>
}