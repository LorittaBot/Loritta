package net.perfectdreams.loritta.lorituber.bhav

import kotlinx.serialization.Serializable

@Serializable
sealed class ObjectActionOption {
    // "Special feedback" options require... you guessed it, special feedback on the frontend side
    // (Example: Answer Phone requires a different message to be received and stuffz)
    @Serializable
    sealed class SpecialFeedbackOption : ObjectActionOption()

    // "Generic options" options require no special feedback on the frontend side, it is a "fire and forget" solution
    @Serializable
    sealed class GenericOption : ObjectActionOption() {

    }

    @Serializable
    data object PlayOnSparklyPower : GenericOption()

    @Serializable
    data object AnswerPhone : SpecialFeedbackOption()

    /* @Serializable
    data object DoomscrollSocialNetwork : ObjectActionOption()

    @Serializable
    data object UseToilet : ObjectActionOption()

    @Serializable
    data object UnclogToilet : ObjectActionOption()

    @Serializable
    data object TakeAShower : ObjectActionOption()

    @Serializable
    data object Sleep : ObjectActionOption()

    @Serializable
    data object PrepareFoodMenu : ObjectActionOption()

    @Serializable
    data class PrepareFood(val items: List<LoriTuberItemId>) : ObjectActionOption()

    @Serializable
    data object EatFoodMenu : ObjectActionOption()

    @Serializable
    data object EatFood : ObjectActionOption()

    @Serializable
    data object GoOutside : ObjectActionOption()

    @Serializable
    data object GoBackToHome : ObjectActionOption()

    @Serializable
    data object GoToNelsonGroceryStore : ObjectActionOption()

    @Serializable
    data object GoToStarryBeach : ObjectActionOption()

    @Serializable
    data object GoIntoTheSea : ObjectActionOption()

    @Serializable
    data object Give1000Sonhos : ObjectActionOption()

    @Serializable
    data object ViewCurrentLotInfo : ObjectActionOption()

    @Serializable
    data object BrowseStoreItems : ObjectActionOption()

    @Serializable
    data class BuyStoreItem(val item: LoriTuberItemId) : ObjectActionOption() */

    /* @Serializable
    data object EatFood : ItemActionOption() */
}