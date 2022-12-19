package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber.LoriTuberMails
import net.perfectdreams.loritta.serializable.lorituber.requests.AcknowledgeMailRequest
import net.perfectdreams.loritta.serializable.lorituber.responses.AcknowledgeMailResponse
import org.jetbrains.exposed.sql.update

class AcknowledgeMailRequestProcessor : LoriTuberRpcProcessor {
    suspend fun process(request: AcknowledgeMailRequest, currentTick: Long, lastUpdate: Long): AcknowledgeMailResponse {
        LoriTuberMails.update({ LoriTuberMails.id eq request.mailId }) {
            it[LoriTuberMails.acknowledged] = true
        }

        return AcknowledgeMailResponse(
            currentTick,
            lastUpdate
        )
    }
}