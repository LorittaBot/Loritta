package net.perfectdreams.loritta.cinnamon.pudding.utils.exposed

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager

fun FieldSet.selectFirst(where: SqlExpressionBuilder.() -> Op<Boolean>) = this.selectAll().where(where).limit(1).single()
fun FieldSet.selectFirstOrNull(where: SqlExpressionBuilder.() -> Op<Boolean>) = this.selectAll().where(where).limit(1).singleOrNull()
fun Query.selectFirst(where: SqlExpressionBuilder.() -> Op<Boolean>) = this.where(where).limit(1).single()
fun Query.selectFirstOrNull(where: SqlExpressionBuilder.() -> Op<Boolean>) = this.where(where).limit(1).singleOrNull()

/**
 * Disables synchronous commit on the current transaction.
 *
 * This gives a performance boost, however because the transaction is async, you may lose data if PostgreSQL crashes!
 */
fun disableSynchronousCommit() = TransactionManager.current().exec("SET LOCAL synchronous_commit = 'off';")