package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateCharacterRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateCharacterResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberCharacterData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import java.util.*

class CreateCharacterProcessor(val m: LoriTuberServer) : PacketProcessor<CreateCharacterRequest> {
    override suspend fun process(request: CreateCharacterRequest): LoriTuberResponse {
        /* return m.transaction {
            val canCreateANewCharacter = LoriTuberCharacters.selectAll()
                .where {
                    LoriTuberCharacters.ownerId eq request.userId
                }.count() == 0L

            return@transaction if (!canCreateANewCharacter)
                CreateCharacterResponse.UserAlreadyHasTooManyCharacters
            else {
                val serverInfo = LoriTuberWorldTicks.selectAll()
                    .where { LoriTuberWorldTicks.type eq LoriTuberServer.GENERAL_INFO_KEY }
                    .first()
                    .let {
                        ServerInfo(
                            it[LoriTuberWorldTicks.currentTick],
                            it[LoriTuberWorldTicks.lastUpdate],
                        )
                    }

                val newCharacter = LoriTuberCharacters.insert {
                    it[LoriTuberCharacters.ownerId] = request.userId
                    it[LoriTuberCharacters.firstName] = request.firstName
                    it[LoriTuberCharacters.lastName] = request.lastName
                    it[LoriTuberCharacters.energyNeed] = 100.0
                    it[LoriTuberCharacters.hungerNeed] = 100.0
                    it[LoriTuberCharacters.funNeed] = 100.0
                    it[LoriTuberCharacters.hygieneNeed] = 100.0
                    it[LoriTuberCharacters.bladderNeed] = 100.0
                    it[LoriTuberCharacters.socialNeed] = 100.0
                    it[LoriTuberCharacters.createdAtTick] = serverInfo.currentTick
                    it[LoriTuberCharacters.ticksLived] = 0
                }

                CreateCharacterResponse.Success(
                    newCharacter[LoriTuberCharacters.id].value,
                    newCharacter[LoriTuberCharacters.firstName]
                )
            }
        } */

        val userAlreadyHasACharacter = m.gameState.characters.any {
            it.id == request.userId
        }

        if (userAlreadyHasACharacter)
            return CreateCharacterResponse.UserAlreadyHasTooManyCharacters

        val character = LoriTuberCharacter(
            m.gameState.nextCharacterId(),
            LoriTuberCharacterData(
                request.userId,
                0,
                request.firstName,
                request.lastName,
                m.gameState.worldInfo.currentTick,
                0,
                100.0,
                100.0,
                100.0,
                100.0,
                100.0,
                100.0,
                null,
                mutableListOf(
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.COMPUTER.id,
                        1,
                        LoriTuberItemBehaviorAttributes.Computer(
                            0,
                            0,
                            null
                        )
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.PHONE.id,
                        1,
                        LoriTuberItemBehaviorAttributes.Phone(null)
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHEAP_TOILET.id,
                        1,
                        LoriTuberItemBehaviorAttributes.Toilet(0, false, 0)
                    )
                ),
                null,
                0,
            )
        )

        character.isDirty = true

        m.gameState.charactersById[character.id] = character

        return CreateCharacterResponse.Success(
            character.id,
            character.data.firstName
        )
    }
}