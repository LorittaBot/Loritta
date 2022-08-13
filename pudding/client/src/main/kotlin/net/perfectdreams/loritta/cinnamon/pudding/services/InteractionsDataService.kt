package net.perfectdreams.loritta.cinnamon.pudding.services

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.InteractionsData
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select

class InteractionsDataService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getInteractionData(
        interactionId: Long
    ): JsonObject? {
        return pudding.transaction { _getInteractionData(interactionId) }
    }

    fun _getInteractionData(
        interactionId: Long
    ): JsonObject? {
        return Json.parseToJsonElement(
            InteractionsData.selectFirstOrNull {
                InteractionsData.id eq interactionId
            }?.getOrNull(InteractionsData.data) ?: return null
        ).jsonObject
    }

    suspend fun deleteInteractionData(
        interactionId: Long
    ) {
        pudding.transaction {
            _deleteInteractionData(interactionId)
        }
    }

    fun _deleteInteractionData(
        interactionId: Long
    ) {
        InteractionsData.deleteWhere {
            InteractionsData.id eq interactionId
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