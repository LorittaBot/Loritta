package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.serializable.lorituber.requests.CancelTaskRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CancelTaskResponse
import org.jetbrains.exposed.sql.update

class CancelTaskRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: CancelTaskRequest, currentTick: Long, lastUpdate: Long): CancelTaskResponse {
        LoriTuberCharacters.update({ LoriTuberCharacters.id eq request.characterId }) {
            it[LoriTuberCharacters.currentTask] = null
        }

        return CancelTaskResponse(
            currentTick,
            lastUpdate
        )
    }
}