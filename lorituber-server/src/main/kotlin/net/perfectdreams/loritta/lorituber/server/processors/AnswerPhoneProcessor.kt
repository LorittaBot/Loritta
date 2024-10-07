package net.perfectdreams.loritta.lorituber.server.processors

import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.rpc.packets.AnswerPhoneRequest
import net.perfectdreams.loritta.lorituber.rpc.packets.AnswerPhoneResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberResponse
import net.perfectdreams.loritta.lorituber.rpc.packets.LoriTuberTask
import net.perfectdreams.loritta.lorituber.server.LoriTuberServer

class AnswerPhoneProcessor(val m: LoriTuberServer) : PacketProcessor<AnswerPhoneRequest> {
    override suspend fun process(request: AnswerPhoneRequest): LoriTuberResponse {
        val character = m.gameState.characters.first { it.id == request.characterId }

        if (character.data.currentTask is LoriTuberTask.Sleeping)
            return AnswerPhoneResponse.YouCantAnswerThePhoneWhileSleeping

        val pendingPhoneCall = character.data.pendingPhoneCall
        if (pendingPhoneCall != null) {
            character.setPendingPhoneCall(null)

            if (pendingPhoneCall.phoneCall is PhoneCall.SonhosReward) {
                character.addSonhos(pendingPhoneCall.phoneCall.sonhosReward)
            }

            return AnswerPhoneResponse.Success(pendingPhoneCall.phoneCall)
        } else {
            return AnswerPhoneResponse.NoCall
        }
    }
}