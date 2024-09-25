package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberCharacters
import net.perfectdreams.loritta.serializable.lorituber.requests.CreateCharacterRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CreateCharacterResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.LoriTuberRPCResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select

class CreateCharacterRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: CreateCharacterRequest, currentTick: Long, lastUpdate: Long): LoriTuberRPCResponse {
        val canCreateANewCharacter = LoriTuberCharacters.select {
            LoriTuberCharacters.owner eq request.ownerId
        }.count() == 0L

        return if (!canCreateANewCharacter)
            CreateCharacterResponse.UserAlreadyHasTooManyCharacters(
                currentTick,
                lastUpdate,
            )
        else {
            val newCharacter = LoriTuberCharacters.insert {
                it[LoriTuberCharacters.name] = request.name
                it[LoriTuberCharacters.owner] = request.ownerId
                it[LoriTuberCharacters.energyNeed] = 100.0
                it[LoriTuberCharacters.hungerNeed] = 100.0
            }

            CreateCharacterResponse.Success(
                currentTick,
                lastUpdate,
                newCharacter[LoriTuberCharacters.id].value,
                newCharacter[LoriTuberCharacters.name]
            )
        }
    }
}