package net.perfectdreams.loritta.lorituber.rpc.packets

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.UUIDSerializer
import net.perfectdreams.loritta.lorituber.bhav.UseItemAttributes
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId
import java.util.*

@Serializable
sealed class LoriTuberTask {
    @Serializable
    class Sleeping : LoriTuberTask()

    @Serializable
    class TakingAShower : LoriTuberTask()

    @Serializable
    class UsingToilet : LoriTuberTask()

    @Serializable
    class Eating(val itemId: LoriTuberItemId, val startedEatingAtTick: Long) : LoriTuberTask()

    @Serializable
    class PreparingFood(
        /**
         * If null, then we are making slop
         */
        val recipeId: String?,
        val items: List<LoriTuberItemId>,
        val startedPreparingAtTick: Long
    ) : LoriTuberTask()

    @Serializable
    class WorkingOnVideo(
        val channelId: Long,
        val pendingVideoId: Long,
        val stage: LoriTuberVideoStage
    ) : LoriTuberTask()

    // A catch-all when an item is delegated to a ItemBehavior
    @Serializable
    class UsingItem(
        @Serializable(with = UUIDSerializer::class)
        val itemLocalId: UUID,
        val useItemAttributes: UseItemAttributes
    ) : LoriTuberTask()
}