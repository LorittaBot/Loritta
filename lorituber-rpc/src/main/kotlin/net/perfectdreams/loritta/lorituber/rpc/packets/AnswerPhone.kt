package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.PhoneCall

@Serializable
data class AnswerPhoneRequest(val characterId: Long) : LoriTuberRequest()

@Serializable
sealed class AnswerPhoneResponse : LoriTuberResponse() {
    @Serializable
    class Success(val call: PhoneCall) : AnswerPhoneResponse()

    @Serializable
    data object NoCall : AnswerPhoneResponse()

    @Serializable
    data object YouCantAnswerThePhoneWhileSleeping : AnswerPhoneResponse()
}