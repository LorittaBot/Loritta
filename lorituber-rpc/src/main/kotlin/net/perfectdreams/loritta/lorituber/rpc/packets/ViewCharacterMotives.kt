package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.bhav.ItemActionRoot
import net.perfectdreams.loritta.lorituber.items.LoriTuberGroceryItemData
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemStackData
import java.util.*

@Serializable
data class ViewCharacterMotivesRequest(
    @Serializable(UUIDSerializer::class)
    val characterId: UUID,
) : LoriTuberRequest()

@Serializable
data class ViewCharacterMotivesResponse(
    val currentTick: Long,
    val name: String,
    val sonhos: Long,
    val mood: Double,
    val energyNeed: Double,
    val hungerNeed: Double,
    val funNeed: Double,
    val hygieneNeed: Double,
    val bladderNeed: Double,
    val socialNeed: Double,
    val currentTask: LoriTuberTask?,
    val items: List<LoriTuberItemStackData>,
    val itemActions: List<ItemActionRoot>,
    val lotScene: LotScene
) : LoriTuberResponse() {
    @Serializable
    sealed class LotScene {
        abstract val currentLotId: UUID

        /**
         * A lot that has objects that the user can interact with
         */
        @Serializable
        data class InteractableLotScene(
            @Serializable(UUIDSerializer::class)
            override val currentLotId: UUID,
            val items: List<LoriTuberItemStackData>
        ) : LotScene()

        @Serializable
        data class ItemStoreLotScene(
            @Serializable(UUIDSerializer::class)
            override val currentLotId: UUID,
            val items: List<LoriTuberGroceryItemData>
        ) : LotScene()
    }
}