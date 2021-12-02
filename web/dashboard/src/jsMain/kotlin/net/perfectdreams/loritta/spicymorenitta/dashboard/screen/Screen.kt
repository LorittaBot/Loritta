package net.perfectdreams.loritta.spicymorenitta.dashboard.screen

sealed class Screen {
    class UserOverview(override val model: UserOverviewViewModel) : Screen(), ScreenWithViewModel
    object Test : Screen()

    interface ScreenWithViewModel {
        val model: ViewModel
    }
}