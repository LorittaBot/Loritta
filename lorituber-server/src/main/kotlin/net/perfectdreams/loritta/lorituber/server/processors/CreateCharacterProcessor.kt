package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.LotType
import net.perfectdreams.loritta.lorituber.bhav.LoriTuberItemBehaviorAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItems
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateCharacterRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.CreateCharacterResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberCharacterData
import net.perfectdreams.loritta.lorituber.server.state.data.LoriTuberLotData
import net.perfectdreams.loritta.lorituber.server.state.entities.LoriTuberCharacter
import net.perfectdreams.loritta.lorituber.server.state.entities.lots.LoriTuberLot
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
            it.data.ownerId == request.userId
        }

        if (userAlreadyHasACharacter)
            return CreateCharacterResponse.UserAlreadyHasTooManyCharacters

        val characterId = m.gameState.generateCharacterId()
        val lotId = m.gameState.generateLotId()

        val lot = LoriTuberLot(
            lotId,
            LoriTuberLotData(
                LotType.Residential(characterId),
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
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHEAP_SHOWER.id,
                        1,
                        LoriTuberItemBehaviorAttributes.Shower
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHEAP_BED.id,
                        1,
                        null
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHEAP_FRIDGE.id,
                        1,
                        null
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.CHARACTER_PORTAL.id,
                        1,
                        null
                    )
                ),
            )
        )

        val character = LoriTuberCharacter(
            characterId,
            LoriTuberCharacterData(
                request.userId,
                lotId,
                lotId,
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
                        LoriTuberItems.PHONE.id,
                        1,
                        LoriTuberItemBehaviorAttributes.Phone(null)
                    ),
                    LoriTuberItemStackData(
                        UUID.randomUUID(),
                        LoriTuberItems.DEBUG_MODE.id,
                        1,
                        null
                    )
                ),
                /* mutableListOf(
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
                ), */
                null,
                0,
            )
        )

        lot.isDirty = true
        character.isDirty = true

        m.gameState.lotsById[lot.id] = lot
        m.gameState.charactersById[character.id] = character

        return CreateCharacterResponse.Success(
            character.id,
            character.data.firstName
        )
    }
}