package net.perfectdreams.loritta.lorituber.server.processors

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberChannels
import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberMails
import net.perfectdreams.loritta.serializable.lorituber.LoriTuberChannel
import net.perfectdreams.loritta.serializable.lorituber.requests.GetChannelByIdRequest
import net.perfectdreams.loritta.serializable.lorituber.requests.GetMailRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.GetChannelByIdResponse
import net.perfectdreams.loritta.serializable.lorituber.responses.GetMailResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class GetMailRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: GetMailRequest, currentTick: Long, lastUpdate: Long): GetMailResponse {
        val mail = LoriTuberMails.select {
            LoriTuberMails.character eq request.characterId and (LoriTuberMails.acknowledged eq false)
        }.orderBy(LoriTuberMails.date, SortOrder.ASC)
            .firstOrNull() ?: return GetMailResponse(
            currentTick,
            lastUpdate,
            null
        )

        return GetMailResponse(
            currentTick,
            lastUpdate,
            GetMailResponse.MailWrapper(
                mail[LoriTuberMails.id].value,
                Json.decodeFromString(mail[LoriTuberMails.type])
            )
        )
    }
}