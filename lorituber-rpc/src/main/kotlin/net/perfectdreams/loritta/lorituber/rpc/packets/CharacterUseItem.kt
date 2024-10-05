package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.bhav.ItemActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import java.util.*

@Serializable
data class CharacterUseItemRequest(
    val characterId: Long,
    @Serializable(UUIDSerializer::class) val localId: UUID,
    val itemActionOption: ItemActionOption
    // val useItemAttributes: UseItemAttributes
) : LoriTuberRequest()

@Serializable
sealed class CharacterUseItemResponse : LoriTuberResponse() {
    @Serializable
    sealed class Success : CharacterUseItemResponse() {
        @Serializable
        data object NoAction : Success()

        @Serializable
        sealed class Fridge : Success() {
            @Serializable
            class PrepareFoodMenu(val inventory: List<LoriTuberItemStackData>) : Fridge()

            @Serializable
            class EatFoodMenu(val inventory: List<LoriTuberItemStackData>) : Fridge()
        }

        @Serializable
        sealed class AnswerCall : Success() {
            @Serializable
            class Success(val call: PhoneCall) : AnswerCall()

            @Serializable
            data object NoCall : AnswerCall()

            @Serializable
            data object YouCantAnswerThePhoneWhileSleeping : AnswerCall()
        }
    }

    @Serializable
    data object UnknownLocalItem : CharacterUseItemResponse()
}
