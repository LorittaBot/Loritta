package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LotType
import net.perfectdreams.loritta.lorituber.PhoneCall
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.bhav.ObjectActionOption
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import java.util.*

@Serializable
data class CharacterUseItemRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
    @Serializable(UUIDSerializer::class) val localId: UUID,
    val objectActionOption: ObjectActionOption
    // val useItemAttributes: UseItemAttributes
) : LoriTuberRequest()

@Serializable
sealed class CharacterUseItemResponse : LoriTuberResponse() {
    @Serializable
    sealed class Success : CharacterUseItemResponse() {
        @Serializable
        data object NoAction : Success()

        @Serializable
        data class ShowMessage(val message: UseItemMessage) : Success()

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

        @Serializable
        sealed class ItemStore : Success() {
            @Serializable
            sealed class BrowseItems : ItemStore() {
                @Serializable
                data class Success(
                    val characterSonhos: Long,
                    val items: List<LoriTuberGroceryItemData>
                ) : BrowseItems()

                @Serializable
                data object Closed : BrowseItems()
            }

            @Serializable
            sealed class BuyItem : ItemStore() {
                @Serializable
                data object Success : BuyItem()

                @Serializable
                data object NotInStock : BuyItem()

                @Serializable
                data object NotEnoughSonhos : BuyItem()

                @Serializable
                data object Closed : BuyItem()
            }
        }

        @Serializable
        sealed class DebugMode : Success() {
            @Serializable
            class LotInfo(
                @Serializable(UUIDSerializer::class)
                val id: UUID,
                val lotType: LotType
            ) : DebugMode()
        }
    }

    @Serializable
    data object UnknownLocalItem : CharacterUseItemResponse()

    @Serializable
    data object AnotherCharacterIsAlreadyUsingThisItem : CharacterUseItemResponse()
}
