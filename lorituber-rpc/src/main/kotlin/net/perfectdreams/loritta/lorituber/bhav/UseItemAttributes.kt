package net.perfectdreams.loritta.lorituber.bhav

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.LoriTuberVideoStage
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId

/**
 * Use item attributes are used for "continuous" use of the item
 */
@Serializable
sealed class UseItemAttributes {
    @Serializable
    sealed class Computer : UseItemAttributes() {
        @Serializable
        class WorkOnVideo(
            val channelId: Long,
            val pendingVideoId: Long,
            val stage: LoriTuberVideoStage,
        ) : Computer()

        @Serializable
        data object PlayOnSparklyPower : Computer()
    }

    @Serializable
    sealed class Phone : UseItemAttributes() {
        @Serializable
        data object DoomscrollingSocialNetwork : Phone()
    }

    @Serializable
    sealed class Toilet : UseItemAttributes() {
        @Serializable
        data object UsingToilet : Toilet()

        @Serializable
        data object UncloggingToilet : Toilet()
    }

    @Serializable
    sealed class Shower : UseItemAttributes() {
        @Serializable
        data object TakingAShower : Shower()
    }

    @Serializable
    sealed class Bed : UseItemAttributes() {
        @Serializable
        data object Sleeping : Bed()
    }

    @Serializable
    sealed class Fridge : UseItemAttributes() {
        @Serializable
        data class PreparingFood(
            val recipeId: String?,
            val items: List<LoriTuberItemId>,
            val startedPreparingAtTick: Long
        ) : Fridge()
    }

    @Serializable
    sealed class Food : UseItemAttributes() {
        @Serializable
        data class EatingFood(val startedEatingAtTick: Long) : Food()
    }
}