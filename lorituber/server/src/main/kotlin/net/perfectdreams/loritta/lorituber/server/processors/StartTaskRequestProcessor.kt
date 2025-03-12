package net.perfectdreams.loritta.lorituber.server.processors

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import net.perfectdreams.loritta.serializable.lorituber.requests.StartTaskRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.StartTaskResponse
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

class StartTaskRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: StartTaskRequest, currentTick: Long, lastUpdate: Long): StartTaskResponse {
        if (LoriTuberCharacters.selectAll().where { LoriTuberCharacters.id eq request.characterId }.firstOrNull()?.get(LoriTuberCharacters.currentTask) != null) {
            return StartTaskResponse.CharacterIsAlreadyDoingAnotherTask(
                currentTick,
                lastUpdate
            )
        }

        LoriTuberCharacters.update({ LoriTuberCharacters.id eq request.characterId }) {
            it[LoriTuberCharacters.currentTask] = Json.encodeToString<LoriTuberTask>(request.task)
        }

        return StartTaskResponse.Success(
            currentTick,
            lastUpdate
        )
    }
}