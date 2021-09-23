package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.InteractionsData
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

class InteractionsDataService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getInteractionData(
        interactionId: Long
    ): JsonObject? {
        return Json.parseToJsonElement(
            pudding.transaction {
                InteractionsData.select {
                    InteractionsData.id eq interactionId
                }.firstOrNull()
            }?.getOrNull(InteractionsData.data) ?: return null
        ).jsonObject
    }

    suspend fun deleteInteractionData(
        interactionId: Long
    ) {
        return pudding.transaction {
            InteractionsData.deleteWhere {
                InteractionsData.id eq interactionId
            }
        }
    }

    suspend fun insertInteractionData(
        data: JsonObject,
        addedAt: Instant,
        expiresAt: Instant
    ): Long {
        return pudding.transaction {
            InteractionsData.insertAndGetId {
                it[InteractionsData.data] = data.toString()
                it[InteractionsData.addedAt] = addedAt.toJavaInstant()
                it[InteractionsData.expiresAt] = expiresAt.toJavaInstant()
            }
        }.value
    }
}