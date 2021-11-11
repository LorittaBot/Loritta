package net.perfectdreams.loritta.cinnamon.pudding.utils.exposed

import org.jetbrains.exposed.sql.FieldSet
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.select

fun FieldSet.selectFirst(where: SqlExpressionBuilder.() -> Op<Boolean>) = select(where).limit(1).single()
fun FieldSet.selectFirstOrNull(where: SqlExpressionBuilder.() -> Op<Boolean>) = select(where).limit(1).singleOrNull()