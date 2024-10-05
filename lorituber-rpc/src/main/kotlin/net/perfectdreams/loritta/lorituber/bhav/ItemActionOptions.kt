package net.perfectdreams.loritta.lorituber.bhav

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.lorituber.items.LoriTuberItemId

@Serializable
sealed class ItemActionOption {
    @Serializable
    data object PlayOnSparklyPower : ItemActionOption()

    @Serializable
    data object AnswerPhone : ItemActionOption()

    @Serializable
    data object DoomscrollSocialNetwork : ItemActionOption()

    @Serializable
    data object UseToilet : ItemActionOption()

    @Serializable
    data object UnclogToilet : ItemActionOption()

    @Serializable
    data object TakeAShower : ItemActionOption()

    @Serializable
    data object Sleep : ItemActionOption()

    @Serializable
    data object PrepareFoodMenu : ItemActionOption()

    @Serializable
    data class PrepareFood(val items: List<LoriTuberItemId>) : ItemActionOption()

    @Serializable
    data object EatFoodMenu : ItemActionOption()

    @Serializable
    data object EatFood : ItemActionOption()

    /* @Serializable
    data object EatFood : ItemActionOption() */
}