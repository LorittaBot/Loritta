package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import java.util.*

@Serializable
data class AnswerPhoneRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID
) : LoriTuberRequest()

@Serializable
sealed class AnswerPhoneResponse : LoriTuberResponse() {
    @Serializable
    class Success(val call: PhoneCall) : AnswerPhoneResponse()

    @Serializable
    data object NoCall : AnswerPhoneResponse()

    @Serializable
    data object YouCantAnswerThePhoneWhileSleeping : AnswerPhoneResponse()
}