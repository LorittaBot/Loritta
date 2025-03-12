package net.perfectdreams.loritta.lorituber.server.processors

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberMails
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberMail
import net.perfectdreams.loritta.serializable.lorituber.requests.CreateChannelRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.CreateChannelResponse
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class CreateChannelRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: CreateChannelRequest, currentTick: Long, lastUpdate: Long): CreateChannelResponse {
        val canCreateANewChannel = LoriTuberChannels.selectAll().where {
            LoriTuberChannels.owner eq request.characterId
        }.count() == 0L

        return if (!canCreateANewChannel)
            CreateChannelResponse.CharacterAlreadyHasTooManyChannels(
                currentTick,
                lastUpdate,
            )
        else {
            val newChannel = LoriTuberChannels.insert {
                it[LoriTuberChannels.owner] = request.characterId
                it[LoriTuberChannels.name] = request.name
            }

            // TODO: If the user already has an channel, show a different message, maybe something like "Player is so good, that they created a second channel!"

            LoriTuberMails.insert {
                it[LoriTuberMails.character] = request.characterId
                it[LoriTuberMails.date] = Instant.now()
                it[LoriTuberMails.type] = Json.encodeToString<LoriTuberMail>(LoriTuberMail.BeginnerChannelCreated(request.characterId, newChannel[LoriTuberChannels.id].value))
                it[LoriTuberMails.acknowledged] = false
            }

            CreateChannelResponse.Success(
                currentTick,
                lastUpdate,
                newChannel[LoriTuberChannels.id].value,
                newChannel[LoriTuberChannels.name]
            )
        }
    }
}