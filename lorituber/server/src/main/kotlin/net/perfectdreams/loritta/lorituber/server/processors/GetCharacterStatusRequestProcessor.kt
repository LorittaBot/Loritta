package net.perfectdreams.loritta.lorituber.server.processors

import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberTask
import net.perfectdreams.loritta.serializable.lorituber.requests.GetCharacterStatusRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetCharacterStatusResponse
import org.jetbrains.exposed.sql.select

class GetCharacterStatusRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: GetCharacterStatusRequest, currentTick: Long, lastUpdate: Long): GetCharacterStatusResponse {
        val character = LoriTuberCharacters.select {
            LoriTuberCharacters.id eq request.characterId
        }.first()

        return GetCharacterStatusResponse(
            currentTick,
            lastUpdate,
            character[LoriTuberCharacters.name],
            character[LoriTuberCharacters.energyNeed],
            character[LoriTuberCharacters.hungerNeed],
            character[LoriTuberCharacters.currentTask]?.let { Json.decodeFromString<LoriTuberTask>(it) }
        )
    }
}